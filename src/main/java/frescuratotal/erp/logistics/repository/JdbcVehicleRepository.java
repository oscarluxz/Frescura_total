package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.logistics.model.Vehicle;
import frescuratotal.erp.logistics.model.VehicleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcVehicleRepository implements VehicleRepository {

    private static final String SELECT =
            "SELECT v.id_vehiculo, v.placa, v.modelo, v.capacidad, v.estado FROM vehiculos v";

    @Override
    public int save(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehiculos (placa, modelo, capacidad, estado) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getPlate());
            ps.setString(2, v.getModel());
            ps.setDouble(3, v.getCapacity());
            ps.setString(4, v.getStatus().getDbName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    @Override
    public void updateStatus(int vehicleId, VehicleStatus status) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE vehiculos SET estado=? WHERE id_vehiculo=?")) {
            ps.setString(1, status.getDbName());
            ps.setInt(2, vehicleId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existsByPlate(String plate) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM vehiculos WHERE placa=?")) {
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Vehicle> findAll() throws SQLException {
        return query(SELECT + " ORDER BY v.placa");
    }

    @Override
    public List<Vehicle> findAvailable() throws SQLException {
        return query(SELECT + " WHERE v.estado = 'Activo' AND NOT EXISTS ("
                + "SELECT 1 FROM asignaciones_entrega a JOIN rutas_entrega r ON r.id_ruta = a.id_ruta "
                + "WHERE a.id_vehiculo = v.id_vehiculo AND r.estado IN ('Planificada', 'En Transito')) "
                + "ORDER BY v.placa");
    }

    private List<Vehicle> query(String sql) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vehicle v = new Vehicle();
                v.setId(rs.getInt("id_vehiculo"));
                v.setPlate(rs.getString("placa"));
                v.setModel(rs.getString("modelo"));
                v.setCapacity(rs.getDouble("capacidad"));
                v.setStatus(VehicleStatus.fromDbName(rs.getString("estado")));
                list.add(v);
            }
        }
        return list;
    }
}