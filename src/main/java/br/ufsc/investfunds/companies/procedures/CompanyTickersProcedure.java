package br.ufsc.investfunds.companies.procedures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import br.ufsc.investfunds.companies.entities.PublicCompanyTicker;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.headers.FileHeaderFactory;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;

public class CompanyTickersProcedure {

    enum IssuersHeaders {
        ISS_CODE, ISS_NAME, ISS_CNPJ, ISS_CREATION_DATE
    }

    public static void doDownload(Path cvmFolder) throws URISyntaxException, IOException, InterruptedException {
        // Download File into Memory
        var downloadOptionsURI = new URI("https://sistemaswebb3-listados.b3.com.br/isinProxy/IsinCall/GetTextDownload");
        var http = HttpClient.newHttpClient();
        var response = http.send(HttpRequest.newBuilder(downloadOptionsURI).build(), BodyHandlers.ofString());
        // Check Download
        if (response.statusCode() != 200) throw new RuntimeException("Options Issuers - Status Code != 200");
        // Parse Current Hash for Issuers DB
        String issuersDBId = JsonPath.read(response.toString(), "$.geralPt.id");
        issuersDBId = Base64.getEncoder().encodeToString(issuersDBId.getBytes());
        // Download DB File
        var fileZipDownloadPath = File.createTempFile(null, ".zip");
        var fileDownloadOptionsURI = new URI("https://sistemaswebb3-listados.b3.com.br/isinProxy/IsinCall/GetFileDownload/".concat(issuersDBId));
        var dbFileResponse = http.send(HttpRequest.newBuilder(fileDownloadOptionsURI).build(), BodyHandlers.ofFile(fileZipDownloadPath.toPath()));
        if (dbFileResponse.statusCode() != 200) throw new RuntimeException("DBFile Issuers - Status Code != 200");
        // Create Zip from Stream
        try(var zipFile = new ZipFile(fileZipDownloadPath)) {
            var zipIssuerStream = zipFile.getInputStream(zipFile.getFileHeader("EMISSOR.TXT"));
            // Create Folder and File
            var issuersFile = cvmFolder.resolve("./STOCK/issuers.csv").toFile();
            issuersFile.mkdirs();
            // Extract File Content to Path
            try(var issuersFileOutputStream = new FileOutputStream(issuersFile)) {
                zipIssuerStream.transferTo(issuersFileOutputStream);
            }
        }
    }

    public static void doRun(EntityDataStore<Persistable> dataStore, Path filePath) throws IOException {
        // Read Files
        var csvReader = CSVParser.parse(filePath, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withHeader(IssuersHeaders.class));
        // Register Records
        var tickerEntities = csvReader.getRecords().stream().parallel().flatMap(csvRow -> {
            // Fetch Data from Record
            var issuerCnpj = csvRow.get(IssuersHeaders.ISS_CODE);
            var issuerCode = csvRow.get(IssuersHeaders.ISS_CODE);
            // Create Entities
            var ordinaryTicker = new PublicCompanyTicker(issuerCnpj, issuerCode.concat("3"));
            var preferentialTicker = new PublicCompanyTicker(issuerCnpj, issuerCode.concat("4"));
            // Return Entities
            return Stream.of(ordinaryTicker, preferentialTicker);
        }).collect(Collectors.toList());
        // Upsert Tickers
        dataStore.upsert(tickerEntities);
    }
}
