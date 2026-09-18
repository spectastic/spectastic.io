package xyz.briancorbin.pk.hex.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * The inbound HTTP adapter (004-query-endpoint, D-001): the JDK's own server, one context per
 * handler. Port 0 binds an ephemeral port that {@link #port()} reports — the tests use it.
 */
public final class HttpApi {
  private final HttpServer server;

  public HttpApi(String host, int port) throws IOException {
    this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
  }

  public HttpApi handle(String path, HttpHandler handler) {
    server.createContext(path, handler);
    return this;
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  public int port() {
    return server.getAddress().getPort();
  }
}
