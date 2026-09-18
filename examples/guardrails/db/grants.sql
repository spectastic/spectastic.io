-- D-008 of 002-downstream-consumers: the DB-grant backstop. Only the adapter's role may write
-- positions; a static rule that is evaded still meets this refusal (H2 SQL state 90096).
CREATE USER IF NOT EXISTS pk_adapter PASSWORD 'pk_adapter';
CREATE USER IF NOT EXISTS pk_reader  PASSWORD 'pk_reader';

GRANT SELECT, INSERT, UPDATE ON positions      TO pk_adapter;
GRANT SELECT, INSERT         ON applied_trades TO pk_adapter;
GRANT SELECT                 ON positions      TO pk_reader;
GRANT SELECT                 ON applied_trades TO pk_reader;
