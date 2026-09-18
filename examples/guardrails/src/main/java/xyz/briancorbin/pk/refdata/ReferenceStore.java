package xyz.briancorbin.pk.refdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The reference store (005-instrument-refdata): a map replaced wholesale on each feed load. */
public final class ReferenceStore {
  private Map<String, Instrument> bySymbol = Map.of();

  public void load(List<Instrument> feed) {
    Map<String, Instrument> next = new HashMap<>();
    for (Instrument i : feed) {
      next.put(i.symbol(), i);
    }
    bySymbol = Map.copyOf(next);
  }

  public Optional<Instrument> lookup(String symbol) {
    return Optional.ofNullable(bySymbol.get(symbol));
  }

  public int size() {
    return bySymbol.size();
  }
}
