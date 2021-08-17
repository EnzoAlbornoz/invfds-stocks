package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.collections4.ListUtils;

public class CompanyPassiveBalanceSheetStatementProcedure {

    private final static Logger LOGGER = Logger.getLogger("CompanyPassiveBalanceSheetStatementProcedure");

    public static void doRun(Connection conn, Path filePath) throws IOException, SQLException {
        // Load Query
        var query = new String(
                CompanyPassiveBalanceSheetStatementProcedure.class.getResourceAsStream("/sql/insertBPP.sql").readAllBytes(),
                "UTF-8");
        // Read from File
        var csvReader = CSVParser.parse(filePath, StandardCharsets.ISO_8859_1,
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader());
        // Define Insert
        try {
            // Disable Autocommit
            conn.setAutoCommit(false);
            // Create Statement
            var stmt = conn.prepareStatement(query);
            // Register Records
            final var BATCH_SIZE = 1000;
            var chunkRows = ListUtils.partition(csvReader.getRecords(), BATCH_SIZE);
            LOGGER.info(String.format("Will process %d rows", chunkRows.size() * BATCH_SIZE));
            for (var chunk: chunkRows) {
                // Clear Batch
                stmt.clearBatch();
                for (var csvRow : chunk) {
                    // Check Row is Valid
                    // System.out.println();
                    if (!csvRow.get("ORDEM_EXERC").equals("ÚLTIMO") || (LocalDate.parse(csvRow.get("DT_INI_EXERC")).until(LocalDate.parse(csvRow.get("DT_REFER"))).toTotalMonths() > 4)) {
                        continue;
                    }
                    // Parse Row
                    var sanitizedCnpj = csvRow.get("CNPJ_CIA").replaceAll("\\D", "");
                    var referenceStartDate = Date.valueOf(csvRow.get("DT_INI_EXERC"));
                    var referenceEndDate = Date.valueOf(csvRow.get("DT_REFER"));
                    var coin = csvRow.get("MOEDA");
                    var scale = csvRow.get("ESCALA_MOEDA");
                    var accountCode = csvRow.get("CD_CONTA");
                    var accountDescription = csvRow.get("DS_CONTA");
                    var accountValue = new BigDecimal(csvRow.get("VL_CONTA"));
                    // Prepare Statement
                    stmt.clearParameters();
                    stmt.setString(1, sanitizedCnpj);
                    stmt.setDate(2, referenceStartDate);
                    stmt.setDate(3, referenceEndDate);
                    stmt.setString(4, coin);
                    stmt.setString(5, scale);
                    stmt.setString(6, accountCode);
                    stmt.setString(7, accountDescription);
                    stmt.setBigDecimal(8, accountValue);
                    // Add Statement to Batch
                    stmt.addBatch();
                }
                // Run Statement Batch
                stmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
        }
    }
}
