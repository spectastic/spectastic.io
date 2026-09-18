package xyz.briancorbin.pk.hex.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * D-008 of 002-downstream-consumers, pinned: the grant in {@code db/grants.sql} refuses a write to
 * positions from any role but the adapter's. Not a static check — the merge verdict cannot see it,
 * which is why D-008 is recorded as a none-with-reason — but the build proves the backstop exists.
 */
class GrantBackstopTest {

  private static final String ADMIN = "jdbc:h2:mem:grants;DB_CLOSE_DELAY=-1";
  private static final String ROLE = "jdbc:h2:mem:grants";

  @BeforeEach
  void applySchemaAndGrants() throws Exception {
    try (Connection admin = DriverManager.getConnection(ADMIN, "sa", "");
        Statement s = admin.createStatement()) {
      s.execute("DROP ALL OBJECTS");
      s.execute(Files.readString(Path.of("db/schema.sql")));
      s.execute(Files.readString(Path.of("db/grants.sql")));
      s.execute("INSERT INTO positions VALUES ('AAPL', 100)");
    }
  }

  @Test
  void aNonAdapterRoleCannotUpdatePositions() throws Exception {
    try (Connection reader = DriverManager.getConnection(ROLE, "pk_reader", "pk_reader");
        Statement s = reader.createStatement()) {
      SQLException refused =
          assertThrows(
              SQLException.class,
              () -> s.executeUpdate("UPDATE positions SET quantity = 1 WHERE instrument = 'AAPL'"));
      assertEquals("90096", refused.getSQLState(), refused.getMessage());
    }
  }

  @Test
  void theAdapterRoleCanUpdatePositions() throws Exception {
    try (Connection adapter = DriverManager.getConnection(ROLE, "pk_adapter", "pk_adapter");
        Statement s = adapter.createStatement()) {
      assertEquals(
          1, s.executeUpdate("UPDATE positions SET quantity = 1 WHERE instrument = 'AAPL'"));
    }
  }
}
