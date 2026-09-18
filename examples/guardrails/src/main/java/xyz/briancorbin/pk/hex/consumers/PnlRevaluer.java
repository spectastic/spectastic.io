package xyz.briancorbin.pk.hex.consumers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import xyz.briancorbin.pk.hex.core.EventBus;
import xyz.briancorbin.pk.hex.core.PositionChanged;

/**
 * The P&amp;L desk's revaluer (002 FR-003), idempotent on tradeId (FR-005): a redelivered event is
 * recognised and skipped, so it never double-counts.
 */
public final class PnlRevaluer {
  private final Map<String, Long> marked = new HashMap<>();
  private final Set<String> seen = new HashSet<>();
  private int revaluations;

  public PnlRevaluer(EventBus bus) {
    bus.subscribe(this::on);
  }

  private void on(PositionChanged event) {
    if (!seen.add(event.tradeId())) {
      return;
    }
    marked.put(event.instrument(), event.quantity());
    revaluations++;
  }

  /** How many distinct position changes have been revalued. */
  public int revaluations() {
    return revaluations;
  }

  /** The quantity P&amp;L last marked for the instrument; zero when none. */
  public long marked(String instrument) {
    return marked.getOrDefault(instrument, 0L);
  }
}
