package br.ufsc.investfunds.companies.procedures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import br.ufsc.investfunds.companies.entities.EPublicCompanyRegisterStatus;
import br.ufsc.investfunds.companies.entities.PublicCompanyRegister;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class CompanyRegisterProcedure {
    public static void doRun(EntityDataStore<Persistable> dataStore, Path filePath) throws IOException {
        // Read from File
        var csvReader = CSVParser.parse(filePath, StandardCharsets.ISO_8859_1,
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader());
        // For each row, try to add it to table
        dataStore.runInTransaction(() -> {
            for (var record : csvReader.getRecords()) {
                // Create Row
                var companyRegister = new PublicCompanyRegister();
                // Load CNPJ
                var sanitizedCnpj = record.get("CNPJ_CIA").replaceAll("\\D", "");
                companyRegister.setCnpj(sanitizedCnpj);
                // Load Social Name
                companyRegister.setSocialName(record.get(PublicCompanyRegister.SOCIAL_NAME.getName()));
                // Load Status
                companyRegister.setStatus(
                        EPublicCompanyRegisterStatus.fromString(record.get(PublicCompanyRegister.STATUS.getName())));
                // Persist In DB
                dataStore.upsert(companyRegister);
            }
            return null;
        });
    }
}
