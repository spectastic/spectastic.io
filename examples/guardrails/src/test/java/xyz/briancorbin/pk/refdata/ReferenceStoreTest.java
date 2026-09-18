package xyz.briancorbin.pk.refdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

/**
 * SC-001 of 005-instrument-refdata: after a feed load, a lookup returns name, currency and lot
 * size.
 */
class ReferenceStoreTest {

  private static final String FEED =
      """
      symbol,name,currency,lotSize
      AAPL,Apple Inc.,USD,1
      MSFT,Microsoft Corporation,USD,1
      7203.T,Toyota Motor Corporation,JPY,100
      """;

  private final ReferenceStore store = new ReferenceStore();

  @Test
  void aLoadedInstrumentIsLookedUpByItsAttributes() {
    store.load(ReferenceFeedLoader.load(new StringReader(FEED)));

    assertEquals(
        new Instrument("7203.T", "Toyota Motor Corporation", "JPY", 100),
        store.lookup("7203.T").orElseThrow());
    assertEquals("Apple Inc.", store.lookup("AAPL").orElseThrow().name());
    assertTrue(store.lookup("NOPE").isEmpty());
    assertEquals(3, store.size());
  }

  @Test
  void aRefreshReplacesTheWholeFeed() {
    store.load(ReferenceFeedLoader.load(new StringReader(FEED)));
    store.load(
        ReferenceFeedLoader.load(
            new StringReader("symbol,name,currency,lotSize\nAAPL,Apple,USD,1\n")));

    assertEquals("Apple", store.lookup("AAPL").orElseThrow().name());
    assertTrue(store.lookup("MSFT").isEmpty());
    assertEquals(1, store.size());
  }

  @Test
  void theBundledFeedLoadsFromTheClasspath() {
    store.load(ReferenceFeedLoader.loadBundled());

    assertTrue(store.size() >= 1);
    assertEquals("GBP", store.lookup("VOD.L").orElseThrow().currency());
  }

  @Test
  void aMalformedRowNamesItsLine() {
    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                ReferenceFeedLoader.load(
                    new StringReader("symbol,name,currency,lotSize\nAAPL,Apple Inc.,USD\n")));

    assertTrue(e.getMessage().startsWith("line 2:"), e.getMessage());
  }
}
