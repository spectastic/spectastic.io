package xyz.briancorbin.pk.hex.recon;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import xyz.briancorbin.pk.hex.app.PositionService;
import xyz.briancorbin.pk.hex.persistence.CustodianSnapshotReader;

/**
 * The nightly reconciliation (007-reconciliation, D-001): read the custodian file, read the store
 * through the snapshot reader, and apply every delta through the position use case — so each
 * correction emits PositionChanged and ADR-0007 holds by construction. The tradeId {@code
 * recon:<sha8 of file>:<instrument>} makes a re-run over the same file a no-op.
 */
public final class ReconciliationJob {
  private final CustodianSnapshotReader snapshot;
  private final PositionService positions;

  public ReconciliationJob(CustodianSnapshotReader snapshot, PositionService positions) {
    this.snapshot = snapshot;
    this.positions = positions;
  }

  /** One correction the run applied. */
  public record Correction(String instrument, long from, long to, String tradeId) {}

  /** What a run did. */
  public record Report(String runId, List<Correction> corrections) {}

  public Report run(Path custodianFile) throws IOException {
    String runId = CustodianFileParser.sha8(custodianFile);
    List<Correction> corrections = new ArrayList<>();
    for (CustodianRecord record : CustodianFileParser.parse(custodianFile)) {
      long stored = snapshot.quantityOf(record.instrument());
      if (stored == record.quantity()) {
        continue;
      }
      String tradeId = "recon:" + runId + ":" + record.instrument();
      positions.correct(record.instrument(), record.quantity(), tradeId);
      corrections.add(new Correction(record.instrument(), stored, record.quantity(), tradeId));
    }
    return new Report(runId, List.copyOf(corrections));
  }
}
