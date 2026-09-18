package xyz.briancorbin.pk.hex.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import xyz.briancorbin.pk.hex.core.TradeEvent;
import xyz.briancorbin.pk.hex.events.SyncEventBus;
import xyz.briancorbin.pk.hex.persistence.InMemoryPositionRepository;

/**
 * SC-001 of 004-query-endpoint: a GET returns the current quantity, and no request path mutates a
 * position — observed as zero PositionChanged events and an unchanged store across every request.
 */
class PositionsEndpointTest {

  private final SyncEventBus bus = new SyncEventBus();
  private final InMemoryPositionRepository repo = new InMemoryPositionRepository(bus);
  private final PositionService service = new PositionService(repo);
  private final List<PositionChanged> emitted = new ArrayList<>();
  private final HttpClient client = HttpClient.newHttpClient();
  private HttpApi api;

  @BeforeEach
  void start() throws Exception {
    bus.subscribe(emitted::add);
    service.book(new TradeEvent("t-1", "AAPL", 100));
    api = new HttpApi("localhost", 0);
    api.handle("/positions/", new PositionsHandler(service));
    api.start();
  }

  @AfterEach
  void stop() {
    api.stop();
  }

  @Test
  void aKnownInstrumentReturnsItsQuantity() throws Exception {
    HttpResponse<String> r = get("/positions/AAPL");

    assertEquals(200, r.statusCode());
    assertEquals("application/json", r.headers().firstValue("Content-Type").orElseThrow());
    assertEquals("{\"instrument\":\"AAPL\",\"quantity\":100}", r.body());
  }

  @Test
  void anUnknownInstrumentIs404() throws Exception {
    HttpResponse<String> r = get("/positions/MSFT");

    assertEquals(404, r.statusCode());
    assertEquals("{\"error\":\"unknown instrument\"}", r.body());
  }

  @Test
  void anythingButGetIs405() throws Exception {
    HttpResponse<String> r =
        client.send(
            HttpRequest.newBuilder(uri("/positions/AAPL"))
                .PUT(HttpRequest.BodyPublishers.ofString("{\"quantity\":0}"))
                .build(),
            HttpResponse.BodyHandlers.ofString());

    assertEquals(405, r.statusCode());
    assertEquals("GET", r.headers().firstValue("Allow").orElseThrow());
  }

  @Test
  void noRequestMutatesAPosition() throws Exception {
    get("/positions/AAPL");
    get("/positions/MSFT");
    get("/positions/");
    client.send(
        HttpRequest.newBuilder(uri("/positions/AAPL"))
            .method("DELETE", HttpRequest.BodyPublishers.noBody())
            .build(),
        HttpResponse.BodyHandlers.ofString());

    assertEquals(List.of(new PositionChanged("AAPL", 100, "t-1")), emitted);
    assertEquals(new Position("AAPL", 100), repo.find("AAPL").orElseThrow());
    assertEquals(true, repo.find("MSFT").isEmpty());
  }

  private HttpResponse<String> get(String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
  }

  private URI uri(String path) {
    return URI.create("http://localhost:" + api.port() + path);
  }
}
