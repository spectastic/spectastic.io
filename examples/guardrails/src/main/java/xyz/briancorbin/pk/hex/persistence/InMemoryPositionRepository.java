package xyz.briancorbin.pk.hex.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import xyz.briancorbin.pk.hex.core.EventBus;
import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.core.PositionRepository;
import xyz.briancorbin.pk.hex.events.SyncEventBus;

/**
 * The first adapter (001-position-core): a map of quantities and a set of applied tradeIds. Lives
 * in the persistence package — the scope 002-downstream-consumers/D-007 permits to touch the store
 * — and, per that decision, is the single emitter of {@link PositionChanged}: every write emits, on
 * the same call (002 FR-001, FR-004, NFR-001).
 */
public final class InMemoryPositionRepository implements PositionRepository {
  private final Map<String, Long> quantities = new HashMap<>();
  private final Set<String> applied = new HashSet<>();
  private final EventBus bus;

  public InMemoryPositionRepository(EventBus bus) {
    this.bus = bus;
  }

  /** A repository nobody listens to — a private bus. */
  public InMemoryPositionRepository() {
    this(new SyncEventBus());
  }

  @Override
  public Optional<Position> find(String instrument) {
    Long quantity = quantities.get(instrument);
    return quantity == null ? Optional.empty() : Optional.of(new Position(instrument, quantity));
  }

  @Override
  public boolean wasApplied(String tradeId) {
    return applied.contains(tradeId);
  }

  @Override
  public void write(String instrument, long quantity, String tradeId) {
    quantities.put(instrument, quantity);
    applied.add(tradeId);
    bus.emit(new PositionChanged(instrument, quantity, tradeId));
  }
}
