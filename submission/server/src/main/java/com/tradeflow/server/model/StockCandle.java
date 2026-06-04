package com.tradeflow.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;

@Table("stock_candles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCandle {

    @PrimaryKey
    private StockCandleKey key;

    @Column("open")
    private BigDecimal open;

    @Column("high")
    private BigDecimal high;

    @Column("low")
    private BigDecimal low;

    @Column("close")
    private BigDecimal close;

    @Column("volume")
    private Long volume;
}
