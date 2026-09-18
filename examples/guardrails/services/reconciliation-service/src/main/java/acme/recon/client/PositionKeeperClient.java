package acme.recon.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * The sanctioned way to correct a position: route it through the owner
 * (briancorbin/position-keeper-guardrails) over its POST /corrections door
 * (api/corrections.openapi.yaml in the owner's repo), so the owner's adapter writes and emits. This
 * is what demo 8's explanation asks for instead of PositionRepositoryAdapter's direct write.
 */
public final class PositionKeeperClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final URI corrections;

  public PositionKeeperClient(String positionKeeperBaseUrl) {
    this.corrections = URI.create(positionKeeperBaseUrl + "/corrections");
  }

  /** One correction; returns the owner's response body ({instrument, quantity}). */
  public String correct(String instrument, long quantity, String tradeId)
      throws IOException, InterruptedException {
    String body =
        "{\"instrument\":\""
            + instrument
            + "\",\"quantity\":"
            + quantity
            + ",\"tradeId\":\""
            + tradeId
            + "\"}";
    HttpResponse<String> r =
        http.send(
            HttpRequest.newBuilder(corrections)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    if (r.statusCode() != 200) {
      throw new IOException("position keeper answered " + r.statusCode() + ": " + r.body());
    }
    return r.body();
  }
}
