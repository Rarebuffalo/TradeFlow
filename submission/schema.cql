-- Create Keyspace
CREATE KEYSPACE IF NOT EXISTS stock_keyspace
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

-- Create Stock Candles table
-- Primary Key structure:
--   Partition Key: (symbol, trade_date) -> Keeps data grouped by symbol and day, preventing partition bloating.
--   Clustering Key: datetime ASC -> Orders stock records chronologically.
CREATE TABLE IF NOT EXISTS stock_keyspace.stock_candles (
  symbol text,
  trade_date date,
  datetime timestamp,
  open decimal,
  high decimal,
  low decimal,
  close decimal,
  volume bigint,
  PRIMARY KEY ((symbol, trade_date), datetime)
) WITH CLUSTERING ORDER BY (datetime ASC);
