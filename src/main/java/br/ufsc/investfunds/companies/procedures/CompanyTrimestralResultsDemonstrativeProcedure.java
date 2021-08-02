package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import br.ufsc.investfunds.companies.entities.PublicCompanyRegister;
import br.ufsc.investfunds.companies.entities.PublicCompanyTrimestralResultsDemonstrative;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class CompanyTrimestralResultsDemonstrativeProcedure {
    public static void doRun(EntityDataStore<Persistable> dataStore, Path filePath) throws IOException {
        // Read from File
        var csvReader = CSVParser.parse(filePath, StandardCharsets.ISO_8859_1,
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader());
        // Register Records
        var tickerEntities = csvReader.getRecords().stream().parallel().map(csvRow -> {
            // Check Row is Valid
            if (!csvRow.get("ORDEM_EXERC").equals("ÚLTIMO") || !(LocalDate.parse(csvRow.get("DT_INI_EXERC"))
                    .isEqual(LocalDate.parse(csvRow.get("DT_REFER")).withDayOfMonth(1).minusMonths(3)))) {
                return null;
            }
            // Parse Row
            var sanitizedCnpj = csvRow.get("CNPJ_CIA").replaceAll("\\D", "");
            var referenceDate = Date.valueOf(csvRow.get("DT_REFER"));
            var coin = csvRow.get("MOEDA");
            var scale = csvRow.get("ESCALA_MOEDA");
            var accountCode = csvRow.get("CD_CONTA");
            var accountDescription = csvRow.get("DS_CONTA");
            var accountValue = new BigDecimal(csvRow.get("VL_CONTA"));
            // Fetch Entity
            var companyEntity = dataStore.findByKey(PublicCompanyRegister.class, sanitizedCnpj);
            // Create Entities
            var entity = new PublicCompanyTrimestralResultsDemonstrative(companyEntity, referenceDate);
            entity.setCoin(coin);
            entity.setScale(scale);
            entity.setAccountCode(accountCode);
            entity.setAccountDescription(accountDescription);
            entity.setAccountValue(accountValue);
            // Return Created Entity
            return entity;
        }).collect(Collectors.filtering((maybeEntity -> maybeEntity != null), Collectors.toList()));
        // Upsert Tickers
        dataStore.runInTransaction(() -> {
            dataStore.upsert(tickerEntities);
            return null;
        });
    }
}
