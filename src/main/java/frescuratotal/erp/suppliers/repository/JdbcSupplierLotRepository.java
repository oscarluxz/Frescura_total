package frescuratotal.erp.suppliers.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.suppliers.model.SupplierLot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcSupplierLotRepository implements SupplierLotRepository {

    private static final String LOTS_WITH_SUPPLIER =
            "SELECT l.id_lote, l.cantidad_inicial, e.nombre AS estado_lote, "
            + "p.id_proveedor, p.nombre AS nombre_proveedor "
            + "FROM lotes_inventario l "
            + "INNER JOIN proveedores p ON l.id_proveedor = p.id_proveedor "
            + "INNER JOIN estados_lote e ON l.id_estado = e.id_estado";

    @Override
    public List<SupplierLot> findAllLots() throws SQLException {
        List<SupplierLot> lots = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(LOTS_WITH_SUPPLIER);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lots.add(new SupplierLot(
                        rs.getInt("id_lote"),
                        rs.getBigDecimal("cantidad_inicial"),
                        rs.getString("estado_lote"),
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor")));
            }
        }
        return lots;
    }
}
