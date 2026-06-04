# Stock Candle Aggregation Service

This project is a stock candle aggregation service that reads 1-minute OHLCV (Open, High, Low, Close, Volume) data from Apache Cassandra and aggregates it to higher timeframes (5m, 15m, 30m, 1h, 1d) via a REST API. It also includes a standalone data ingestion script and a console client to consume the API.

---

## Architecture Diagram

```text
                                +-------------------+
                                |   stock_data.csv  |
                                +---------+---------+
                                          |
                                          | Ingest (CSV parsing)
                                          v
                                +-------------------+
                                |    CsvImporter    |
                                +---------+---------+
                                          |
                                          | Direct CQL Write
                                          v
                                +-------------------+
                                | Apache Cassandra  |
                                +---------+---------+
                                          ^
                                          | Query (Partition-by-Day)
                                          |
                                +---------+---------+
                                |  CandleRepository |
                                +---------+---------+
                                          ^
                                          |
                                          v
                                +-------------------+
                                |   CandleService   | <-----+ TimeframeAggregator
                                +---------+---------+
                                          ^
                                          |
                                          v
                                +-------------------+
                                |  CandleController |
                                +---------+---------+
                                          ^
                                          |
                                          | HTTP REST (GET /api/v1/candles)
                                          |
                                +---------+---------+
                                |   CandleClient    | (Console Client App)
                                +-------------------+
```

---

## Tech Stack

* **Java 17** (or above)
* **Spring Boot 3.3.0**
* **Maven 3.9**
* **Apache Cassandra** (DataStax Cassandra Java Driver)
* **Lombok**
* **Springdoc OpenAPI** (Swagger UI)

---

## Assumptions

* **1-Minute Granularity**: Input data in `stock_data.csv` is assumed to be at a 1-minute interval granularity.
* **Market Hours & Holidays**: Days or periods with missing candles (e.g., weekends, holidays, off-market hours) are simply ignored and treated as missing data in aggregation. No empty/zero candles are generated.
* **Clock Interval Alignment**: Timeframe buckets are aligned to the clock relative to the start of the epoch (which naturally aligns with UTC day start 00:00:00). For example, 15-minute buckets are grouped as `09:15-09:29`, `09:30-09:44`, etc.
* **Date Query Limits**: Query ranges spanning multiple days require querying multiple partition keys sequentially since the table is partitioned by `(symbol, trade_date)`. This keeps Cassandra queries highly optimized.

---

## Cassandra Schema Setup

Run the CQL commands in `schema.cql` to create the keyspace and table:

```sql
-- Create Keyspace
CREATE KEYSPACE IF NOT EXISTS stock_keyspace
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

-- Create Stock Candles Table
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
```

> **Design Choice**: Partitioning by `(symbol, trade_date)` ensures that data for a given symbol is distributed evenly across multiple nodes by day, preventing a single partition from bloating. Clustering on `datetime` keeps the candles chronologically sorted in storage.

---

## Setup & Running Guide

### 1. Ingest CSV Data into Cassandra
Make sure the `stock_data.csv` is in the root directory (where this README is located). Compile the server package and run the `CsvImporter` class:

```bash
cd server
mvn clean compile
mvn exec:java -Dexec.mainClass="com.tradeflow.server.importer.CsvImporter"
```

### 2. Start the Server Application
Run the Spring Boot application from the `server` directory:

```bash
mvn spring-boot:run
```
The server will start on port `8080`.

### 3. API Endpoint Details
* **REST URL**: `GET /api/v1/candles`
* **Query Parameters**:
  * `symbol`: TCS
  * `timeframe`: 15m (supported values: 1m, 5m, 15m, 30m, 1h, 1d)
  * `start_date`: 2026-01-01 09:15:00 (format: `yyyy-MM-dd HH:mm:ss`)
  * `end_date`: 2026-01-01 15:30:00 (format: `yyyy-MM-dd HH:mm:ss`)

#### Sample cURL Request:
```bash
curl -X GET "http://localhost:8080/api/v1/candles?symbol=TCS&timeframe=15m&start_date=2026-01-01%2009:15:00&end_date=2026-01-01%2015:30:00"
```

#### Sample JSON Response:
```json
{
  "symbol": "TCS",
  "timeframe": "15m",
  "candles": [
    {
      "datetime": "2026-01-01T09:15:00Z",
      "open": 3215.0,
      "high": 3230.0,
      "low": 3208.0,
      "close": 3228.0,
      "volume": 450000
    }
  ],
  "count": 1
}
```

### 4. Swagger UI Documentation
Once the server is running, you can view and test the API using Swagger UI:
* **Interactive UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Specs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 5. Running the Console Client
Compile the client application and execute it with parameters:

```bash
cd ../client
mvn clean package
java -jar target/client-1.0.0.jar TCS 15m "2026-01-01 09:15:00" "2026-01-01 15:30:00"
```

---

## Running Unit Tests
To run the aggregator logic unit tests:

```bash
cd ../server
mvn test
```
