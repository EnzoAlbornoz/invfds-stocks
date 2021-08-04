package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;


public class CompanyRegisterProcedure {
    public static void doRun(Connection conn, Path filePath) throws IOException, SQLException {
        // Load SQL Statement from Resource
        var queryText = new String(CompanyRegisterProcedure.class.getResourceAsStream("/sql/insertCompany.sql").readAllBytes(), "UTF-8");
        // Read from File
        var csvReader = CSVParser.parse(filePath, StandardCharsets.ISO_8859_1,
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader());
        // Create Transaction
        conn.setAutoCommit(false);
        try {
            var stmt = conn.prepareStatement(queryText);
            // For each row, try to add it to table
            for (var record : csvReader.getRecords()) {
                // Clear Parameters
                stmt.clearParameters();
                // Fetch Parameters
                var sanitizedCnpj = record.get("CNPJ_CIA").replaceAll("\\D", "");
                var socialName = record.get("DENOM_SOCIAL").strip();
                var status = record.get("SIT");
                // Define Parameters
                stmt.setString(1, sanitizedCnpj);
                stmt.setString(2, socialName);
                stmt.setString(3, status);
                // Persist In DB
                stmt.addBatch();
            }
            // Do Query
            stmt.executeBatch();
            // var modifiedRows = IntStream.of(batchReturn).sum();
            conn.commit();
        } catch(Exception e) {
            e.printStackTrace();
            conn.rollback();
        }
    }
}
