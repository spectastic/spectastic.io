-- The positions store (002-downstream-consumers T-012; 003-jdbc-persistence runs it at open).
CREATE TABLE IF NOT EXISTS positions (
  instrument VARCHAR(32) PRIMARY KEY,
  quantity   BIGINT      NOT NULL
);

-- The applied-tradeId record: idempotency lives here, written in the same transaction as the quantity.
CREATE TABLE IF NOT EXISTS applied_trades (
  trade_id   VARCHAR(64) PRIMARY KEY,
  instrument VARCHAR(32) NOT NULL
);
