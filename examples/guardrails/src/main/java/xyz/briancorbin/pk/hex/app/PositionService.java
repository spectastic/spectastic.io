package xyz.briancorbin.pk.hex.app;

import java.util.Optional;
import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.PositionRepository;
import xyz.briancorbin.pk.hex.core.TradeEvent;

/**
 * The position use case (001-position-core). Idempotency on tradeId is decided here, against the
 * repository's applied-tradeId record, so it holds for every adapter (design D-005).
 */
public final class PositionService {
  private final PositionRepository repository;

  public PositionService(PositionRepository repository) {
    this.repository = repository;
  }

  /** Apply a trade; a tradeId already applied leaves the position unchanged (FR-001, FR-002). */
  public Position book(TradeEvent trade) {
    if (repository.wasApplied(trade.tradeId())) {
      return current(trade.instrument());
    }
    long quantity = current(trade.instrument()).quantity() + trade.quantity();
    repository.write(trade.instrument(), quantity, trade.tradeId());
    return new Position(trade.instrument(), quantity);
  }

  /** The current position, flat when nothing has traded. */
  public Position current(String instrument) {
    return lookup(instrument).orElse(Position.flat(instrument));
  }

  /** The position if anything was ever booked for the instrument (004: the 404 distinction). */
  public Optional<Position> lookup(String instrument) {
    return repository.find(instrument);
  }

  /**
   * Bring the instrument to {@code target} by booking the delta under the supplied tradeId (007: a
   * reconciliation run). Zero delta books nothing — and records nothing, so a tradeId whose
   * correction was a no-op is not remembered; a repeated tradeId that did book is idempotent like
   * any other.
   */
  public Position correct(String instrument, long target, String tradeId) {
    long delta = target - current(instrument).quantity();
    if (delta == 0) {
      return current(instrument);
    }
    return book(new TradeEvent(tradeId, instrument, delta));
  }
}
