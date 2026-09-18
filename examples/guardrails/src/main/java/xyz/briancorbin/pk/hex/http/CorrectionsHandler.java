package xyz.briancorbin.pk.hex.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xyz.briancorbin.pk.hex.app.PositionService;

/**
 * POST /corrections (007-reconciliation FR-005; api/corrections.openapi.yaml): the sanctioned door
 * for a downstream reconciliation service. The correction lands on {@link PositionService#correct}
 * — the nightly job's own path — so it emits PositionChanged through the adapter; this class holds
 * the use case, never a repository. Three flat fields, hand-parsed like the rest of the surface.
 */
public final class CorrectionsHandler implements HttpHandler {
  private static final Pattern INSTRUMENT =
      Pattern.compile("\"instrument\"\\s*:\\s*\"([^\"]{1,32})\"");
  private static final Pattern QUANTITY = Pattern.compile("\"quantity\"\\s*:\\s*(-?\\d{1,18})\\b");
  private static final Pattern TRADE_ID = Pattern.compile("\"tradeId\"\\s*:\\s*\"([^\"]{1,64})\"");

  private final PositionService positions;

  public CorrectionsHandler(PositionService positions) {
    this.positions = positions;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"POST".equals(exchange.getRequestMethod())) {
      exchange.getResponseHeaders().add("Allow", "POST");
      PositionsHandler.respond(exchange, 405, Json.error("method not allowed"));
      return;
    }
    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    Matcher instrument = INSTRUMENT.matcher(body);
    Matcher quantity = QUANTITY.matcher(body);
    Matcher tradeId = TRADE_ID.matcher(body);
    String missing =
        !instrument.find()
            ? "instrument"
            : !quantity.find() ? "quantity" : !tradeId.find() ? "tradeId" : null;
    if (missing != null) {
      PositionsHandler.respond(exchange, 400, Json.error("missing or malformed field: " + missing));
      return;
    }
    PositionsHandler.respond(
        exchange,
        200,
        Json.position(
            positions.correct(
                instrument.group(1), Long.parseLong(quantity.group(1)), tradeId.group(1))));
  }
}
