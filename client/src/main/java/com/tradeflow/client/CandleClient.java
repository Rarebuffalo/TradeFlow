package com.tradeflow.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class CandleClient implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(CandleClient.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length < 4) {
            System.out.println("Error: Missing parameters.");
            System.out.println("Usage: java -jar client.jar <symbol> <timeframe> <start_date> <end_date>");
            System.out.println("Example: java -jar client.jar TCS 15m \"2026-01-01 09:15:00\" \"2026-01-01 15:30:00\"");
            return;
        }

        String symbol = args[0];
        String timeframe = args[1];
        String startDate = args[2];
        String endDate = args[3];

        String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/v1/candles")
                .queryParam("symbol", symbol)
                .queryParam("timeframe", timeframe)
                .queryParam("start_date", startDate)
                .queryParam("end_date", endDate)
                .toUriString();

        System.out.println("Connecting to API: " + url + "\n");

        try {
            ResponseEntity<ClientCandleResponse> responseEntity = restTemplate.getForEntity(url, ClientCandleResponse.class);
            ClientCandleResponse response = responseEntity.getBody();

            if (response != null) {
                System.out.println("=== Fetched Candle Data ===");
                System.out.println("Symbol: " + response.getSymbol());
                System.out.println("Timeframe: " + response.getTimeframe());
                System.out.println("Total Candles: " + response.getCount());
                System.out.println("===========================");

                List<ClientAggregatedCandle> candles = response.getCandles();
                if (candles == null || candles.isEmpty()) {
                    System.out.println("No candle data returned.");
                } else {
                    for (int i = 0; i < candles.size(); i++) {
                        ClientAggregatedCandle candle = candles.get(i);
                        System.out.printf("%d. %s | O: %.2f | H: %.2f | L: %.2f | C: %.2f | Vol: %d%n",
                                (i + 1),
                                candle.getDatetime(),
                                candle.getOpen(),
                                candle.getHigh(),
                                candle.getLow(),
                                candle.getClose(),
                                candle.getVolume()
                        );
                    }
                }
                System.out.println("===========================");
            }
        } catch (HttpClientErrorException ex) {
            System.err.println("API Error: " + ex.getStatusCode());
            System.err.println("Response: " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            System.err.println("Network Error: Failed to connect to stock candle service at localhost:8080");
            System.err.println("Details: " + ex.getMessage());
        }
    }

    public static class ClientCandleResponse {
        private String symbol;
        private String timeframe;
        private List<ClientAggregatedCandle> candles;
        private int count;

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

        public List<ClientAggregatedCandle> getCandles() {
            return candles;
        }

        public void setCandles(List<ClientAggregatedCandle> candles) {
            this.candles = candles;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class ClientAggregatedCandle {
        private String datetime;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public BigDecimal getOpen() {
            return open;
        }

        public void setOpen(BigDecimal open) {
            this.open = open;
        }

        public BigDecimal getHigh() {
            return high;
        }

        public void setHigh(BigDecimal high) {
            this.high = high;
        }

        public BigDecimal getLow() {
            return low;
        }

        public void setLow(BigDecimal low) {
            this.low = low;
        }

        public BigDecimal getClose() {
            return close;
        }

        public void setClose(BigDecimal close) {
            this.close = close;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }
    }
}
