package xyz.briancorbin.pk.hex.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import xyz.briancorbin.pk.hex.core.EventBus;
import xyz.briancorbin.pk.hex.core.PositionChanged;

/**
 * D-006 of 002-downstream-consumers: a synchronous in-memory bus. {@code emit} delivers to every
 * subscriber on the caller's thread before returning, so a committed position and its event never
 * diverge (spec NFR-001).
 */
public final class SyncEventBus implements EventBus {
  private final List<Consumer<PositionChanged>> subscribers = new ArrayList<>();

  @Override
  public void emit(PositionChanged event) {
    for (Consumer<PositionChanged> subscriber : subscribers) {
      subscriber.accept(event);
    }
  }

  @Override
  public void subscribe(Consumer<PositionChanged> subscriber) {
    subscribers.add(subscriber);
  }
}
