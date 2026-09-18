package xyz.briancorbin.pk.hex.core;

/** An instrument and its signed quantity — the running sum of applied trades (spec 001, FR-001). */
public record Position(String instrument, long quantity) {
  public Position {
    if (instrument == null || instrument.isBlank()) {
      throw new IllegalArgumentException("instrument must not be blank");
    }
  }

  /** The empty position for an instrument nothing has traded yet. */
  public static Position flat(String instrument) {
    return new Position(instrument, 0L);
  }
}
