package frescuratotal.erp.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        String password = System.getenv("ERP_DB_PASSWORD");
        if (password == null) {
            throw new SQLException("Set ERP_DB_PASSWORD in your environment before connecting to MySQL.");
        }

        String url = setting("ERP_DB_URL", null);
        if (url == null) {
            String host = setting("ERP_DB_HOST", "localhost");
            String port = setting("ERP_DB_PORT", "3306");
            String database = setting("ERP_DB_NAME", "erp_frescura_total");
            url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?serverTimezone=America/Bogota";
        }

        return DriverManager.getConnection(url, setting("ERP_DB_USER", "root"), password);
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
