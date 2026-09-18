package xyz.briancorbin.pk.hex.core;

import java.util.function.Consumer;

/**
 * The outbound port for position events (spec 002). The persistence adapter is its single emitter
 * (002-downstream-consumers/D-007); consumers subscribe.
 */
public interface EventBus {
  void emit(PositionChanged event);

  void subscribe(Consumer<PositionChanged> subscriber);
}
