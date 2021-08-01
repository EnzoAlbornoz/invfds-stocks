package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.stream.Collectors;

import br.ufsc.investfunds.companies.entities.PublicCompanyStockSerie;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class CompanyStocksProcedure {

    public static void doRun(EntityDataStore<Persistable> dataStore, Path filePath) throws IOException {
        // Convert File Content to Stock Entries
        try (var stockFileLines = Files.lines(filePath, Charset.forName("ISO-8859-1"))) {
            // Build Entities
            var stockFileEntities = stockFileLines
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
                        var tradeDate = String.format("%s-%s-%s", stockLine.substring(3, 7), stockLine.substring(7, 9),
                                stockLine.substring(9, 11));
                        var coin = stockLine.substring(53, 57);
                        var openingPrice = new BigDecimal(new BigInteger(stockLine.substring(57, 70), 2));
                        var maximumPrice = new BigDecimal(new BigInteger(stockLine.substring(70, 83), 2));
                        var minimumPrice = new BigDecimal(new BigInteger(stockLine.substring(83, 96), 2));
                        var averagePrice = new BigDecimal(new BigInteger(stockLine.substring(96, 109), 2));
                        var closingPrice = new BigDecimal(new BigInteger(stockLine.substring(109, 121), 2));
                        // Create Entity
                        var stockEntry = new PublicCompanyStockSerie();
                        // Update Info
                        stockEntry.setTicker(ticker);
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
        }
    }

}
