package frescuratotal.erp.support;

import frescuratotal.erp.common.DatabaseConnection;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

public final class SqlTestDatabase {
    private static final Set<String> CATALOGS = Set.of("estados_lote", "tipos_cliente", "estados_venta", "informacion_empresa", "categorias_producto");
    private SqlTestDatabase() { }

    public static Connection create() throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        if (connection.getCatalog() == null || !connection.getCatalog().startsWith("erp_inventory_test_")) {
            connection.close();
            throw new SQLException("Las pruebas requieren una base con prefijo erp_inventory_test_.");
        }
        String database = "erp_inventory_test_" + UUID.randomUUID().toString().replace("-", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + database + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        connection.setCatalog(database);
        return connection;
    }

    public static void minimal(Statement statement, Path path) throws Exception {
        for (String sql : statements(Files.readString(path))) {
            Matcher insert = Pattern.compile("(?is)^INSERT\\s+INTO\\s+(\\w+).*").matcher(sql);
            if (sql.startsWith("CREATE TABLE") || (insert.matches() && CATALOGS.contains(insert.group(1)))) {
                statement.execute(sql);
            }
        }
    }

    public static void consolidated(Connection connection, Path path) throws Exception {
        if (!connection.getCatalog().startsWith("erp_inventory_test_")) { throw new SQLException("Se requiere base de prueba."); }
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements(Files.readString(path))) {
                if (sql.startsWith("CREATE DATABASE") || sql.startsWith("USE ")) { continue; }
                if (!sql.matches("(?is)^(CREATE TABLE|INSERT INTO|UPDATE |START TRANSACTION|COMMIT)[^;]*.*")) {
                    throw new SQLException("Sentencia no admitida en el consolidado de pruebas: " + sql.substring(0, Math.min(sql.length(), 80)));
                }
                statement.execute(sql);
            }
        }
    }

    public static List<String> statements(String source) {
        source = source.replace("\uFEFF", "");
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false, lineComment = false, blockComment = false;
        for (int index = 0; index < source.length(); index++) {
            char value = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';
            if (lineComment) { if (value == '\n') { lineComment = false; current.append('\n'); } continue; }
            if (blockComment) { if (value == '*' && next == '/') { blockComment = false; index++; current.append(' '); } continue; }
            if (!quoted && value == '-' && next == '-' && (index + 2 == source.length() || Character.isWhitespace(source.charAt(index + 2)))) { lineComment = true; index++; continue; }
            if (!quoted && value == '#') { lineComment = true; continue; }
            if (!quoted && value == '/' && next == '*') { blockComment = true; index++; continue; }
            if (quoted && value == '\\' && next != '\0') { current.append(value).append(next); index++; continue; }
            if (value == '\'') {
                if (quoted && next == '\'') { current.append("''"); index++; continue; }
                quoted = !quoted;
            }
            if (value == ';' && !quoted) {
                if (!current.toString().isBlank()) { result.add(current.toString().trim()); }
                current.setLength(0);
            } else { current.append(value); }
        }
        if (quoted || blockComment) { throw new IllegalArgumentException("SQL sin cerrar."); }
        if (!current.toString().isBlank()) { result.add(current.toString().trim()); }
        return result;
    }
}
