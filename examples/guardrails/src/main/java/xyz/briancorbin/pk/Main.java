package xyz.briancorbin.pk;

import java.io.IOException;
import java.nio.file.Path;
import xyz.briancorbin.pk.hex.app.PositionService;
import xyz.briancorbin.pk.hex.consumers.PnlRevaluer;
import xyz.briancorbin.pk.hex.consumers.RiskExposureView;
import xyz.briancorbin.pk.hex.events.SyncEventBus;
import xyz.briancorbin.pk.hex.http.CorrectionsHandler;
import xyz.briancorbin.pk.hex.http.HealthHandler;
import xyz.briancorbin.pk.hex.http.HttpApi;
import xyz.briancorbin.pk.hex.http.PositionsHandler;
import xyz.briancorbin.pk.hex.persistence.CustodianSnapshotReader;
import xyz.briancorbin.pk.hex.persistence.H2Database;
import xyz.briancorbin.pk.hex.persistence.H2StoreProbe;
import xyz.briancorbin.pk.hex.persistence.JdbcPositionRepositoryAdapter;
import xyz.briancorbin.pk.hex.recon.ReconciliationJob;
import xyz.briancorbin.pk.refdata.ReferenceFeedLoader;
import xyz.briancorbin.pk.refdata.ReferenceStore;

/**
 * Wiring only (004-query-endpoint T-112): the store, the bus, the adapter, the consumers, the API.
 */
public final class Main {
  private Main() {}

  public static void main(String[] args) throws IOException {
    int port = Integer.parseInt(env("PK_PORT", "8080"));
    Path dbFile = Path.of(env("PK_DB", "data/pk"));

    H2Database db = H2Database.open(dbFile);
    SyncEventBus bus = new SyncEventBus();
    JdbcPositionRepositoryAdapter repository = new JdbcPositionRepositoryAdapter(db, bus);
    RiskExposureView risk = new RiskExposureView(bus);
    PnlRevaluer pnl = new PnlRevaluer(bus);
    bus.subscribe(
        e ->
            System.out.printf(
                "PositionChanged %s %d %s · risk=%d pnl-revaluations=%d%n",
                e.instrument(),
                e.quantity(),
                e.tradeId(),
                risk.exposure(e.instrument()),
                pnl.revaluations()));
    PositionService positions = new PositionService(repository);

    if (args.length == 2 && "reconcile".equals(args[0])) {
      ReconciliationJob.Report report =
          new ReconciliationJob(new CustodianSnapshotReader(repository), positions)
              .run(Path.of(args[1]));
      System.out.printf(
          "reconcile run %s: %d correction(s)%n", report.runId(), report.corrections().size());
      return;
    }

    ReferenceStore refdata = new ReferenceStore();
    refdata.load(ReferenceFeedLoader.loadBundled());

    HttpApi api =
        new HttpApi("localhost", port)
            .handle("/positions/", new PositionsHandler(positions))
            .handle("/health", new HealthHandler(new H2StoreProbe(db)))
            .handle("/corrections", new CorrectionsHandler(positions));
    api.start();
    System.out.printf(
        "position-keeper listening on http://localhost:%d (db %s, %d instruments)%n",
        api.port(), dbFile, refdata.size());
  }

  private static String env(String name, String fallback) {
    String v = System.getenv(name);
    return v == null || v.isBlank() ? fallback : v;
  }
}
