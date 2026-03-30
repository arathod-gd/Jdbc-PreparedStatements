package org.databaseiep.sqlconnection;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConn {
    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public Connection getConnection() throws SQLException {
        String host = getValue("POSTGRES_HOST");
        String user = getValue("POSTGRES_USER");
        String port = getValue("POSTGRES_PORT");
        String password = getValue("POSTGRES_PASSWORD");
        String db = getValue("POSTGRES_DB");
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

        return DriverManager.getConnection(url, user, password);
    }

    private String getValue(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return value;
    }
}
