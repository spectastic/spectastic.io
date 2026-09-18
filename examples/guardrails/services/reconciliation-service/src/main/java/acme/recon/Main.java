package acme.recon;

import acme.recon.client.PositionKeeperClient;
import acme.recon.http.HealthHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * acme/reconciliation-service: a health endpoint on 8081 and, with `correct <instrument> <quantity>
 * <tradeId>`, one correction routed through the owner.
 */
public final class Main {
  private Main() {}

  public static void main(String[] args) throws IOException, InterruptedException {
    String owner = env("PK_URL", "http://localhost:8080");
    if (args.length == 4 && "correct".equals(args[0])) {
      String answer =
          new PositionKeeperClient(owner).correct(args[1], Long.parseLong(args[2]), args[3]);
      System.out.println("position keeper answered " + answer);
      return;
    }
    int port = Integer.parseInt(env("RECON_PORT", "8081"));
    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
    server.createContext("/health", new HealthHandler());
    server.start();
    System.out.printf(
        "reconciliation-service listening on http://localhost:%d (owner %s)%n",
        server.getAddress().getPort(), owner);
  }

  private static String env(String name, String fallback) {
    String v = System.getenv(name);
    return v == null || v.isBlank() ? fallback : v;
  }
}
