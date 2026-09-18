package acme.recon.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The client posts the owner's contract shape to /corrections and returns the answer. */
class PositionKeeperClientTest {
  @Test
  void aCorrectionIsPostedToTheOwnersDoor() throws Exception {
    List<String> received = new ArrayList<>();
    HttpServer stubOwner = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    stubOwner.createContext(
        "/corrections",
        exchange -> {
          received.add(
              exchange.getRequestMethod()
                  + " "
                  + new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          byte[] body =
              "{\"instrument\":\"AAPL\",\"quantity\":100}".getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    stubOwner.start();
    try {
      String answer =
          new PositionKeeperClient("http://localhost:" + stubOwner.getAddress().getPort())
              .correct("AAPL", 100, "recon:c0ffee00:AAPL");

      assertEquals("{\"instrument\":\"AAPL\",\"quantity\":100}", answer);
      assertEquals(
          List.of(
              "POST {\"instrument\":\"AAPL\",\"quantity\":100,\"tradeId\":\"recon:c0ffee00:AAPL\"}"),
          received);
    } finally {
      stubOwner.stop(0);
    }
  }
}
