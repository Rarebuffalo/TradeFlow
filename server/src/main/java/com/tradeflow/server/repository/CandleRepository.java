package com.tradeflow.server.repository;

import com.tradeflow.server.model.StockCandle;
import com.tradeflow.server.model.StockCandleKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CandleRepository extends CassandraRepository<StockCandle, StockCandleKey> {

    @Query("SELECT * FROM stock_candles WHERE symbol = ?0 AND trade_date = ?1 AND datetime >= ?2 AND datetime <= ?3")
    List<StockCandle> findBySymbolAndTradeDateAndDatetimeRange(
            String symbol,
            LocalDate tradeDate,
            Instant startDateTime,
            Instant endDateTime
    );

    @Query("SELECT * FROM stock_candles WHERE symbol = ?0 LIMIT 1 ALLOW FILTERING")
    List<StockCandle> findSymbolExists(String symbol);
}
