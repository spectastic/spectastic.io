package xyz.briancorbin.pk.hex.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.core.PositionRepository;

/**
 * The sanctioned single writer of the positions store (ADR-0007's subject).
 * Inside xyz.briancorbin.pk.hex.persistence — the scope ADR-0007 permits. It
 * writes the positions table AND emits PositionChanged in the same transaction.
 * The verdict does NOT flag its "UPDATE positions" — it is within the allowed path.
 */
public final class JdbcPositionRepositoryAdapter implements PositionRepository {
  private final Connection db;
  private final EventBus bus;
  public JdbcPositionRepositoryAdapter(Connection db, EventBus bus) { this.db = db; this.bus = bus; }

  @Override
  public void write(String instrument, long quantity, String tradeId) throws Exception {
    try (PreparedStatement ps =
        db.prepareStatement("UPDATE positions SET quantity = ? WHERE instrument = ?")) {
      ps.setLong(1, quantity);
      ps.setString(2, instrument);
      ps.executeUpdate();
    }
    bus.emit(new PositionChanged(instrument, quantity, tradeId));
  }
}
