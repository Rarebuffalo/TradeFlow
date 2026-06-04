package com.tradeflow.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@PrimaryKeyClass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCandleKey implements Serializable {

    @PrimaryKeyColumn(name = "symbol", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String symbol;

    @PrimaryKeyColumn(name = "trade_date", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private LocalDate tradeDate;

    @PrimaryKeyColumn(name = "datetime", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING, ordinal = 2)
    private Instant datetime;
}
