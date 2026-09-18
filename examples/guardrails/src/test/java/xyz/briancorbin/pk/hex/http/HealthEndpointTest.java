package xyz.briancorbin.pk.hex.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** SC-001 of 006-health-check: 200 when the store is reachable, 503 when it is not. */
class HealthEndpointTest {

  private final AtomicBoolean storeUp = new AtomicBoolean(true);
  private final HttpClient client = HttpClient.newHttpClient();
  private HttpApi api;

  @BeforeEach
  void start() throws Exception {
    api = new HttpApi("localhost", 0).handle("/health", new HealthHandler(storeUp::get));
    api.start();
  }

  @AfterEach
  void stop() {
    api.stop();
  }

  @Test
  void aReachableStoreIsReady() throws Exception {
    HttpResponse<String> r = get();

    assertEquals(200, r.statusCode());
    assertEquals("{\"live\":true,\"ready\":true}", r.body());
  }

  @Test
  void anUnreachableStoreIs503ButStillLive() throws Exception {
    storeUp.set(false);

    HttpResponse<String> r = get();

    assertEquals(503, r.statusCode());
    assertEquals("{\"live\":true,\"ready\":false}", r.body());
  }

  @Test
  void anythingButGetIs405() throws Exception {
    HttpResponse<String> r =
        client.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + api.port() + "/health"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());

    assertEquals(405, r.statusCode());
  }

  private HttpResponse<String> get() throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + api.port() + "/health"))
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
