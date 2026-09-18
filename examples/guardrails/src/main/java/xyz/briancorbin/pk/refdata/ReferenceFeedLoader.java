package xyz.briancorbin.pk.refdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the reference feed (005-instrument-refdata, D-002): CSV with the header {@code
 * symbol,name,currency,lotSize}, no quoting. A malformed row fails the whole load, naming its line,
 * so a store is never half-refreshed.
 */
public final class ReferenceFeedLoader {
  static final String HEADER = "symbol,name,currency,lotSize";
  static final String BUNDLED = "/refdata/instruments.csv";

  private ReferenceFeedLoader() {}

  /** The feed bundled with the build. */
  public static List<Instrument> loadBundled() {
    InputStream in = ReferenceFeedLoader.class.getResourceAsStream(BUNDLED);
    if (in == null) {
      throw new IllegalStateException("bundled feed missing: " + BUNDLED);
    }
    return load(new InputStreamReader(in, StandardCharsets.UTF_8));
  }

  public static List<Instrument> load(Reader reader) {
    try (BufferedReader lines = new BufferedReader(reader)) {
      String header = lines.readLine();
      if (!HEADER.equals(header)) {
        throw new IllegalArgumentException(
            "line 1: expected header " + HEADER + " but was " + header);
      }
      List<Instrument> out = new ArrayList<>();
      String line;
      for (int n = 2; (line = lines.readLine()) != null; n++) {
        if (line.isBlank()) {
          continue;
        }
        String[] cols = line.split(",", -1);
        if (cols.length != 4) {
          throw new IllegalArgumentException(
              "line " + n + ": expected 4 columns, got " + cols.length);
        }
        try {
          out.add(new Instrument(cols[0], cols[1], cols[2], Integer.parseInt(cols[3].trim())));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("line " + n + ": " + e.getMessage(), e);
        }
      }
      return out;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
