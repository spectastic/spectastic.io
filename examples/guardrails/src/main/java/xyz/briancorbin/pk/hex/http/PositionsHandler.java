package xyz.briancorbin.pk.hex.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import xyz.briancorbin.pk.hex.app.PositionService;

/**
 * GET /positions/{instrument} (004-query-endpoint; api/positions.openapi.yaml). Read-only by
 * construction: it holds the use case, not a repository, and routes nothing but GET (D-003).
 */
public final class PositionsHandler implements HttpHandler {
  static final String PREFIX = "/positions/";

  private final PositionService positions;

  public PositionsHandler(PositionService positions) {
    this.positions = positions;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"GET".equals(exchange.getRequestMethod())) {
      exchange.getResponseHeaders().add("Allow", "GET");
      respond(exchange, 405, Json.error("method not allowed"));
      return;
    }
    String path = exchange.getRequestURI().getPath();
    String instrument = path.startsWith(PREFIX) ? path.substring(PREFIX.length()) : "";
    if (instrument.isEmpty() || instrument.contains("/")) {
      respond(exchange, 404, Json.error("unknown instrument"));
      return;
    }
    respond(
        exchange,
        positions.lookup(instrument).isPresent() ? 200 : 404,
        positions
            .lookup(instrument)
            .map(Json::position)
            .orElseGet(() -> Json.error("unknown instrument")));
  }

  static void respond(HttpExchange exchange, int status, String json) throws IOException {
    byte[] body = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, body.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(body);
    }
  }
}
