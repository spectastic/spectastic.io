package xyz.briancorbin.pk.hex.consumers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import xyz.briancorbin.pk.hex.app.PositionService;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.core.TradeEvent;
import xyz.briancorbin.pk.hex.events.SyncEventBus;
import xyz.briancorbin.pk.hex.persistence.InMemoryPositionRepository;

/**
 * SC-001 of 002-downstream-consumers: a trade booked through the use case reaches Risk via the
 * adapter's PositionChanged — synchronously, once per tradeId.
 */
class DownstreamConsumersTest {

  private final SyncEventBus bus = new SyncEventBus();
  private final InMemoryPositionRepository repo = new InMemoryPositionRepository(bus);
  private final PositionService service = new PositionService(repo);
  private final RiskExposureView risk = new RiskExposureView(bus);
  private final List<PositionChanged> recorded = new ArrayList<>();

  @Test
  void aBookedTradeReachesRiskBeforeBookReturns() {
    List<Long> exposureWhenEmitted = new ArrayList<>();
    bus.subscribe(event -> exposureWhenEmitted.add(risk.exposure("AAPL")));

    service.book(new TradeEvent("t-1", "AAPL", 100));

    assertEquals(100, risk.exposure("AAPL"));
    // Risk subscribed first, so by the time our recorder ran the view had already been updated —
    // delivery is synchronous and in subscription order (NFR-001, D-006).
    assertEquals(List.of(100L), exposureWhenEmitted);
  }

  @Test
  void theEventCarriesInstrumentQuantityAndTradeId() {
    bus.subscribe(recorded::add);

    service.book(new TradeEvent("t-1", "AAPL", 100));

    assertEquals(List.of(new PositionChanged("AAPL", 100, "t-1")), recorded);
  }

  @Test
  void rebookingTheSameTradeIdEmitsNothing() {
    bus.subscribe(recorded::add);

    service.book(new TradeEvent("t-1", "AAPL", 100));
    service.book(new TradeEvent("t-1", "AAPL", 100));

    assertEquals(1, recorded.size());
    assertTrue(repo.wasApplied("t-1"));
    assertEquals(100, risk.exposure("AAPL"));
  }
}
