package xyz.briancorbin.pk.hex.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.TradeEvent;
import xyz.briancorbin.pk.hex.persistence.InMemoryPositionRepository;

/** SC-001 of 001-position-core: a trade updates the position; re-booking its tradeId does not. */
class PositionServiceTest {

  private final InMemoryPositionRepository repo = new InMemoryPositionRepository();
  private final PositionService service = new PositionService(repo);

  @Test
  void bookingATradeUpdatesThePosition() {
    service.book(new TradeEvent("t-1", "AAPL", 100));

    assertEquals(new Position("AAPL", 100), repo.find("AAPL").orElseThrow());
  }

  @Test
  void rebookingTheSameTradeIdLeavesThePositionUnchanged() {
    service.book(new TradeEvent("t-1", "AAPL", 100));
    Position again = service.book(new TradeEvent("t-1", "AAPL", 100));

    assertEquals(100, again.quantity());
    assertEquals(new Position("AAPL", 100), repo.find("AAPL").orElseThrow());
    assertTrue(repo.wasApplied("t-1"));
  }

  @Test
  void aSecondTradeIdNetsAgainstTheRunningPosition() {
    service.book(new TradeEvent("t-1", "AAPL", 100));
    service.book(new TradeEvent("t-2", "AAPL", -40));

    assertEquals(new Position("AAPL", 60), service.current("AAPL"));
    assertEquals(Position.flat("MSFT"), service.current("MSFT"));
  }
}
