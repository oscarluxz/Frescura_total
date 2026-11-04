package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.logistics.model.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcDriverRepository implements DriverRepository {

    private static final String SELECT =
            "SELECT d.id_conductor, d.nombre, d.numero_licencia, d.telefono, d.email, d.activo FROM conductores d";

    @Override
    public int save(Driver d) throws SQLException {
        String sql = "INSERT INTO conductores (nombre, numero_licencia, telefono, email) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getLicenseNumber());
            ps.setString(3, d.getPhone());
            ps.setString(4, d.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    @Override
    public void setActive(int driverId, boolean active) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE conductores SET activo=? WHERE id_conductor=?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, driverId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existsByLicense(String licenseNumber) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM conductores WHERE numero_licencia=?")) {
            ps.setString(1, licenseNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Driver> findAll() throws SQLException {
        return query(SELECT + " ORDER BY d.nombre");
    }

    @Override
    public List<Driver> findAvailable() throws SQLException {
        return query(SELECT + " WHERE d.activo = TRUE AND NOT EXISTS ("
                + "SELECT 1 FROM asignaciones_entrega a JOIN rutas_entrega r ON r.id_ruta = a.id_ruta "
                + "WHERE a.id_conductor = d.id_conductor AND r.estado IN ('Planificada', 'En Transito')) "
                + "ORDER BY d.nombre");
    }

    private List<Driver> query(String sql) throws SQLException {
        List<Driver> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Driver d = new Driver();
                d.setId(rs.getInt("id_conductor"));
                d.setName(rs.getString("nombre"));
                d.setLicenseNumber(rs.getString("numero_licencia"));
                d.setPhone(rs.getString("telefono"));
                d.setEmail(rs.getString("email"));
                d.setActive(rs.getBoolean("activo"));
                list.add(d);
            }
        }
        return list;
    }
}