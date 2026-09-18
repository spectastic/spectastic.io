package xyz.briancorbin.pk.hex.core;

import java.util.Optional;

/**
 * The port the domain reaches its store through (spec 001, FR-003). The adapter behind it is the
 * single writer of the positions store (002-downstream-consumers/D-007); {@code write} records the
 * applied tradeId together with the new quantity so the two can never disagree (design D-005).
 */
public interface PositionRepository {
  Optional<Position> find(String instrument);

  boolean wasApplied(String tradeId);

  void write(String instrument, long quantity, String tradeId);
}
