package xyz.briancorbin.pk.recon;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * THE PRIYA PR — the bypass the guardrail exists to catch. A nightly
 * reconciliation that corrects positions by writing the positions table
 * DIRECTLY, bypassing the position use case and the PositionChanged emission.
 * It lives OUTSIDE xyz.briancorbin.pk.hex.persistence, so it violates ADR-0007
 * (002-downstream-consumers/D-007). Committed straight to a branch.
 */
public final class PositionReconciliationJob {
  private final Connection db;
  public PositionReconciliationJob(Connection db) { this.db = db; }

  public void reconcile(CustodianFile file) throws Exception {
    for (CustodianRecord record : file.records()) {
      try (PreparedStatement ps =
          db.prepareStatement("UPDATE positions SET quantity = ? WHERE instrument = ?")) {
        ps.setLong(1, record.quantity());
        ps.setString(2, record.instrument());
        ps.executeUpdate();
      }
    }
  }
}
