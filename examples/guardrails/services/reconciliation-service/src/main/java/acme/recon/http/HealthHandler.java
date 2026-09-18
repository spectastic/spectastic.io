package acme.recon.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** GET /health for the reconciliation service: {"live":true}. */
public final class HealthHandler implements HttpHandler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    byte[] body = "{\"live\":true}".getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(
        "GET".equals(exchange.getRequestMethod()) ? 200 : 405, body.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(body);
    }
  }
}
