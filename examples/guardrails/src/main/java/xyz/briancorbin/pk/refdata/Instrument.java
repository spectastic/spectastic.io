package xyz.briancorbin.pk.refdata;

/**
 * Static reference attributes of an instrument (005-instrument-refdata). Nothing to do with
 * positions.
 */
public record Instrument(String symbol, String name, String currency, int lotSize) {
  public Instrument {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol must not be blank");
    }
    if (lotSize < 1) {
      throw new IllegalArgumentException("lotSize must be at least 1: " + lotSize);
    }
  }
}
