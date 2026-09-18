package acme.recon.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class HealthEndpointTest {
  @Test
  void healthIsLive() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext("/health", new HealthHandler());
    server.start();
    try {
      HttpResponse<String> r =
          HttpClient.newHttpClient()
              .send(
                  HttpRequest.newBuilder(
                          URI.create("http://localhost:" + server.getAddress().getPort() + "/health"))
                      .GET()
                      .build(),
                  HttpResponse.BodyHandlers.ofString());
      assertEquals(200, r.statusCode());
      assertEquals("{\"live\":true}", r.body());
    } finally {
      server.stop(0);
    }
  }
}
