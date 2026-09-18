package xyz.briancorbin.pk.hex.http;

import xyz.briancorbin.pk.hex.core.Position;

/** The two JSON shapes the HTTP surface speaks (api/positions.openapi.yaml); no library needed. */
public final class Json {
  private Json() {}

  public static String position(Position p) {
    return "{\"instrument\":" + string(p.instrument()) + ",\"quantity\":" + p.quantity() + "}";
  }

  public static String error(String message) {
    return "{\"error\":" + string(message) + "}";
  }

  static String string(String s) {
    StringBuilder out = new StringBuilder("\"");
    for (char c : s.toCharArray()) {
      switch (c) {
        case '"' -> out.append("\\\"");
        case '\\' -> out.append("\\\\");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        default -> {
          if (c < 0x20) {
            out.append(String.format("\\u%04x", (int) c));
          } else {
            out.append(c);
          }
        }
      }
    }
    return out.append('"').toString();
  }
}
