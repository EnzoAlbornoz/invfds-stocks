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
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import br.ufsc.investfunds.companies.entities.PublicCompanyRegister;
import br.ufsc.investfunds.companies.entities.PublicCompanyStockSerie;
import br.ufsc.investfunds.companies.entities.PublicCompanyTicker;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import net.lingala.zip4j.ZipFile;

public class CompanyTickersProcedure {

    enum IssuersHeaders {
        ISS_CODE, ISS_NAME, ISS_CNPJ, ISS_CREATION_DATE
    }

    public static void doDownload(EntityDataStore<Persistable> dataStore, Path cvmFolder) throws URISyntaxException, IOException, InterruptedException {
        var http = HttpClient.newHttpClient();
        // Download File into Memory
        var downloadOptionsURI = new URI("https://sistemaswebb3-listados.b3.com.br/isinProxy/IsinCall/GetTextDownload");
        System.out.println(String.format("[CompanyTickersProcedure] [doDownload] Feching Download options ..."));
        var response = http.send(HttpRequest.newBuilder(downloadOptionsURI).build(), BodyHandlers.ofString());
        // Check Download
        if (response.statusCode() != 200) throw new RuntimeException("Options Issuers - Status Code != 200");
        // Parse Current Hash for Issuers DB
        String issuersDBId = ((Integer) JsonPath.read(response.body(), "$.geralPt.id")).toString();
        issuersDBId = Base64.getEncoder().encodeToString(issuersDBId.getBytes());
        // Download DB File
        var fileZipDownloadPath = File.createTempFile("issuers", ".zip");
        var fileDownloadOptionsURI = new URI("https://sistemaswebb3-listados.b3.com.br/isinProxy/IsinCall/GetFileDownload/".concat(issuersDBId));
        System.out.println(String.format("[CompanyTickersProcedure] [doDownload] Downloading Issuers ..."));
        var dbFileResponse = http.send(HttpRequest.newBuilder(fileDownloadOptionsURI).build(), BodyHandlers.ofFile(fileZipDownloadPath.toPath()));
        if (dbFileResponse.statusCode() != 200) throw new RuntimeException("DBFile Issuers - Status Code != 200");
        System.out.println("[CompanyTickersProcedure] [doDownload] Downloading completed");
        // Create Zip from Stream
        try(var zipFile = new ZipFile(fileZipDownloadPath)) {
            // Create Folder and File
            var issuersFolder = cvmFolder.resolve("STOCK/ISSUERS").toFile();
            issuersFolder.mkdirs();
            // Extract File
            zipFile.extractFile(zipFile.getFileHeader("EMISSOR.TXT"), issuersFolder.getPath());
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
            // Fetch Entities
            var issuer = dataStore.findByKey(PublicCompanyRegister.class, issuerCnpj);
            // Create Entities
            PublicCompanyTicker ordinaryTicker = new PublicCompanyTicker();
            ordinaryTicker.setCompanyRegister(issuer);
            ordinaryTicker.setTicker(issuerCode.concat("3"));
            PublicCompanyTicker preferentialTicker =  new PublicCompanyTicker();
            preferentialTicker.setCompanyRegister(issuer);
            preferentialTicker.setTicker(issuerCode.concat("4"));
            // Return Entities
            return Stream.of(ordinaryTicker, preferentialTicker);
        }).collect(Collectors.filtering((ticker) -> ticker != null, Collectors.toList()));
        // Upsert Tickers
        dataStore.upsert(tickerEntities);
    }
}
