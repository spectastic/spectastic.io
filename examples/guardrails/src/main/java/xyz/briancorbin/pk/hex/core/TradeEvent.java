package xyz.briancorbin.pk.hex.core;

/**
 * A trade to apply: signed quantity for an instrument, keyed by a unique tradeId (spec 001,
 * FR-002).
 */
public record TradeEvent(String tradeId, String instrument, long quantity) {
  public TradeEvent {
    if (tradeId == null || tradeId.isBlank()) {
      throw new IllegalArgumentException("tradeId must not be blank");
    }
    if (instrument == null || instrument.isBlank()) {
      throw new IllegalArgumentException("instrument must not be blank");
    }
  }
}
