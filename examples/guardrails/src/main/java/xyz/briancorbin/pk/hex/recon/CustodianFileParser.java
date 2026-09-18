package xyz.briancorbin.pk.hex.recon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** Parses the custodian EOD file (007-reconciliation): CSV, header {@code instrument,quantity}. */
public final class CustodianFileParser {
  static final String HEADER = "instrument,quantity";

  private CustodianFileParser() {}

  public static List<CustodianRecord> parse(Path file) throws IOException {
    List<String> lines = Files.readAllLines(file);
    if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
      throw new IllegalArgumentException(file + ": expected header " + HEADER);
    }
    List<CustodianRecord> out = new ArrayList<>();
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      if (line.isBlank()) {
        continue;
      }
      String[] cols = line.split(",", -1);
      if (cols.length != 2) {
        throw new IllegalArgumentException(file + " line " + (i + 1) + ": expected 2 columns");
      }
      out.add(new CustodianRecord(cols[0].trim(), Long.parseLong(cols[1].trim())));
    }
    return out;
  }

  /** The run identifier: the first eight hex characters of the file's SHA-256. */
  public static String sha8(Path file) throws IOException {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
      return HexFormat.of().formatHex(digest, 0, 4);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
