package br.ufsc.investfunds.companies;

// Import Dependencies
import java.net.URI;
import javax.sql.DataSource;
import io.requery.Persistable;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import io.requery.sql.SchemaModifier;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;
import com.mysql.cj.jdbc.MysqlDataSource;

import org.apache.commons.lang3.function.FailableBiConsumer;

import br.ufsc.investfunds.companies.entities.Models;

// Import Entities
// Define Module Functions
public class InvestFundsCompanies {
    // Define Private Store Data Store
    private static EntityDataStore<Persistable> dataStore;
    private static DataSource dataSource;

    // Declare Main Static Functions
    public static void connectDatabase(String dbUrl, String dbName, String dbUser, String dbPassword)
            throws URISyntaxException {
        // Define Info for Connection
        var url = new URI(dbUrl.replace("jdbc:", ""));
        var mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setServerName(url.getHost());
        mysqlDataSource.setPort(url.getPort() != -1 ? url.getPort() : 3306);
        mysqlDataSource.setDatabaseName(dbName);
        mysqlDataSource.setUser(dbUser);
        mysqlDataSource.setPassword(dbPassword);
        // Instantiate Data Store
        dataStore = new EntityDataStore<>(mysqlDataSource, Models.DEFAULT);
        // Store Data Source
        dataSource = mysqlDataSource;
    }

    public static void createEntitiesTables() {
        // Create Schema Modifier
        var schemaModifier = new SchemaModifier(dataSource, Models.DEFAULT);
        // Create Tables
        schemaModifier.createTables(TableCreationMode.CREATE_NOT_EXISTS);
    }

    public static void runProcedure(FailableBiConsumer<EntityDataStore<Persistable>, Path, Exception> procedure,
            Path filePath) throws Exception {
        // Execute Procedure
        procedure.accept(dataStore, filePath);
    }

    public static void close() {
        // Close Data Store
        dataStore.close();
        // Clear References
        dataStore = null;
        dataSource = null;
    }
}
