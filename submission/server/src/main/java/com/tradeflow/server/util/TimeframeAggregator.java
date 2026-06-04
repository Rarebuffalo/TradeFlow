package com.tradeflow.server.util;

import com.tradeflow.server.dto.AggregatedCandle;
import com.tradeflow.server.model.StockCandle;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class TimeframeAggregator {

    public static List<AggregatedCandle> aggregate(List<StockCandle> rawCandles, String timeframe) {
        if (rawCandles == null || rawCandles.isEmpty()) {
            return Collections.emptyList();
        }

        int minutes = parseTimeframe(timeframe);

        List<StockCandle> sortedCandles = new ArrayList<>(rawCandles);
        sortedCandles.sort(Comparator.comparing(c -> c.getKey().getDatetime()));

        Map<Instant, List<StockCandle>> grouped = new LinkedHashMap<>();
        for (StockCandle candle : sortedCandles) {
            Instant bucketStart = getBucketStart(candle.getKey().getDatetime(), minutes);
            grouped.computeIfAbsent(bucketStart, k -> new ArrayList<>()).add(candle);
        }

        List<AggregatedCandle> aggregatedList = new ArrayList<>();
        for (Map.Entry<Instant, List<StockCandle>> entry : grouped.entrySet()) {
            Instant bucketStart = entry.getKey();
            List<StockCandle> bucketCandles = entry.getValue();

            BigDecimal open = bucketCandles.get(0).getOpen();
            BigDecimal close = bucketCandles.get(bucketCandles.size() - 1).getClose();

            BigDecimal high = bucketCandles.get(0).getHigh();
            BigDecimal low = bucketCandles.get(0).getLow();
            long totalVolume = 0;

            for (StockCandle candle : bucketCandles) {
                if (candle.getHigh() != null && (high == null || candle.getHigh().compareTo(high) > 0)) {
                    high = candle.getHigh();
                }
                if (candle.getLow() != null && (low == null || candle.getLow().compareTo(low) < 0)) {
                    low = candle.getLow();
                }
                if (candle.getVolume() != null) {
                    totalVolume += candle.getVolume();
                }
            }

            aggregatedList.add(AggregatedCandle.builder()
                    .datetime(bucketStart)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(totalVolume)
                    .build());
        }

        return aggregatedList;
    }

    private static int parseTimeframe(String timeframe) {
        String cleaned = timeframe.trim().toLowerCase();
        switch (cleaned) {
            case "1m": return 1;
            case "5m": return 5;
            case "15m": return 15;
            case "30m": return 30;
            case "1h": return 60;
            case "1d": return 1440;
            default:
                throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
    }

    public static Instant getBucketStart(Instant instant, int timeframeMinutes) {
        if (timeframeMinutes == 1440) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            LocalDateTime dayStart = localDateTime.toLocalDate().atStartOfDay();
            return dayStart.toInstant(ZoneOffset.UTC);
        }

        // Align minutes into buckets from epoch (works since 1440 is divisible by all intervals)
        long minutesSinceEpoch = instant.getEpochSecond() / 60;
        long bucketMinutes = (minutesSinceEpoch / timeframeMinutes) * timeframeMinutes;
        return Instant.ofEpochSecond(bucketMinutes * 60);
    }
}
