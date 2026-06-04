package com.tradeflow.server.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@PrimaryKeyClass
public class StockCandleKey implements Serializable {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String symbol;

    @PrimaryKeyColumn(name = "trade_date", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private LocalDate tradeDate;

    @PrimaryKeyColumn(name = "datetime", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING, ordinal = 2)
    private Instant datetime;

    public StockCandleKey() {
    }

    public StockCandleKey(String symbol, LocalDate tradeDate, Instant datetime) {
        this.symbol = symbol;
        this.tradeDate = tradeDate;
        this.datetime = datetime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Instant getDatetime() {
        return datetime;
    }

    public void setDatetime(Instant datetime) {
        this.datetime = datetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockCandleKey that = (StockCandleKey) o;
        return Objects.equals(symbol, that.symbol) &&
                Objects.equals(tradeDate, that.tradeDate) &&
                Objects.equals(datetime, that.datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, tradeDate, datetime);
    }
}
