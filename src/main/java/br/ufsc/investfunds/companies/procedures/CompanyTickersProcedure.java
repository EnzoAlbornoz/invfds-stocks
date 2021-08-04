package br.ufsc.investfunds.companies.procedures;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.function.Failable;
import org.json.JSONObject;

import br.ufsc.investfunds.companies.helper.StreamUtils;
import net.lingala.zip4j.ZipFile;

public class CompanyTickersProcedure {

    private final static Logger LOGGER = Logger.getLogger("CompanyTickersProcedure");

    enum IssuersHeaders {
        ISS_CODE, ISS_NAME, ISS_CNPJ, ISS_CREATION_DATE
    }

    public static void doDownload(Connection conn, Path cvmFolder)
            throws URISyntaxException, IOException, InterruptedException {
        var http = HttpClient.newHttpClient();
        // Download File into Memory
        var downloadOptionsURI = new URI("https://sistemaswebb3-listados.b3.com.br/isinProxy/IsinCall/GetTextDownload");
        System.out.println(String.format("[CompanyTickersProcedure] [doDownload] Feching Download options ..."));
        var response = http.send(HttpRequest.newBuilder(downloadOptionsURI).build(), BodyHandlers.ofString());
        // Check Download
        if (response.statusCode() != 200)
            throw new RuntimeException("Options Issuers - Status Code != 200");
        // Parse Current Hash for Issuers DB
        String issuersDBId = String.valueOf(new JSONObject(response.body()).getJSONObject("geralPt").getInt("id"));
        issuersDBId = Base64.getEncoder().encodeToString(issuersDBId.getBytes());
        // Download DB File
        var fileZipDownloadPath = File.createTempFile("issuers", ".zip");
        var fileDownloadOptionsURI = new URI(
                "https://sistemaswebb3-listados.b3.com.br/isinProxy/IsinCall/GetFileDownload/".concat(issuersDBId));
        System.out.println(String.format("[CompanyTickersProcedure] [doDownload] Downloading Issuers ..."));
        var dbFileResponse = http.send(HttpRequest.newBuilder(fileDownloadOptionsURI).build(),
                BodyHandlers.ofFile(fileZipDownloadPath.toPath()));
        if (dbFileResponse.statusCode() != 200)
            throw new RuntimeException("DBFile Issuers - Status Code != 200");
        System.out.println("[CompanyTickersProcedure] [doDownload] Downloading completed");
        // Create Zip from Stream
        try (var zipFile = new ZipFile(fileZipDownloadPath)) {
            // Create Folder and File
            var issuersFolder = cvmFolder.resolve("STOCK/ISSUERS").toFile();
            issuersFolder.mkdirs();
            // Extract File
            zipFile.extractFile(zipFile.getFileHeader("EMISSOR.TXT"), issuersFolder.getPath());
        }
    }

    public static void doRun(Connection conn, Path filePath) throws IOException, SQLException {
        LOGGER.info(String.format("[doRun] Loading issuers from %s", filePath.toString()));
        // Define Batch Size
        final var BATCH_SIZE = 200;
        // Define Query
        var query = new String(
                CompanyTickersProcedure.class.getResourceAsStream("/sql/insertTicker.sql").readAllBytes(), "UTF-8");
        // Read Files
        var csvReader = CSVParser.parse(filePath, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withHeader(IssuersHeaders.class));
        // Disable AutoCommit
        conn.setAutoCommit(false);
        try {
            // Prepare SQL Insert Statement
            var stmt = conn.prepareStatement(query);
            // Process In Batches
            Failable.stream(StreamUtils.partition(csvReader.getRecords().stream(), BATCH_SIZE)).forEach(records -> {
                // Clear Batch
                stmt.clearBatch();
                // Compute Batch Parallel
                records.stream().parallel().filter((csvRow) -> csvRow.get(IssuersHeaders.ISS_CNPJ).length() > 0)
                        .sequential().forEach((csvRow) -> {
                            try {
                                // Clear Last params
                                stmt.clearParameters();
                                // Fetch Data from Record
                                var issuerCode = csvRow.get(IssuersHeaders.ISS_CODE);
                                var issuerCnpj = csvRow.get(IssuersHeaders.ISS_CNPJ);
                                // Define Statement Ordinary
                                stmt.setString(1, issuerCode.concat("3"));
                                stmt.setString(2, issuerCnpj);
                                stmt.addBatch();
                                // Define Statement Preferential
                                stmt.setString(1, issuerCode.concat("4"));
                                stmt.setString(2, issuerCnpj);
                                stmt.addBatch();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                // Execute Batch
                stmt.executeBatch();
                // var touchedRows = IntStream.of(stmt.executeBatch()).filter((i) -> i >= 0).sum();
                // Commit Batch
                conn.commit();
                // Log
                // LOGGER.info(String.format("[doRun] Touched %d rows", touchedRows));
            });
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
        }
    }
}
