module br.ufsc.investfunds.companies {
    requires java.logging;
    requires java.naming;
    requires java.compiler;
    requires java.net.http;
    requires commons.csv;
    requires org.json;
    requires zip4j;
    requires transitive org.apache.commons.lang3;
    requires transitive java.sql;
    exports br.ufsc.investfunds.companies;
}
