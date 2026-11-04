package frescuratotal.erp.inventory;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.inventory.model.dao.InventoryJdbc;
import frescuratotal.erp.inventory.model.dao.InventoryQualityDAO;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryDmlMySqlTest {
    private static final Set<String> INVENTORY_TABLES = Set.of("productos", "productos_proveedor",
            "lotes_inventario", "registro_cuarentena", "inspecciones_calidad", "registro_mermas", "operaciones_merma");
    private static final Pattern INSERT = Pattern.compile("INSERT INTO (\\w+) \\(([^)]+)\\) VALUES[\\s\\S]+", Pattern.CASE_INSENSITIVE);

    @Test public void containsOnlyInventoryInsertsWithExplicitIds() throws Exception {
        Set<String> tables = new HashSet<>();
        List<String> statements = statements(Files.readString(Path.of("docs/dml_inventory.sql")));
        assertEquals(7, statements.size());
        for (String sql : statements) {
            Matcher match = INSERT.matcher(sql);
            assertTrue("Se esperaba únicamente INSERT con columnas explícitas.", match.matches());
            assertTrue(INVENTORY_TABLES.contains(match.group(1)));
            assertTrue(match.group(2).contains("id_"));
            tables.add(match.group(1));
        }
        assertEquals(INVENTORY_TABLES, tables);
    }

    @Test public void loadsWithOscarSuppliersAndKeepsStockAndQualityConsistent() throws Exception {
        String schemaFile = System.getenv("ERP_TEST_SCHEMA_FILE");
        String suppliersFile = System.getenv("ERP_TEST_SUPPLIERS_FILE");
        Assume.assumeTrue("Configura el SQL base y el DML real de proveedores para esta prueba.",
                schemaFile != null && suppliersFile != null);
        String database = "erp_inventory_test_seed_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assertTrue(connection.getCatalog().startsWith("erp_inventory_test_"));
            try (Statement admin = connection.createStatement()) {
                admin.execute("CREATE DATABASE " + database + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            connection.setCatalog(database);
            try (Statement setup = connection.createStatement()) {
                for (String sql : statements(Files.readString(Path.of(schemaFile)))) {
                    if (sql.startsWith("CREATE TABLE") || sql.startsWith("INSERT INTO")) { setup.execute(sql); }
                }
            }
            connection.setAutoCommit(false);
            try (Statement setup = connection.createStatement()) {
                for (String sql : statements(Files.readString(Path.of(suppliersFile)))) {
                    Matcher match = INSERT.matcher(sql);
                    assertTrue("El archivo de proveedores solo debe contener INSERT.", match.matches());
                    assertTrue(Set.of("proveedores", "conductores", "vehiculos").contains(match.group(1)));
                    setup.executeUpdate(sql);
                }
                for (String sql : statements(Files.readString(Path.of("docs/dml_inventory.sql")))) { setup.executeUpdate(sql); }
                assertEquals(50, scalar(setup, "SELECT COUNT(*) FROM productos"));
                assertEquals(50, scalar(setup, "SELECT COUNT(*) FROM productos_proveedor"));
                assertEquals(80, scalar(setup, "SELECT COUNT(*) FROM lotes_inventario"));
                assertEquals(50, scalar(setup, "SELECT COUNT(*) FROM registro_cuarentena"));
                assertEquals(75, scalar(setup, "SELECT COUNT(*) FROM inspecciones_calidad"));
                assertEquals(50, scalar(setup, "SELECT COUNT(*) FROM registro_mermas"));
                assertEquals(50, scalar(setup, "SELECT COUNT(*) FROM operaciones_merma"));
                assertEquals(25, scalar(setup, "SELECT COUNT(*) FROM registro_cuarentena WHERE resuelta=FALSE"));
                assertEquals(0, scalar(setup, "SELECT COUNT(*) FROM lotes_inventario l LEFT JOIN "
                        + "(SELECT id_lote,SUM(cantidad_perdida) perdida FROM registro_mermas GROUP BY id_lote) m "
                        + "ON m.id_lote=l.id_lote WHERE l.cantidad<>l.cantidad_inicial-COALESCE(m.perdida,0) OR l.cantidad<0"));
                assertEquals(0, scalar(setup, "SELECT COUNT(*) FROM lotes_inventario l LEFT JOIN productos_proveedor p "
                        + "ON p.id_producto=l.id_producto AND p.id_proveedor=l.id_proveedor WHERE p.id_producto IS NULL"));
                assertEquals(0, scalar(setup, "SELECT COUNT(*) FROM lotes_inventario l JOIN proveedores p "
                        + "ON p.id_proveedor=l.id_proveedor WHERE p.activo=FALSE"));
                assertEquals(0, scalar(setup, "SELECT COUNT(*) FROM lotes_inventario l JOIN estados_lote e ON e.id_estado=l.id_estado "
                        + "WHERE e.nombre<>CASE WHEN EXISTS(SELECT 1 FROM registro_cuarentena q WHERE q.id_lote=l.id_lote AND q.resuelta=FALSE) "
                        + "THEN 'En Cuarentena' WHEN l.fecha_vencimiento<=CURRENT_DATE THEN 'Vencido' "
                        + "WHEN l.fecha_vencimiento<=DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY) THEN 'Proximo a Vencer' ELSE 'Disponible' END"));
                assertEquals(0, scalar(setup, "SELECT COUNT(*) FROM registro_mermas m JOIN inspecciones_calidad i "
                        + "ON i.id_inspeccion=m.id_inspeccion WHERE i.id_lote<>m.id_lote OR i.resultado<>'Rechazado' "
                        + "OR i.fecha_inspeccion>m.fecha_registro OR m.cantidad_perdida<=0"));
                InventoryQualityDAO quality = new InventoryQualityDAO();
                quality.assertSellable(connection, 1004, BigDecimal.ONE);
                quality.assertSellable(connection, 1001, BigDecimal.ONE);
                expectBlocked(quality, connection, 1002);
                expectBlocked(quality, connection, 1026);
                expectBlocked(quality, connection, 1030);
                assertEquals(0, scalar(setup, "SELECT cantidad FROM lotes_inventario WHERE id_lote=1030"));
            } catch (SQLException | RuntimeException | AssertionError error) {
                connection.rollback();
                throw error;
            }
            connection.commit();
        }
        InventoryQualityDAO quality = new InventoryQualityDAO(new InventoryJdbc(() -> {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            connection.setCatalog(database);
            return connection;
        }));
        assertEquals(1001, quality.recordWaste(1001, 1001, new BigDecimal("2.00"),
                "Empaque deteriorado", "Junior - datos de prueba", "SEED-JUNIOR-MERMA-1001"));
    }

    private void expectBlocked(InventoryQualityDAO quality, Connection connection, int lotId) throws SQLException {
        try { quality.assertSellable(connection, lotId, BigDecimal.ONE); fail("Se esperaba bloquear el lote " + lotId); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("bloqueado")); }
    }

    private int scalar(Statement statement, String sql) throws SQLException {
        try (ResultSet result = statement.executeQuery(sql)) { assertTrue(result.next()); return result.getInt(1); }
    }

    // Split SQL without treating semicolons inside quoted text as statement ends.
    private static List<String> statements(String source) {
        String sql = source.replaceAll("(?m)^--.*$", "");
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < sql.length(); index++) {
            char character = sql.charAt(index);
            if (character == '\'') {
                if (quoted && index + 1 < sql.length() && sql.charAt(index + 1) == '\'') {
                    current.append("''"); index++; continue;
                }
                quoted = !quoted;
            }
            if (character == ';' && !quoted) {
                if (!current.toString().isBlank()) { result.add(current.toString().trim()); }
                current.setLength(0);
            } else { current.append(character); }
        }
        assertFalse("Cadena SQL sin cerrar.", quoted);
        if (!current.toString().isBlank()) { result.add(current.toString().trim()); }
        return result;
    }
}
