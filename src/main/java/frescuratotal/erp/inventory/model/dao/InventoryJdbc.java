package frescuratotal.erp.inventory.model.dao;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.inventory.model.Lot;
import frescuratotal.erp.inventory.model.LotStatus;
import java.sql.*;
import java.time.LocalDate;

public class InventoryJdbc {
    @FunctionalInterface
    public interface ConnectionProvider { Connection open() throws SQLException; }
    @FunctionalInterface
    public interface Transaction<T> { T execute(Connection connection) throws SQLException; }

    private final ConnectionProvider connections;

    public InventoryJdbc() { this(DatabaseConnection.getInstance()::getConnection); }
    public InventoryJdbc(ConnectionProvider connections) { this.connections = connections; }
    public Connection open() throws SQLException { return connections.open(); }

    public <T> T transaction(Transaction<T> action) throws SQLException {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                T result = action.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException error) {
                try { connection.rollback(); }
                catch (SQLException rollbackError) { error.addSuppressed(rollbackError); }
                throw error;
            }
        }
    }

    public Lot lockLot(Connection connection, int lotId) throws SQLException {
        if (lotId <= 0) { throw new IllegalArgumentException("El ID del lote debe ser positivo."); }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM lotes_inventario WHERE id_lote = ? FOR UPDATE")) {
            statement.setInt(1, lotId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) { throw new SQLException("El lote no existe."); }
                return readLot(result);
            }
        }
    }

    public static Lot readLot(ResultSet result) throws SQLException {
        return new Lot(result.getInt("id_lote"), result.getString("numero_lote"),
                result.getInt("id_producto"), result.getInt("id_proveedor"),
                result.getTimestamp("fecha_ingreso").toLocalDateTime(),
                result.getDate("fecha_vencimiento").toLocalDate(),
                result.getBigDecimal("cantidad_inicial"), result.getBigDecimal("cantidad"),
                result.getBigDecimal("precio_compra"), result.getBigDecimal("precio_venta"),
                result.getString("pasillo"), result.getString("estante"), result.getInt("id_estado"));
    }

    public LotStatus status(Connection connection, int statusId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT nombre FROM estados_lote WHERE id_estado = ?")) {
            statement.setInt(1, statusId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) { throw new SQLException("No existe el estado del lote."); }
                return LotStatus.fromDatabaseName(result.getString(1));
            }
        }
    }

    public void setStatus(Connection connection, int lotId, LotStatus status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE lotes_inventario SET id_estado = "
                + "(SELECT id_estado FROM estados_lote WHERE nombre = ?) WHERE id_lote = ?")) {
            statement.setString(1, status.getDatabaseName());
            statement.setInt(2, lotId);
            if (statement.executeUpdate() != 1) { throw new SQLException("No se pudo actualizar el estado."); }
        }
    }

    public LotStatus refreshLocked(Connection connection, Lot lot, LocalDate today) throws SQLException {
        LotStatus current = status(connection, lot.getStatusId());
        // Quarantine remains in force until an approved inspection resolves it.
        if (current == LotStatus.IN_QUARANTINE) { return current; }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM registro_cuarentena WHERE id_lote = ? AND resuelta = FALSE")) {
            statement.setInt(1, lot.getId());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    setStatus(connection, lot.getId(), LotStatus.IN_QUARANTINE);
                    return LotStatus.IN_QUARANTINE;
                }
            }
        }
        LotStatus next = LotStatus.forDate(lot.getExpirationDate(), today);
        setStatus(connection, lot.getId(), next);
        return next;
    }

    public static String requiredText(String value, int limit, String label) {
        if (value == null || value.isBlank() || value.trim().length() > limit) {
            throw new IllegalArgumentException(label + " es obligatorio y admite hasta " + limit + " caracteres.");
        }
        return value.trim();
    }
}
