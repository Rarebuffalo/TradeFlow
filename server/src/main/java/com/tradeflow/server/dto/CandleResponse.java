package com.tradeflow.server.dto;

import java.util.List;

public class CandleResponse {
    private String symbol;
    private String timeframe;
    private List<AggregatedCandle> candles;
    private int count;

    public CandleResponse() {
    }

    public CandleResponse(String symbol, String timeframe, List<AggregatedCandle> candles, int count) {
        this.symbol = symbol;
        this.timeframe = timeframe;
        this.candles = candles;
        this.count = count;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public List<AggregatedCandle> getCandles() {
        return candles;
    }

    public void setCandles(List<AggregatedCandle> candles) {
        this.candles = candles;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
