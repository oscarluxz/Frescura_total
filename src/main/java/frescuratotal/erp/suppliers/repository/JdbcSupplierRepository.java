package frescuratotal.erp.suppliers.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.suppliers.model.CatalogProduct;
import frescuratotal.erp.suppliers.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcSupplierRepository implements SupplierRepository {

    private static final String SELECT_SUPPLIER =
            "SELECT id_proveedor, nombre, nit, persona_contacto, email, telefono, direccion, "
            + "activo, fecha_registro FROM proveedores";

    @Override
    public int save(Supplier s) throws SQLException {
        String sql = "INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getBusinessName());
            ps.setString(2, s.getNit());
            ps.setString(3, s.getContactPerson());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    @Override
    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE proveedores SET nombre=?, nit=?, persona_contacto=?, email=?, telefono=?, "
                + "direccion=? WHERE id_proveedor=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getBusinessName());
            ps.setString(2, s.getNit());
            ps.setString(3, s.getContactPerson());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getAddress());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        }
    }

    /** Suppliers are never physically deleted: lots and purchase history reference them. */
    @Override
    public void setActive(int supplierId, boolean active) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE proveedores SET activo=? WHERE id_proveedor=?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, supplierId);
            ps.executeUpdate();
        }
    }

    @Override
    public Supplier findById(int supplierId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SUPPLIER + " WHERE id_proveedor=?")) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Supplier s = map(rs);
                s.setCatalog(findCatalog(supplierId));
                return s;
            }
        }
    }

    @Override
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SUPPLIER + " ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public boolean existsByNit(String nit) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM proveedores WHERE nit=?")) {
            ps.setString(1, nit);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<CatalogProduct> findCatalog(int supplierId) throws SQLException {
        String sql = "SELECT p.id_producto, p.nombre, p.unidad_medida FROM productos_proveedor pp "
                + "JOIN productos p ON p.id_producto = pp.id_producto WHERE pp.id_proveedor=? ORDER BY p.nombre";
        return queryProducts(sql, supplierId);
    }

    @Override
    public List<CatalogProduct> findAllProducts() throws SQLException {
        return queryProducts("SELECT id_producto, nombre, unidad_medida FROM productos ORDER BY nombre", null);
    }

    @Override
    public void addProductToCatalog(int supplierId, int productId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT IGNORE INTO productos_proveedor (id_proveedor, id_producto) VALUES (?, ?)")) {
            ps.setInt(1, supplierId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public void removeProductFromCatalog(int supplierId, int productId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM productos_proveedor WHERE id_proveedor=? AND id_producto=?")) {
            ps.setInt(1, supplierId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private List<CatalogProduct> queryProducts(String sql, Integer supplierId) throws SQLException {
        List<CatalogProduct> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (supplierId != null) {
                ps.setInt(1, supplierId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CatalogProduct(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
            }
        }
        return list;
    }

    private Supplier map(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id_proveedor"));
        s.setBusinessName(rs.getString("nombre"));
        s.setNit(rs.getString("nit"));
        s.setContactPerson(rs.getString("persona_contacto"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("telefono"));
        s.setAddress(rs.getString("direccion"));
        s.setActive(rs.getBoolean("activo"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        s.setRegistrationDate(ts);
        return s;
    }
}