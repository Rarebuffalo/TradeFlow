package com.tradeflow.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleResponse {
    private String symbol;
    private String timeframe;
    private List<AggregatedCandle> candles;
    private int count;
}
