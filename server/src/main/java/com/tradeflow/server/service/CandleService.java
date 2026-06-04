package com.tradeflow.server.service;

import com.tradeflow.server.dto.AggregatedCandle;
import com.tradeflow.server.dto.CandleResponse;
import com.tradeflow.server.model.StockCandle;
import com.tradeflow.server.repository.CandleRepository;
import com.tradeflow.server.util.TimeframeAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CandleService {

    private static final Logger log = LoggerFactory.getLogger(CandleService.class);
    private final CandleRepository candleRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> VALID_TIMEFRAMES = Arrays.asList("1m", "5m", "15m", "30m", "1h", "1d");

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public CandleResponse getCandles(String symbol, String timeframe, String startDateStr, String endDateStr) {
        log.info("Request received - Symbol: {}, Timeframe: {}, Start: {}, End: {}", symbol, timeframe, startDateStr, endDateStr);

        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol parameter is required");
        }
        if (timeframe == null || timeframe.trim().isEmpty()) {
            throw new IllegalArgumentException("Timeframe parameter is required");
        }
        if (!VALID_TIMEFRAMES.contains(timeframe.trim().toLowerCase())) {
            throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }

        Instant startInstant;
        Instant endInstant;
        LocalDateTime startLdt;
        LocalDateTime endLdt;

        try {
            startLdt = LocalDateTime.parse(startDateStr.trim(), DATE_TIME_FORMATTER);
            startInstant = startLdt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid start_date format. Expected: yyyy-MM-dd HH:mm:ss");
        }

        try {
            endLdt = LocalDateTime.parse(endDateStr.trim(), DATE_TIME_FORMATTER);
            endInstant = endLdt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid end_date format. Expected: yyyy-MM-dd HH:mm:ss");
        }

        if (startInstant.isAfter(endInstant)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        List<String> existenceCheck = candleRepository.findSymbolExists(symbol.trim());
        if (existenceCheck.isEmpty()) {
            throw new ResourceNotFoundException("Symbol not found: " + symbol);
        }

        LocalDate startDate = startLdt.toLocalDate();
        LocalDate endDate = endLdt.toLocalDate();
        List<LocalDate> datesToQuery = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            datesToQuery.add(current);
            current = current.plusDays(1);
        }

        long queryStartTime = System.currentTimeMillis();
        List<StockCandle> rawCandles = new ArrayList<>();
        for (LocalDate date : datesToQuery) {
            List<StockCandle> dayCandles = candleRepository.findBySymbolAndTradeDateAndDatetimeRange(
                    symbol.trim(), date, startInstant, endInstant
            );
            rawCandles.addAll(dayCandles);
        }
        long queryEndTime = System.currentTimeMillis();
        log.info("Fetched {} raw candles in {} ms", rawCandles.size(), (queryEndTime - queryStartTime));

        long aggStartTime = System.currentTimeMillis();
        List<AggregatedCandle> aggregatedCandles = TimeframeAggregator.aggregate(rawCandles, timeframe);
        long aggEndTime = System.currentTimeMillis();
        log.info("Aggregated to {} candles in {} ms", aggregatedCandles.size(), (aggEndTime - aggStartTime));

        return new CandleResponse(symbol.trim(), timeframe.trim(), aggregatedCandles, aggregatedCandles.size());
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
