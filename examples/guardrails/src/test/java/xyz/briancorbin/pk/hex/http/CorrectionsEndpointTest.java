package xyz.briancorbin.pk.hex.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.briancorbin.pk.hex.app.PositionService;
import xyz.briancorbin.pk.hex.core.Position;
import xyz.briancorbin.pk.hex.core.PositionChanged;
import xyz.briancorbin.pk.hex.events.SyncEventBus;
import xyz.briancorbin.pk.hex.persistence.InMemoryPositionRepository;

/**
 * FR-005 of 007-reconciliation (change 2026-09-18-http-corrections): a correction over POST
 * /corrections goes through the use case — one PositionChanged — and a repeated tradeId changes
 * nothing.
 */
class CorrectionsEndpointTest {

  private final SyncEventBus bus = new SyncEventBus();
  private final InMemoryPositionRepository repo = new InMemoryPositionRepository(bus);
  private final PositionService positions = new PositionService(repo);
  private final List<PositionChanged> emitted = new ArrayList<>();
  private final HttpClient client = HttpClient.newHttpClient();
  private HttpApi api;

  @BeforeEach
  void start() throws Exception {
    bus.subscribe(emitted::add);
    api = new HttpApi("localhost", 0).handle("/corrections", new CorrectionsHandler(positions));
    api.start();
  }

  @AfterEach
  void stop() {
    api.stop();
  }

  @Test
  void aCorrectionIsAppliedThroughTheUseCaseAndEmitsOnce() throws Exception {
    HttpResponse<String> r =
        post("{\"instrument\":\"AAPL\",\"quantity\":100,\"tradeId\":\"recon:c0ffee00:AAPL\"}");

    assertEquals(200, r.statusCode());
    assertEquals("{\"instrument\":\"AAPL\",\"quantity\":100}", r.body());
    assertEquals(List.of(new PositionChanged("AAPL", 100, "recon:c0ffee00:AAPL")), emitted);
    assertEquals(new Position("AAPL", 100), repo.find("AAPL").orElseThrow());
  }

  @Test
  void aRepeatedTradeIdChangesNothing() throws Exception {
    String body = "{\"instrument\":\"AAPL\",\"quantity\":100,\"tradeId\":\"recon:c0ffee00:AAPL\"}";
    post(body);

    HttpResponse<String> again = post(body);

    assertEquals(200, again.statusCode());
    assertEquals("{\"instrument\":\"AAPL\",\"quantity\":100}", again.body());
    assertEquals(1, emitted.size());
    assertTrue(repo.wasApplied("recon:c0ffee00:AAPL"));
  }

  @Test
  void aBodyThatIsNotACorrectionIs400() throws Exception {
    HttpResponse<String> r = post("{\"instrument\":\"AAPL\"}");

    assertEquals(400, r.statusCode());
    assertTrue(r.body().contains("quantity"), r.body());
    assertTrue(emitted.isEmpty());
  }

  @Test
  void anythingButPostIs405() throws Exception {
    HttpResponse<String> r =
        client.send(
            HttpRequest.newBuilder(uri()).GET().build(), HttpResponse.BodyHandlers.ofString());

    assertEquals(405, r.statusCode());
    assertEquals("POST", r.headers().firstValue("Allow").orElseThrow());
  }

  private HttpResponse<String> post(String json) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private URI uri() {
    return URI.create("http://localhost:" + api.port() + "/corrections");
  }
}
