package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.logistics.model.DispatchOrder;
import frescuratotal.erp.logistics.model.DispatchStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcDispatchOrderRepository implements DispatchOrderRepository {

    @Override
    public List<DispatchOrder> findInPreparation() throws SQLException {
        // LEFT JOIN: orders without an active route still appear (id_ruta is NULL).
        // Cancelled routes are ignored, so their orders show as unassigned again.
        String sql = "SELECT v.id_venta, v.numero_venta, c.nombre, c.direccion, v.fecha_venta, "
                + "v.monto_total, r.id_ruta, r.nombre_ruta "
                + "FROM ventas v "
                + "JOIN clientes c ON c.id_cliente = v.id_cliente "
                + "JOIN estados_venta e ON e.id_estado = v.id_estado "
                + "LEFT JOIN ventas_ruta vr ON vr.id_venta = v.id_venta "
                + "     AND vr.id_ruta IN (SELECT id_ruta FROM rutas_entrega WHERE estado <> 'Cancelada') "
                + "LEFT JOIN rutas_entrega r ON r.id_ruta = vr.id_ruta "
                + "WHERE e.nombre = ? "
                + "ORDER BY v.fecha_venta";
        List<DispatchOrder> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, DispatchStatus.IN_PREPARATION.getDbName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // getInt returns 0 when the column is NULL, which means "no route".
                    list.add(new DispatchOrder(
                            rs.getInt("id_venta"),
                            rs.getString("numero_venta"),
                            rs.getString("nombre"),
                            rs.getString("direccion"),
                            rs.getTimestamp("fecha_venta"),
                            rs.getDouble("monto_total"),
                            DispatchStatus.IN_PREPARATION,
                            rs.getInt("id_ruta"),
                            rs.getString("nombre_ruta")));
                }
            }
        }
        return list;
    }
}
