package xyz.briancorbin.pk.hex.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.briancorbin.pk.hex.app.PositionService;
import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.core.TradeEvent;
import xyz.briancorbin.pk.hex.events.SyncEventBus;

/** SC-001 of 003-jdbc-persistence: a booked trade is read back after a restart, emitted once. */
class JdbcPositionRepositoryAdapterTest {

  @Test
  void aBookedTradeSurvivesReopen(@TempDir Path dir) throws Exception {
    Path file = dir.resolve("pk");
    List<PositionChanged> emitted = new ArrayList<>();

    // First process: book through the use case over the JDBC adapter.
    SyncEventBus bus = new SyncEventBus();
    bus.subscribe(emitted::add);
    JdbcPositionRepositoryAdapter adapter =
        new JdbcPositionRepositoryAdapter(H2Database.open(file), bus);
    new PositionService(adapter).book(new TradeEvent("t-1", "AAPL", 100));
    assertEquals(List.of(new PositionChanged("AAPL", 100, "t-1")), emitted);

    // "Restart": a fresh opener and adapter over the same file.
    SyncEventBus bus2 = new SyncEventBus();
    bus2.subscribe(emitted::add);
    JdbcPositionRepositoryAdapter reopened =
        new JdbcPositionRepositoryAdapter(H2Database.open(file), bus2);
    assertEquals(new Position("AAPL", 100), reopened.find("AAPL").orElseThrow());
    assertTrue(reopened.wasApplied("t-1"));

    // Idempotent across the restart too: the applied record was durable.
    new PositionService(reopened).book(new TradeEvent("t-1", "AAPL", 100));
    assertEquals(1, emitted.size());
    assertEquals(new Position("AAPL", 100), reopened.find("AAPL").orElseThrow());

    // A second trade updates the existing row (UPDATE, not INSERT) and emits.
    new PositionService(reopened).book(new TradeEvent("t-2", "AAPL", -40));
    assertEquals(new Position("AAPL", 60), reopened.find("AAPL").orElseThrow());
    assertEquals(2, emitted.size());
  }
}
