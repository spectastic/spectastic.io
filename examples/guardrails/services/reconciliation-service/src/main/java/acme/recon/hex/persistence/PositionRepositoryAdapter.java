package acme.recon.hex.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * THE CROSS-SERVICE PRIYA PR — the reconciliation service reaching into the
 * position-keeper's store. This is a textbook persistence adapter: inside the
 * consumer's own src/.../persistence/... package, cleanly layered, ports-and-
 * adapters by the book. A path rule would call it sanctioned. It is still a
 * violation, because acme/reconciliation-service does not OWN
 * spectastic://briancorbin/position-keeper-guardrails/datastore/positions —
 * the defect is ownership, not layering.
 */
public final class PositionRepositoryAdapter {
  private final Connection db;
  public PositionRepositoryAdapter(Connection db) { this.db = db; }

  public void correct(String instrument, long quantity) throws Exception {
    try (PreparedStatement ps =
        db.prepareStatement("UPDATE positions SET quantity = ? WHERE instrument = ?")) {
      ps.setLong(1, quantity);
      ps.setString(2, instrument);
      ps.executeUpdate();
    }
  }
}
