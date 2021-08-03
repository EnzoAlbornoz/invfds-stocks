package br.ufsc.investfunds.companies;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
// Import Dependencies
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.function.FailableBiConsumer;
// Import Entities
// Define Module Functions
public class InvestFundsCompanies {
    // Define Private Store Data Store
    static Connection mainConnection;
    // Declare Main Static Functions
    public static void createEntitiesTables(Connection connection) throws SQLException, IOException {
        // Save Connection
        mainConnection = connection;
        // Load File
        var createTablesText = new String(InvestFundsCompanies.class.getResourceAsStream("/sql/createTables.sql").readAllBytes(), "UTF-8");
        // var createTablesStatements
        var statementTexts = createTablesText.split(";\n");
        // Create Tables
        var createTablesStatement = connection.createStatement();
        for (var statementText: statementTexts) {
            createTablesStatement.addBatch(statementText);
        }
        createTablesStatement.executeBatch();
        createTablesStatement.close();
    }

    public static void runProcedure(FailableBiConsumer<Connection, Path, Exception> procedure,
            Connection connection,
            Path filePath) throws Exception {
        // Execute Procedure
        procedure.accept(connection, filePath);
    }

    // public static void main(String[] args) throws IOException {
    //     var file = InvestFundsCompanies.class.getResource("/sql/createTables.sql");
    //     System.out.println(file);
    //     // var str = String.valueOf(file.readAllBytes());
    //     // System.out.println(str);
    // }
}
