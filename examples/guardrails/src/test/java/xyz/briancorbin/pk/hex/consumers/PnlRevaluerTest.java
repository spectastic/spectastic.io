package xyz.briancorbin.pk.hex.consumers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.events.SyncEventBus;

/** SC-002 of 002-downstream-consumers: a redelivered event leaves P&L's revaluation unchanged. */
class PnlRevaluerTest {

  private final SyncEventBus bus = new SyncEventBus();
  private final PnlRevaluer pnl = new PnlRevaluer(bus);

  @Test
  void aRedeliveredEventRevaluesOnce() {
    bus.emit(new PositionChanged("AAPL", 100, "t-1"));
    bus.emit(new PositionChanged("AAPL", 100, "t-1"));

    assertEquals(1, pnl.revaluations());
    assertEquals(100, pnl.marked("AAPL"));
  }

  @Test
  void aNewTradeIdRevaluesAgain() {
    bus.emit(new PositionChanged("AAPL", 100, "t-1"));
    bus.emit(new PositionChanged("AAPL", 60, "t-2"));

    assertEquals(2, pnl.revaluations());
    assertEquals(60, pnl.marked("AAPL"));
  }
}
