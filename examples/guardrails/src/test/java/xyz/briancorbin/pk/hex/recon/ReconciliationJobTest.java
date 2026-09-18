package xyz.briancorbin.pk.hex.recon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import xyz.briancorbin.pk.hex.app.PositionService;
import xyz.briancorbin.pk.hex.consumers.RiskExposureView;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.core.TradeEvent;
import xyz.briancorbin.pk.hex.events.SyncEventBus;
import xyz.briancorbin.pk.hex.persistence.CustodianSnapshotReader;
import xyz.briancorbin.pk.hex.persistence.InMemoryPositionRepository;

/**
 * SC-001 / SC-002 of 007-reconciliation: a run over a file with drift corrects through the use case
 * — one PositionChanged per correction, Risk sees it, the store matches — and a re-run corrects
 * nothing twice.
 */
class ReconciliationJobTest {

  private static final Path FIXTURE = Path.of("src/test/resources/custodian/eod-2026-09-13.csv");

  private final SyncEventBus bus = new SyncEventBus();
  private final InMemoryPositionRepository repo = new InMemoryPositionRepository(bus);
  private final PositionService positions = new PositionService(repo);
  private final RiskExposureView risk = new RiskExposureView(bus);
  private final List<PositionChanged> emitted = new ArrayList<>();
  private final ReconciliationJob job =
      new ReconciliationJob(new CustodianSnapshotReader(repo), positions);

  private void bookDriftedBook() {
    positions.book(new TradeEvent("t-1", "AAPL", 90)); // custodian says 100
    positions.book(new TradeEvent("t-2", "MSFT", 250)); // matches
    bus.subscribe(emitted::add);
  }

  @Test
  void aDriftedPositionIsCorrectedThroughTheUseCaseAudibly() throws Exception {
    bookDriftedBook();

    ReconciliationJob.Report report = job.run(FIXTURE);

    String sha8 = CustodianFileParser.sha8(FIXTURE);
    assertEquals(List.of(new PositionChanged("AAPL", 100, "recon:" + sha8 + ":AAPL")), emitted);
    assertEquals(100, positions.current("AAPL").quantity());
    assertEquals(250, positions.current("MSFT").quantity());
    assertEquals(0, positions.current("VOD.L").quantity());
    assertEquals(100, risk.exposure("AAPL"));
    assertEquals(1, report.corrections().size());
    assertEquals("AAPL", report.corrections().get(0).instrument());
  }

  @Test
  void reRunningTheSameFileCorrectsNothingTwice() throws Exception {
    bookDriftedBook();

    job.run(FIXTURE);
    ReconciliationJob.Report second = job.run(FIXTURE);

    assertTrue(second.corrections().isEmpty());
    assertEquals(1, emitted.size());
    assertTrue(repo.wasApplied("recon:" + CustodianFileParser.sha8(FIXTURE) + ":AAPL"));
    assertEquals(100, positions.current("AAPL").quantity());
  }
}
