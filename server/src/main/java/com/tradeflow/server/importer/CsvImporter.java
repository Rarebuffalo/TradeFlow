package com.tradeflow.server.importer;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CsvImporter {

    private static final Logger log = LoggerFactory.getLogger(CsvImporter.class);

    private static final String DEFAULT_CSV_PATH = "stock_data.csv";
    private static final String CASSANDRA_HOST = "127.0.0.1";
    private static final int CASSANDRA_PORT = 9042;
    private static final String KEYSPACE = "stock_keyspace";
    private static final String DATACENTER = "datacenter1";

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    public static void main(String[] args) {
        String csvPath = args.length > 0 ? args[0] : DEFAULT_CSV_PATH;
        log.info("Starting CSV Import from: {}", csvPath);

        long startTime = System.currentTimeMillis();
        int recordsImported = 0;

        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(CASSANDRA_HOST, CASSANDRA_PORT))
                .withLocalDatacenter(DATACENTER)
                .withKeyspace(KEYSPACE)
                .build()) {

            String insertQuery = "INSERT INTO stock_candles (symbol, trade_date, datetime, open, high, low, close, volume) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = session.prepare(insertQuery);

            try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
                String line;
                br.readLine(); 

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] columns = line.split(",");
                    if (columns.length < 7) {
                        continue;
                    }

                    try {
                        String symbol = columns[0].trim();
                        Instant datetime = parseDateTime(columns[1].trim());
                        LocalDate tradeDate = LocalDate.ofInstant(datetime, ZoneOffset.UTC);
                        BigDecimal open = new BigDecimal(columns[2].trim());
                        BigDecimal high = new BigDecimal(columns[3].trim());
                        BigDecimal low = new BigDecimal(columns[4].trim());
                        BigDecimal close = new BigDecimal(columns[5].trim());
                        long volume = Long.parseLong(columns[6].trim());

                        BoundStatement boundStatement = preparedStatement.bind(
                                symbol,
                                tradeDate,
                                datetime,
                                open,
                                high,
                                low,
                                close,
                                volume
                        );

                        session.execute(boundStatement);
                        recordsImported++;

                        if (recordsImported % 500 == 0) {
                            log.info("Imported {} records...", recordsImported);
                        }

                    } catch (Exception e) {
                        log.error("Failed to parse row: {} due to: {}", line, e.getMessage());
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("Import completed. {} records in {} ms.", recordsImported, (endTime - startTime));

        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", csvPath, e);
        } catch (Exception e) {
            log.error("Cassandra error during import", e);
        }
    }

    private static Instant parseDateTime(String dateTimeStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
                return localDateTime.toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
        }
        return Instant.parse(dateTimeStr);
    }
}
