package xyz.briancorbin.pk.hex.consumers;

import java.util.HashMap;
import java.util.Map;
import xyz.briancorbin.pk.hex.core.EventBus;
import xyz.briancorbin.pk.hex.core.PositionChanged;

/**
 * The Risk desk's view: the latest quantity per instrument, fed by PositionChanged (002 FR-002).
 */
public final class RiskExposureView {
  private final Map<String, Long> exposure = new HashMap<>();

  public RiskExposureView(EventBus bus) {
    bus.subscribe(this::on);
  }

  private void on(PositionChanged event) {
    exposure.put(event.instrument(), event.quantity());
  }

  /** The exposure Risk currently holds for the instrument; zero when no change has been seen. */
  public long exposure(String instrument) {
    return exposure.getOrDefault(instrument, 0L);
  }
}
