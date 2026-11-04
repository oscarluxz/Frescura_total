package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.model.LotTrace;
import frescuratotal.erp.inventory.model.dao.TraceabilityDAO;
import java.math.BigDecimal;
import java.sql.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TraceabilityMySqlTest extends QualityMySqlSupport {
    @Test public void followsSupplierAndDeliveredCustomerRelationsInBothDirections() throws Exception {
        int lot = addLot(dao.today().plusDays(5));
        int other = addLot(dao.today().plusDays(20));
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO clientes(nombre,documento,id_tipo) VALUES('Cliente prueba','DOC-1',1)");
            statement.executeUpdate("INSERT INTO ventas(numero_venta,id_cliente,subtotal,total_descuento,total_impuesto,monto_total,id_estado) "
                    + "SELECT CONCAT('V-',id_estado),1,10,0,0,10,id_estado FROM estados_venta WHERE nombre IN ('Entregada','Cancelada','Registrada')");
            statement.executeUpdate("INSERT INTO detalles_venta(id_venta,id_lote,cantidad_vendida,precio_unitario,subtotal,total_linea) "
                    + "SELECT id_venta," + lot + ",2,5,10,10 FROM ventas");
        }
        TraceabilityDAO trace = new TraceabilityDAO(jdbc);
        LotTrace result = trace.traceLot(lot);
        assertEquals("Proveedor prueba", result.getSupplierName());
        assertEquals("TEST", result.getSupplierNit());
        assertEquals("Producto prueba", result.getProductName());
        assertEquals(1, result.getCustomers().size());
        assertEquals("DOC-1", result.getCustomers().get(0).getDocument());
        assertEquals(0, new BigDecimal("2").compareTo(result.getCustomers().get(0).getDeliveredQuantity()));
        assertEquals(1, trace.lotsByCustomer(1).size());
        assertEquals(lot, trace.lotsByCustomer(1).get(0).getLot().getId());
        assertEquals(2, trace.lotsBySupplier(1).size());
        assertTrue(trace.traceLot(other).getCustomers().isEmpty());
        assertEquals(2, trace.inventory().size());
        assertTrue(trace.lotsBySupplier(999).isEmpty());
        try { trace.traceLot(999); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("no existe")); }
    }
}
