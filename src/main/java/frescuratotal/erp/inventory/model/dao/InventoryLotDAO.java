package frescuratotal.erp.inventory.model.dao;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.inventory.model.Lot;
import frescuratotal.erp.inventory.model.LotStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventoryLotDAO {

    public Lot save(int productId, int supplierId, LocalDate expirationDate,
                    BigDecimal quantity, BigDecimal purchasePrice,
                    BigDecimal salePrice, String aisle, String shelf)
            throws SQLException {
        LocalDateTime receivedAt = LocalDateTime.now();
        String insertSql = "INSERT INTO lotes_inventario "
                + "(numero_lote, id_producto, id_proveedor, fecha_ingreso, "
                + "fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, "
                + "precio_venta, pasillo, estante, id_estado) "
                + "VALUES (CONCAT('TMP-', UUID()), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int statusId = findInitialStatusId(connection, expirationDate);
                validateSupplierProduct(connection, supplierId, productId);

                int lotId;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, productId);
                    statement.setInt(2, supplierId);
                    statement.setTimestamp(3, Timestamp.valueOf(receivedAt));
                    statement.setDate(4, Date.valueOf(expirationDate));
                    statement.setBigDecimal(5, quantity);
                    statement.setBigDecimal(6, quantity);
                    statement.setBigDecimal(7, purchasePrice);
                    statement.setBigDecimal(8, salePrice);
                    statement.setString(9, aisle);
                    statement.setString(10, shelf);
                    statement.setInt(11, statusId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("No se pudo registrar el lote.");
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se obtuvo el ID del lote.");
                        }
                        lotId = keys.getInt(1);
                    }
                }

                String code = "LOT-"
                        + receivedAt.format(DateTimeFormatter.BASIC_ISO_DATE)
                        + "-" + String.format("%04d", lotId);
                String updateSql = "UPDATE lotes_inventario "
                        + "SET numero_lote = ? WHERE id_lote = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setString(1, code);
                    statement.setInt(2, lotId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("No se pudo asignar el código del lote.");
                    }
                }

                Lot lot = new Lot(lotId, code, productId, supplierId,
                        receivedAt, expirationDate, quantity, quantity,
                        purchasePrice, salePrice, aisle, shelf, statusId);
                connection.commit();
                return lot;
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
        }
    }

    private int findInitialStatusId(Connection connection, LocalDate expirationDate) throws SQLException {
        String sql = "SELECT id_estado FROM estados_lote WHERE nombre = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LotStatus.forDate(expirationDate, InventoryQualityDAO.today()).getDatabaseName());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Falta el estado inicial del lote en estados_lote.");
                }
                return result.getInt("id_estado");
            }
        }
    }

    private void validateSupplierProduct(Connection connection, int supplierId,
                                         int productId) throws SQLException {
        String sql = "SELECT 1 FROM productos_proveedor "
                + "WHERE id_proveedor = ? AND id_producto = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierId);
            statement.setInt(2, productId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "El producto no está asociado al proveedor seleccionado.");
                }
            }
        }
    }
}
