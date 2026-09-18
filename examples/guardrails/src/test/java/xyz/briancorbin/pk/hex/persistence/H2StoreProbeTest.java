package xyz.briancorbin.pk.hex.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The real probe (006 D-001): true against an opened file, false when H2 cannot get there; writes
 * nothing.
 */
class H2StoreProbeTest {

  @Test
  void anOpenedStoreIsReachableAndTheProbeWritesNothing(@TempDir Path dir) throws Exception {
    H2Database db = H2Database.open(dir.resolve("pk"));

    assertTrue(new H2StoreProbe(db).reachable());

    try (Connection reader = db.readerConnection();
        Statement s = reader.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM positions")) {
      rs.next();
      assertTrue(rs.getLong(1) == 0, "the probe must not write");
    }
  }

  @Test
  void aStoreThatCannotBeOpenedIsNotReachable(@TempDir Path dir) throws Exception {
    Path notADirectory = dir.resolve("blocker");
    Files.writeString(notADirectory, "a regular file where H2 would need a directory");

    assertFalse(new H2StoreProbe(H2Database.at(notADirectory.resolve("pk"))).reachable());
  }
}
