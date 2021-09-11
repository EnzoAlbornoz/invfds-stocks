package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.Failable;

import net.lingala.zip4j.ZipFile;

public class CompanyStocksProcedure {

    private final static Logger LOGGER = Logger.getLogger("CompanyStocksProcedure");

    public static <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);

        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue);
    }

    public static void doDownload(Connection conn, Path cvmPath)
            throws URISyntaxException, UnsupportedEncodingException, IOException, SQLException {
        // Get Last Stock Price Date from Database
        var query = new String(
                CompanyStocksProcedure.class.getResourceAsStream("/sql/getLastSyncedStockDate.sql").readAllBytes(),
                "UTF-8");
        var queryResult = conn.createStatement().executeQuery(query);
        queryResult.next();
        var lastStockPriceDate = queryResult.getDate("LAST_SYNCED_DATE").toLocalDate();
        System.out.println(String.format("[CompanyStocksProcedure] [doDownload] Syncronizing from %s",
                lastStockPriceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        var today = LocalDate.now().withDayOfMonth(2);
        // Iterate Over Months, Downloading the Updates
        // Define Download URI Stream
        List<URI> downloadURIStream = Failable.stream(lastStockPriceDate.withDayOfMonth(1).datesUntil(today, Period.ofMonths(1)))
                .map((itDate) -> {
                    // Define URI to download
                    var downloadURI = new URI(
                            String.format("http://bvmf.bmfbovespa.com.br/InstDados/SerHist/COTAHIST_M%02d%04d.ZIP",
                                    itDate.getMonthValue(), itDate.getYear()));
                    return downloadURI;
                }).collect(Collectors.toList());
        // Define Extraction Folder
        var extractionFolder = cvmPath.resolve("STOCK/PRICES/");
        extractionFolder.toFile().mkdirs();
        // Create HTTP Handler
        var http = HttpClient.newHttpClient();
        // Download Files (In Parallel)
        System.out.println(String.format("[CompanyStocksProcedure] [doDownload] Downloading %d historical months ...",
                downloadURIStream.size()));
        downloadURIStream.stream().parallel().forEach(downloadUri -> {
            try {
                // Create Temp File for Zip
                var tempZipFile = Files.createTempFile("stocks-", ".zip").toFile();
                // Download Zip
                var zipDownloadRes = http.send(HttpRequest.newBuilder(downloadUri).build(),
                        BodyHandlers.ofFile(tempZipFile.toPath()));
                System.out.println(
                        String.format("[CompanyStocksProcedure] [doDownload] Extracting %s", tempZipFile.getName()));
                // Return Downloaded Temp File
                var zip = new ZipFile(zipDownloadRes.body().toFile());
                zip.extractAll(extractionFolder.toString());
                zip.close();
            } catch (IOException | InterruptedException e) {
                // End Processing
            }
        });
        System.out.println("[CompanyStocksProcedure] [doDownload] Download/Extraction Complete");
    }

    public static void doRun(Connection conn, Path filePath) throws IOException, SQLException {
        LOGGER.info(String.format("[doRun] Processing file %s", filePath.toString()));
        // Load Query
        var query = new String(
                CompanyStocksProcedure.class.getResourceAsStream("/sql/insertStockPriceHistoric.sql").readAllBytes(),
                "ISO-8859-1");
        // Convert File Content to Stock Entries
        try (var stockFileLines = Files.lines(filePath, Charset.forName("ISO-8859-1"))) {
            // Create Transaction
            conn.setAutoCommit(false);
            try {
                // Prepare Statement
                var stmt = conn.prepareStatement(query);
                // Build Entities
                stockFileLines.parallel().filter(stockLine -> stockLine.startsWith("01")
                        && stockLine.substring(10, 12).equals("02") && stockLine.subSequence(24, 27).equals("010"))
                        .sequential().forEach((stockLine) -> {
                            /*
                             * You can learn more about the format at
                             * http://www.b3.com.br/pt_br/market-data-e-indices/servicos-de-dados/market-
                             * data/historico/mercado-a-vista/cotacoes-historicas/
                             */
                            try {
                                // Clear Statement Parameters
                                stmt.clearParameters();
                                // Parse Line
                                var ticker = stockLine.substring(12, 24).strip();
                                var tradeDate = String.format("%s-%s-%s", stockLine.substring(2, 6),
                                        stockLine.substring(6, 8), stockLine.substring(8, 10));
                                var coin = stockLine.substring(52, 56);
                                var openingPrice = new BigDecimal(new BigInteger(stockLine.substring(56, 69)), 2);
                                var maximumPrice = new BigDecimal(new BigInteger(stockLine.substring(69, 82)), 2);
                                var minimumPrice = new BigDecimal(new BigInteger(stockLine.substring(82, 95)), 2);
                                var averagePrice = new BigDecimal(new BigInteger(stockLine.substring(95, 108)), 2);
                                var closingPrice = new BigDecimal(new BigInteger(stockLine.substring(108, 121)), 2);
                                // Define Parameters
                                stmt.setString(1, ticker);
                                stmt.setDate(2, Date.valueOf(tradeDate));
                                stmt.setString(3, coin);
                                stmt.setBigDecimal(4, openingPrice);
                                stmt.setBigDecimal(5, maximumPrice);
                                stmt.setBigDecimal(6, minimumPrice);
                                stmt.setBigDecimal(7, averagePrice);
                                stmt.setBigDecimal(8, closingPrice);
                                // Add to Batch
                                stmt.addBatch();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                // Execute Batch
                stmt.executeBatch();
                // var resultStream = IntStream.of();
                // var touchedRows = resultStream.filter(i -> i >= 0).sum();
                // Commit Results
                conn.commit();
                // Return Touched Rows
                // System.out.printf("[CompanyStocksProcedure] [doRun] Modified rows: %d%n",
                // touchedRows);
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        }
    }
}
