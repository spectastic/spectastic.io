package xyz.briancorbin.pk.hex.persistence;

import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.PositionRepository;

/**
 * The reconciliation job's read side (007-reconciliation): current quantities through the existing
 * adapter, nothing else. In the persistence package because the design places it here, inside
 * 002-downstream-consumers/D-007's scope — and honouring it: this class adds no writer.
 */
public final class CustodianSnapshotReader {
  private final PositionRepository repository;

  public CustodianSnapshotReader(PositionRepository repository) {
    this.repository = repository;
  }

  /** The stored quantity, zero when nothing was ever booked. */
  public long quantityOf(String instrument) {
    return repository.find(instrument).map(Position::quantity).orElse(0L);
  }
}
