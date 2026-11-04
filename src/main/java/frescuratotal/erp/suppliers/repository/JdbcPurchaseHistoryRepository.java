package frescuratotal.erp.suppliers.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.suppliers.model.PurchaseRecord;
import frescuratotal.erp.suppliers.model.SupplierReliability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcPurchaseHistoryRepository implements PurchaseHistoryRepository {

    @Override
    public List<PurchaseRecord> findBySupplier(int supplierId) throws SQLException {
        String sql = "SELECT l.id_lote, l.numero_lote, l.id_proveedor, p.nombre AS producto, "
                + "l.fecha_ingreso, l.fecha_vencimiento, l.cantidad_inicial, l.cantidad, "
                + "l.precio_compra, e.nombre AS estado "
                + "FROM lotes_inventario l "
                + "JOIN productos p ON p.id_producto = l.id_producto "
                + "JOIN estados_lote e ON e.id_estado = l.id_estado "
                + "WHERE l.id_proveedor = ? ORDER BY l.fecha_ingreso DESC";
        List<PurchaseRecord> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PurchaseRecord(
                            rs.getInt("id_lote"),
                            rs.getString("numero_lote"),
                            rs.getInt("id_proveedor"),
                            rs.getString("producto"),
                            rs.getTimestamp("fecha_ingreso"),
                            rs.getDate("fecha_vencimiento"),
                            rs.getDouble("cantidad_inicial"),
                            rs.getDouble("cantidad"),
                            rs.getDouble("precio_compra"),
                            rs.getString("estado")));
                }
            }
        }
        return list;
    }

    @Override
    public SupplierReliability calculateReliability(int supplierId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_lots, COALESCE(SUM(l.cantidad_inicial), 0) AS total_qty, "
                + "COALESCE((SELECT SUM(m.cantidad_perdida) FROM registro_mermas m "
                + "          JOIN lotes_inventario l2 ON l2.id_lote = m.id_lote "
                + "          WHERE l2.id_proveedor = ?), 0) AS wasted_qty, "
                + "(SELECT COUNT(*) FROM inspecciones_calidad i "
                + "          JOIN lotes_inventario l3 ON l3.id_lote = i.id_lote "
                + "          WHERE l3.id_proveedor = ? AND i.resultado = 'Rechazado') AS rejected "
                + "FROM lotes_inventario l WHERE l.id_proveedor = ?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, supplierId);
            ps.setInt(3, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new SupplierReliability(supplierId, rs.getInt("total_lots"),
                        rs.getDouble("total_qty"), rs.getDouble("wasted_qty"),
                        rs.getInt("rejected"));
            }
        }
    }
}