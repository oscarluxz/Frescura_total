package frescuratotal.erp.inventory.model.dao;

import frescuratotal.erp.inventory.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TraceabilityDAO {
    private final InventoryJdbc jdbc;
    private static final String LOT_DETAILS = "SELECT l.*,p.nombre AS producto,s.nombre AS proveedor,"
            + "s.nit AS nit_proveedor,s.email AS email_proveedor,e.nombre AS estado "
            + "FROM lotes_inventario l JOIN productos p ON p.id_producto=l.id_producto "
            + "JOIN proveedores s ON s.id_proveedor=l.id_proveedor JOIN estados_lote e ON e.id_estado=l.id_estado ";

    public TraceabilityDAO() { this(new InventoryJdbc()); }
    public TraceabilityDAO(InventoryJdbc jdbc) { this.jdbc = jdbc; }

    public LotTrace traceLot(int lotId) throws SQLException {
        return jdbc.transaction(connection -> {
            jdbc.refreshLocked(connection, jdbc.lockLot(connection, lotId), InventoryQualityDAO.today());
            try (PreparedStatement statement = connection.prepareStatement(LOT_DETAILS + "WHERE l.id_lote=?")) {
                statement.setInt(1, lotId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) { throw new SQLException("El lote no existe."); }
                    return readTrace(result, customers(connection, lotId));
                }
            }
        });
    }

    public List<LotTrace> inventory() throws SQLException { return findLots(null, null); }
    public List<LotTrace> lotsBySupplier(int supplierId) throws SQLException {
        if (supplierId <= 0) { throw new IllegalArgumentException("El ID del proveedor debe ser positivo."); }
        return findLots(supplierId, null);
    }
    public List<LotTrace> lotsByCustomer(int customerId) throws SQLException {
        if (customerId <= 0) { throw new IllegalArgumentException("El ID del cliente debe ser positivo."); }
        return findLots(null, customerId);
    }

    private List<LotTrace> findLots(Integer supplierId, Integer customerId) throws SQLException {
        new InventoryQualityDAO(jdbc).refreshStatuses(InventoryQualityDAO.today());
        String filter = supplierId != null ? "WHERE l.id_proveedor=? " : customerId != null
                ? "WHERE EXISTS (SELECT 1 FROM detalles_venta d JOIN ventas v ON v.id_venta=d.id_venta "
                  + "JOIN estados_venta ev ON ev.id_estado=v.id_estado WHERE d.id_lote=l.id_lote AND v.id_cliente=? AND ev.nombre='Entregada') " : "";
        List<LotTrace> lots = new ArrayList<>();
        try (Connection connection = jdbc.open(); PreparedStatement statement = connection.prepareStatement(
                LOT_DETAILS + filter + "ORDER BY l.fecha_vencimiento,l.id_lote")) {
            if (supplierId != null) { statement.setInt(1, supplierId); }
            else if (customerId != null) { statement.setInt(1, customerId); }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) { lots.add(readTrace(result, List.of())); }
            }
        }
        return lots;
    }

    private List<LotCustomer> customers(Connection connection, int lotId) throws SQLException {
        List<LotCustomer> customers = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT c.id_cliente,c.nombre,c.documento,SUM(d.cantidad_vendida) AS entregada "
                + "FROM detalles_venta d JOIN ventas v ON v.id_venta=d.id_venta "
                + "JOIN clientes c ON c.id_cliente=v.id_cliente JOIN estados_venta e ON e.id_estado=v.id_estado "
                + "WHERE d.id_lote=? AND e.nombre='Entregada' GROUP BY c.id_cliente,c.nombre,c.documento ORDER BY c.id_cliente")) {
            statement.setInt(1, lotId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) { customers.add(new LotCustomer(result.getInt(1), result.getString(2), result.getString(3), result.getBigDecimal(4))); }
            }
        }
        return customers;
    }

    private LotTrace readTrace(ResultSet result, List<LotCustomer> customers) throws SQLException {
        return new LotTrace(InventoryJdbc.readLot(result), result.getString("producto"), result.getString("proveedor"),
                result.getString("nit_proveedor"), result.getString("email_proveedor"),
                LotStatus.fromDatabaseName(result.getString("estado")), customers);
    }
}
