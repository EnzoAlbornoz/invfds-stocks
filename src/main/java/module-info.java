module br.ufsc.investfunds.companies {
    requires java.logging;
    requires java.naming;
    requires java.compiler;
    requires java.net.http;
    requires transitive requery;
    requires requery.processor;
    requires mysql.connector.java;
    requires commons.csv;
    requires json.path;
    requires zip4j;
    requires transitive org.apache.commons.lang3;
    requires transitive java.sql;
    exports br.ufsc.investfunds.companies;
}
