package xyz.briancorbin.pk.hex.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import xyz.briancorbin.pk.hex.core.StoreProbe;

/**
 * GET /health (006-health-check; api/health.openapi.yaml): live always, ready when the probe says
 * so.
 */
public final class HealthHandler implements HttpHandler {
  private final StoreProbe store;

  public HealthHandler(StoreProbe store) {
    this.store = store;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"GET".equals(exchange.getRequestMethod())) {
      exchange.getResponseHeaders().add("Allow", "GET");
      PositionsHandler.respond(exchange, 405, Json.error("method not allowed"));
      return;
    }
    boolean ready = store.reachable();
    PositionsHandler.respond(
        exchange, ready ? 200 : 503, "{\"live\":true,\"ready\":" + ready + "}");
  }
}
