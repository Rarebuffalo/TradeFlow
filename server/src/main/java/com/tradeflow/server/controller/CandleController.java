package com.tradeflow.server.controller;

import com.tradeflow.server.dto.CandleResponse;
import com.tradeflow.server.service.CandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
@Tag(name = "Stock Candles")
public class CandleController {

    private final CandleService candleService;

    @Operation(summary = "Retrieve aggregated stock candles")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CandleResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/candles")
    public ResponseEntity<CandleResponse> getCandles(
            @Parameter(description = "Stock symbol", required = true, example = "TCS")
            @RequestParam String symbol,

            @Parameter(description = "Aggregation timeframe (1m, 5m, 15m, 30m, 1h, 1d)", required = true, example = "15m")
            @RequestParam String timeframe,

            @Parameter(description = "Start date-time (yyyy-MM-dd HH:mm:ss)", required = true, example = "2026-01-01 09:15:00")
            @RequestParam("start_date") String startDate,

            @Parameter(description = "End date-time (yyyy-MM-dd HH:mm:ss)", required = true, example = "2026-01-01 15:30:00")
            @RequestParam("end_date") String endDate
    ) {
        CandleResponse response = candleService.getCandles(symbol, timeframe, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
