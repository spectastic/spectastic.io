# 8-consumer-service → `services/reconciliation-service/`

The non-owner project for demonstration 8 (`acme/reconciliation-service`) is now a real Gradle subproject at
[`services/reconciliation-service/`](../services/reconciliation-service/): its own `spectastic.json`, its verbatim copy of
D-007, the adapter that writes the owner's store from a textbook persistence package (bytes unchanged, still the
subject of demo 8), plus a `Main` on port 8081, a health endpoint and a `PositionKeeperClient` that routes corrections
through the owner over `POST /corrections` — the change demo 8's explanation asks for.
