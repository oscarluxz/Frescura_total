package frescuratotal.erp.inventory;

import frescuratotal.erp.support.SqlTestDatabase;
import java.nio.file.Path;
import java.sql.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ConsolidatedDatabaseMySqlTest {
    @Test public void loadsCompleteDeliveryDataWithCoherentStock() throws Exception {
        String schema = System.getenv("ERP_TEST_SCHEMA_FILE");
        Assume.assumeTrue(schema != null);
        try (Connection connection = SqlTestDatabase.create()) {
            SqlTestDatabase.consolidated(connection, Path.of(schema));
            try (Statement statement = connection.createStatement()) {
                for (String table : new String[]{"proveedores", "productos", "clientes", "lotes_inventario", "ventas", "detalles_venta", "facturas"}) {
                    try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                        assertTrue(result.next());
                        assertTrue(table, result.getInt(1) >= 50);
                    }
                }
                try (ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM lotes_inventario l WHERE cantidad < 0 OR cantidad <> cantidad_inicial - COALESCE((SELECT SUM(cantidad_perdida) FROM registro_mermas m WHERE m.id_lote=l.id_lote),0) - COALESCE((SELECT SUM(cantidad_vendida) FROM detalles_venta d WHERE d.id_lote=l.id_lote),0)")) {
                    assertTrue(result.next());
                    assertEquals(0, result.getInt(1));
                }
            }
        }
    }
}
