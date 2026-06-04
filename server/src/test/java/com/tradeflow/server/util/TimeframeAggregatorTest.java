package com.tradeflow.server.util;

import com.tradeflow.server.dto.AggregatedCandle;
import com.tradeflow.server.model.StockCandle;
import com.tradeflow.server.model.StockCandleKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeframeAggregatorTest {

    private List<StockCandle> sample1mCandles;

    @BeforeEach
    public void setUp() {
        sample1mCandles = new ArrayList<>();
        LocalDate tradeDate = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < 15; i++) {
            Instant datetime = LocalDateTimeToInstant(2026, 1, 1, 9, 15 + i, 0);

            BigDecimal open = BigDecimal.valueOf(100 + i);
            BigDecimal close = BigDecimal.valueOf(101 + i);
            BigDecimal high = BigDecimal.valueOf(105 + i);
            BigDecimal low = BigDecimal.valueOf(95 + i);
            Long volume = 1000L;

            StockCandleKey key = new StockCandleKey("TCS", tradeDate, datetime);
            sample1mCandles.add(new StockCandle(key, open, high, low, close, volume));
        }
    }

    @Test
    public void testAggregateTo5m() {
        List<AggregatedCandle> result = TimeframeAggregator.aggregate(sample1mCandles, "5m");

        assertEquals(3, result.size());

        AggregatedCandle first = result.get(0);
        assertEquals(LocalDateTimeToInstant(2026, 1, 1, 9, 15, 0), first.getDatetime());
        assertEquals(BigDecimal.valueOf(100.0), first.getOpen());
        assertEquals(BigDecimal.valueOf(109.0), first.getHigh());
        assertEquals(BigDecimal.valueOf(95.0), first.getLow());
        assertEquals(BigDecimal.valueOf(105.0), first.getClose());
        assertEquals(5000L, first.getVolume());
    }

    @Test
    public void testAggregateTo15m() {
        List<AggregatedCandle> result = TimeframeAggregator.aggregate(sample1mCandles, "15m");

        assertEquals(1, result.size());

        AggregatedCandle aggregated = result.get(0);
        assertEquals(LocalDateTimeToInstant(2026, 1, 1, 9, 15, 0), aggregated.getDatetime());
        assertEquals(BigDecimal.valueOf(100.0), aggregated.getOpen());
        assertEquals(BigDecimal.valueOf(119.0), aggregated.getHigh());
        assertEquals(BigDecimal.valueOf(95.0), aggregated.getLow());
        assertEquals(BigDecimal.valueOf(115.0), aggregated.getClose());
        assertEquals(15000L, aggregated.getVolume());
    }

    @Test
    public void testAggregateTo1h() {
        List<AggregatedCandle> result = TimeframeAggregator.aggregate(sample1mCandles, "1h");

        assertEquals(1, result.size());
        assertEquals(LocalDateTimeToInstant(2026, 1, 1, 9, 0, 0), result.get(0).getDatetime());
    }

    @Test
    public void testAggregateTo1d() {
        List<AggregatedCandle> result = TimeframeAggregator.aggregate(sample1mCandles, "1d");

        assertEquals(1, result.size());
        assertEquals(LocalDateTimeToInstant(2026, 1, 1, 0, 0, 0), result.get(0).getDatetime());
    }

    @Test
    public void testEmptyList() {
        List<AggregatedCandle> result = TimeframeAggregator.aggregate(new ArrayList<>(), "15m");
        assertTrue(result.isEmpty());
    }

    private Instant LocalDateTimeToInstant(int year, int month, int day, int hour, int minute, int second) {
        return java.time.LocalDateTime.of(year, month, day, hour, minute, second).toInstant(ZoneOffset.UTC);
    }
}
