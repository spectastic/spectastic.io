package xyz.briancorbin.pk.hex.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import xyz.briancorbin.pk.hex.core.EventBus;
import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.core.PositionRepository;

/**
 * The sanctioned single writer of the positions store (ADR-0007's subject). Inside
 * xyz.briancorbin.pk.hex.persistence — the scope ADR-0007 permits. It writes the positions table
 * AND emits PositionChanged on the same call, after the transaction commits (003 D-002). The
 * verdict does NOT flag its "UPDATE positions" — it is within the allowed path. Connects as the
 * {@code pk_adapter} role, the only one the grant (D-008) lets write.
 */
public final class JdbcPositionRepositoryAdapter implements PositionRepository {
  private final H2Database db;
  private final EventBus bus;

  public JdbcPositionRepositoryAdapter(H2Database db, EventBus bus) {
    this.db = db;
    this.bus = bus;
  }

  @Override
  public Optional<Position> find(String instrument) {
    try (Connection c = db.adapterConnection();
        PreparedStatement ps =
            c.prepareStatement("SELECT quantity FROM positions WHERE instrument = ?")) {
      ps.setString(1, instrument);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(new Position(instrument, rs.getLong(1))) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new PersistenceException("find " + instrument, e);
    }
  }

  @Override
  public boolean wasApplied(String tradeId) {
    try (Connection c = db.adapterConnection();
        PreparedStatement ps =
            c.prepareStatement("SELECT 1 FROM applied_trades WHERE trade_id = ?")) {
      ps.setString(1, tradeId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new PersistenceException("wasApplied " + tradeId, e);
    }
  }

  @Override
  public void write(String instrument, long quantity, String tradeId) {
    try (Connection c = db.adapterConnection()) {
      c.setAutoCommit(false);
      try {
        int rows;
        try (PreparedStatement ps =
            c.prepareStatement("UPDATE positions SET quantity = ? WHERE instrument = ?")) {
          ps.setLong(1, quantity);
          ps.setString(2, instrument);
          rows = ps.executeUpdate();
        }
        if (rows == 0) {
          try (PreparedStatement ps =
              c.prepareStatement("INSERT INTO positions (instrument, quantity) VALUES (?, ?)")) {
            ps.setString(1, instrument);
            ps.setLong(2, quantity);
            ps.executeUpdate();
          }
        }
        try (PreparedStatement ps =
            c.prepareStatement("INSERT INTO applied_trades (trade_id, instrument) VALUES (?, ?)")) {
          ps.setString(1, tradeId);
          ps.setString(2, instrument);
          ps.executeUpdate();
        }
        c.commit();
      } catch (SQLException e) {
        c.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new PersistenceException("write " + instrument + " " + tradeId, e);
    }
    bus.emit(new PositionChanged(instrument, quantity, tradeId));
  }
}
