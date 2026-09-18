package xyz.briancorbin.pk.hex.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The embedded store (003-jdbc-persistence, D-001/D-003). {@code open} applies {@code
 * db/schema.sql} and {@code db/grants.sql} as the admin — both idempotent — then hands out
 * connections as the two roles the grant defines. The adapter writes as {@code pk_adapter};
 * everything else reads as {@code pk_reader}, so 002-downstream-consumers/D-008's backstop is live
 * on every write.
 */
public final class H2Database {
  private static final Path SCHEMA = Path.of("db/schema.sql");
  private static final Path GRANTS = Path.of("db/grants.sql");

  private final String url;

  private H2Database(String url) {
    this.url = url;
  }

  /**
   * Open (creating if absent) the file database at {@code file}, with schema and grants applied.
   */
  public static H2Database open(Path file) throws IOException {
    H2Database db = new H2Database("jdbc:h2:file:" + file.toAbsolutePath());
    try (Connection admin = DriverManager.getConnection(db.url, "sa", "");
        Statement s = admin.createStatement()) {
      s.execute(Files.readString(SCHEMA));
      s.execute(Files.readString(GRANTS));
    } catch (SQLException e) {
      throw new PersistenceException("open " + file, e);
    }
    return db;
  }

  /** A handle for a file that may not exist or be openable — schema and grants are not applied. */
  public static H2Database at(Path file) {
    return new H2Database("jdbc:h2:file:" + file.toAbsolutePath());
  }

  public String url() {
    return url;
  }

  /** A connection as the only role allowed to write positions. */
  public Connection adapterConnection() throws SQLException {
    return DriverManager.getConnection(url, "pk_adapter", "pk_adapter");
  }

  /** A read-only role for queries and probes. */
  public Connection readerConnection() throws SQLException {
    return DriverManager.getConnection(url, "pk_reader", "pk_reader");
  }
}
