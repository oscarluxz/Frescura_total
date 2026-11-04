package frescuratotal.erp.inventory;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.inventory.model.dao.InventoryJdbc;
import frescuratotal.erp.inventory.model.dao.InventoryQualityDAO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.Before;
import org.junit.Assume;

public class QualityMySqlSupport {
    protected InventoryJdbc jdbc;
    protected InventoryQualityDAO dao;
    protected String database;

    @Before public void createIsolatedDatabase() throws Exception {
        String schema = System.getenv("ERP_TEST_SCHEMA_FILE");
        Assume.assumeTrue("Configura ERP_TEST_SCHEMA_FILE para habilitar MySQL.", schema != null);
        database = "erp_inventory_test_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            if (!connection.getCatalog().startsWith("erp_inventory_test_")) {
                throw new SQLException("Se requiere una conexión a una base de pruebas.");
            }
            statement.execute("CREATE DATABASE " + database + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            connection.setCatalog(database);
            try (Statement setup = connection.createStatement()) {
                executeSchema(setup, Path.of(schema));
                Path migration = Path.of("docs/migrations/001_waste_operations.sql");
                if (Files.exists(migration)) { executeSchema(setup, migration); }
                setup.executeUpdate("INSERT INTO proveedores(nombre,nit,email) VALUES('Proveedor prueba','TEST','test@example.invalid')");
                setup.executeUpdate("INSERT INTO productos(nombre,id_categoria,unidad_medida) VALUES('Producto prueba',1,'kg')");
                setup.executeUpdate("INSERT INTO productos_proveedor VALUES(1,1)");
            }
        }
        jdbc = new InventoryJdbc(() -> {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            connection.setCatalog(database);
            return connection;
        });
        dao = new InventoryQualityDAO(jdbc);
    }

    private void executeSchema(Statement statement, Path path) throws Exception {
        frescuratotal.erp.support.SqlTestDatabase.minimal(statement, path);
    }

    protected int addLot(LocalDate expiration) throws SQLException {
        try (Connection connection = jdbc.open(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO lotes_inventario(numero_lote,id_producto,id_proveedor,fecha_vencimiento,"
                + "cantidad_inicial,cantidad,precio_compra,precio_venta,id_estado) VALUES(?,1,1,?,10,10,5,8,1)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "TEST-" + UUID.randomUUID());
            statement.setDate(2, Date.valueOf(expiration));
            statement.executeUpdate();
            try (ResultSet result = statement.getGeneratedKeys()) { result.next(); return result.getInt(1); }
        }
    }

    protected int count(String table) throws SQLException {
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next(); return result.getInt(1);
        }
    }

    // Historical pending rejections must still support manual, partial waste records.
    protected int addHistoricalRejection(int lot, String reason) throws SQLException {
        try (Connection connection = jdbc.open(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO inspecciones_calidad(id_lote,resultado,nombre_inspector,motivo_rechazo) VALUES(?,'Rechazado','Ana',?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, lot);
            statement.setString(2, reason);
            statement.executeUpdate();
            try (ResultSet result = statement.getGeneratedKeys()) { result.next(); return result.getInt(1); }
        }
    }
}
