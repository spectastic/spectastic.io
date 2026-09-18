package xyz.briancorbin.pk.hex.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import xyz.briancorbin.pk.hex.core.StoreProbe;

/**
 * Readiness for the H2 store (006-health-check, D-001): one {@code SELECT 1} as the read-only
 * {@code pk_reader} role. Inside the persistence package (002-downstream-consumers/D-007's scope)
 * because it touches the data-access API — and, by its role's grant, it cannot write.
 */
public final class H2StoreProbe implements StoreProbe {
  private final H2Database db;

  public H2StoreProbe(H2Database db) {
    this.db = db;
  }

  @Override
  public boolean reachable() {
    try (Connection c = db.readerConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT 1")) {
      return rs.next();
    } catch (SQLException e) {
      return false;
    }
  }
}
