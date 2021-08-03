package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
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
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.ufsc.investfunds.companies.entities.PublicCompanyStockSerie;
import br.ufsc.investfunds.companies.entities.PublicCompanyTicker;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import net.lingala.zip4j.ZipFile;

public class CompanyStocksProcedure {

    public static <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);

        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue);
    }

    public static void doDownload(EntityDataStore<Persistable> dataStore, Path cvmPath) throws URISyntaxException {
        // Get Last Stock Price Date
        var lastStockPriceDates = dataStore.select(PublicCompanyStockSerie.TRADE_DATE)
                .orderBy(PublicCompanyStockSerie.TRADE_DATE.desc()).limit(1).get().toList();
        var lastStockPriceDate = lastStockPriceDates.size() > 0
                ? lastStockPriceDates.get(0).get(PublicCompanyStockSerie.TRADE_DATE).toLocalDate()
                : Date.valueOf("2015-01-01").toLocalDate();
        System.out.println(String.format("[CompanyStocksProcedure] [doDownload] Syncronizing from %s",
                lastStockPriceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        var today = LocalDate.now().withDayOfMonth(2);
        // Iterate Over Months, Downloading the Updates
        var downloadURIStream = new ArrayList<URI>();
        // Define Download URI Stream
        for (var itDate = lastStockPriceDate; itDate.withDayOfMonth(1).isBefore(today); itDate = itDate.plusMonths(1)) {
            // Define URI to download
            var downloadURI = new URI(
                    String.format("http://bvmf.bmfbovespa.com.br/InstDados/SerHist/COTAHIST_M%02d%04d.ZIP",
                            itDate.getMonthValue(), itDate.getYear()));
            // Insert then in stream
            downloadURIStream.add(downloadURI);
        }
        // Define Extraction Folder
        var extractionFolder = cvmPath.resolve("STOCK/PRICES/");
        extractionFolder.toFile().mkdirs();
        // Create HTTP Handler
        var http = HttpClient.newHttpClient();
        // Download Files (In Parallel)
        System.out.println(String.format("[CompanyStocksProcedure] [doDownload] Downloading %d historical months ...", downloadURIStream.size()));
        chunked(downloadURIStream.stream(), 8).forEach((downloadUris) -> {
            downloadUris.stream().parallel().forEach(downloadUri -> {
                try {
                    // Create Temp File for Zip
                    var tempZipFile = Files.createTempFile("stocks-", ".zip").toFile();
                    // Download Zip
                    var zipDownloadRes = http.send(HttpRequest.newBuilder(downloadUri).build(),
                            BodyHandlers.ofFile(tempZipFile.toPath()));
                    System.out.println(String.format("[CompanyStocksProcedure] [doDownload] Extracting %s",
                            tempZipFile.getName()));
                    // Return Downloaded Temp File
                    var zip = new ZipFile(zipDownloadRes.body().toFile());
                    zip.extractAll(extractionFolder.toString());
                    zip.close();
                } catch (IOException | InterruptedException e) {
                    // End Processing
                }
            });
        });
        System.out.println("[CompanyStocksProcedure] [doDownload] Download/Extraction Complete");
    }

    public static void doRun(EntityDataStore<Persistable> dataStore, Path filePath) throws IOException {
        // Convert File Content to Stock Entries
        try (var stockFileLines = Files.lines(filePath, Charset.forName("ISO-8859-1"))) {
            // Build Entities
            chunked(stockFileLines,  100).forEach((stockLines) -> {
                var stockFileEntities = stockLines.stream()
                        // Process In Parallel
                        .parallel()
                        // Map Entries
                        .map((stockLine) -> {
                            // You can learn more about the format at
                            // http://www.b3.com.br/pt_br/market-data-e-indices/servicos-de-dados/market-data/historico/mercado-a-vista/cotacoes-historicas/

                            // Skip if not a valid Stock Registry
                            if (!stockLine.startsWith("01") || !stockLine.substring(11, 13).equals("02")) {
                                return null;
                            }
                            // Parse Line
                            var ticker = stockLine.substring(13, 25).strip();
                            var tradeDate = String.format("%s-%s-%s", stockLine.substring(3, 7),
                                    stockLine.substring(7, 9), stockLine.substring(9, 11));
                            var coin = stockLine.substring(53, 57);
                            var openingPrice = new BigDecimal(new BigInteger(stockLine.substring(57, 70), 2));
                            var maximumPrice = new BigDecimal(new BigInteger(stockLine.substring(70, 83), 2));
                            var minimumPrice = new BigDecimal(new BigInteger(stockLine.substring(83, 96), 2));
                            var averagePrice = new BigDecimal(new BigInteger(stockLine.substring(96, 109), 2));
                            var closingPrice = new BigDecimal(new BigInteger(stockLine.substring(109, 121), 2));
                            // Create Entity
                            var tickerEntity = dataStore.findByKey(PublicCompanyTicker.class, ticker);
                            var stockEntry = new PublicCompanyStockSerie();
                            // Update Info
                            stockEntry.setTicker(tickerEntity);
                            stockEntry.setTradeDate(Date.valueOf(tradeDate));
                            stockEntry.setCoin(coin);
                            stockEntry.setOpeningPrice(openingPrice);
                            stockEntry.setMaximumPrice(maximumPrice);
                            stockEntry.setMinimumPrice(minimumPrice);
                            stockEntry.setAveragePrice(averagePrice);
                            stockEntry.setClosingPrice(closingPrice);
                            // Return the Populated Entity
                            return stockEntry;
                        }).collect(Collectors.filtering(entry -> entry != null, Collectors.toList()));
                // Insert Entities in Database
                dataStore.upsert(stockFileEntities);
            });
        }
    }

}
