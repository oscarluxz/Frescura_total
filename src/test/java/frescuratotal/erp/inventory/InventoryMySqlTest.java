package frescuratotal.erp.inventory;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.inventory.controller.LotController;
import frescuratotal.erp.inventory.model.Lot;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryMySqlTest {

    @Test public void registersLotsAndRollsBackFailuresAgainstOfficialSchema() throws Exception {
        String schemaPath = System.getenv("ERP_TEST_SCHEMA_FILE");
        Assume.assumeTrue("Configura ERP_TEST_SCHEMA_FILE para habilitar MySQL.", schemaPath != null);
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            // Only an empty, explicitly named test database may receive the schema.
            assertTrue(connection.getCatalog().startsWith("erp_inventory_test_"));
            try (ResultSet tables = connection.getMetaData().getTables(
                    connection.getCatalog(), null, "%", new String[] {"TABLE"})) {
                assertFalse("La base de prueba debe estar vacía.", tables.next());
            }
            String source = Files.readString(Path.of(schemaPath)).replaceAll("(?m)^--.*$", "");
            for (String sql : source.split(";")) {
                String command = sql.trim();
                // Never execute the original DROP DATABASE / CREATE DATABASE / USE.
                if (command.startsWith("CREATE TABLE") || command.startsWith("INSERT INTO")) {
                    statement.execute(command);
                }
            }
            statement.executeUpdate("UPDATE estados_lote SET id_estado = 17 WHERE nombre = 'Disponible'");
            statement.executeUpdate("INSERT INTO proveedores (nombre, nit, email) "
                    + "VALUES ('Prueba', 'TEST-1', 'test@example.invalid')");
            statement.executeUpdate("INSERT INTO productos (nombre, id_categoria, unidad_medida) "
                    + "VALUES ('Prueba', 1, 'kg')");
            statement.executeUpdate("INSERT INTO productos_proveedor VALUES (1, 1)");

            LotController controller = new LotController();
            Lot first = register(controller, 1);
            Lot second = register(controller, 1);
            assertNotEquals(first.getCode(), second.getCode());
            assertTrue(first.getCode().matches("LOT-[0-9]{8}-[0-9]{4,}"));
            assertEquals(17, first.getStatusId());
            try (ResultSet result = statement.executeQuery("SELECT * FROM lotes_inventario ORDER BY id_lote")) {
                assertTrue(result.next());
                assertEquals(first.getCode(), result.getString("numero_lote"));
                assertEquals(0, new BigDecimal("12.50").compareTo(result.getBigDecimal("cantidad")));
                assertEquals(result.getBigDecimal("cantidad"), result.getBigDecimal("cantidad_inicial"));
                assertEquals(0, result.getBigDecimal("precio_compra").signum());
                assertEquals(0, new BigDecimal("99999999.99").compareTo(result.getBigDecimal("precio_venta")));
                assertEquals("A1", result.getString("pasillo"));
                assertNull(result.getString("estante"));
                assertEquals(first.getExpirationDate(), result.getDate("fecha_vencimiento").toLocalDate());
                assertTrue(result.next());
                assertFalse(result.next());
            }
            try {
                register(controller, 999);
                fail("Se esperaba rechazar un proveedor no asociado.");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("no está asociado"));
            }
            // Fail after INSERT to verify that the whole transaction is rolled back.
            statement.execute("CREATE TRIGGER fail_lot_code BEFORE UPDATE ON lotes_inventario "
                    + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Forced update failure'");
            try {
                register(controller, 1);
                fail("Se esperaba un error al asignar el código.");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("Forced update failure"));
            } finally {
                statement.execute("DROP TRIGGER fail_lot_code");
            }
            try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM lotes_inventario")) {
                assertTrue(result.next());
                assertEquals(2, result.getInt(1));
            }
        }
    }

    private Lot register(LotController controller, int supplierId) throws SQLException {
        return controller.registerLot(1, supplierId, LocalDate.now().plusDays(30),
                new BigDecimal("12.50"), BigDecimal.ZERO,
                new BigDecimal("99999999.99"), " A1 ", "   ");
    }
}
