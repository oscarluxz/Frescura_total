package frescuratotal.erp.inventory.model.dao;

import frescuratotal.erp.inventory.model.Lot;
import frescuratotal.erp.inventory.model.LotStatus;
import frescuratotal.erp.inventory.model.Inspection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class InventoryQualityDAO {
    private final InventoryJdbc jdbc;

    public InventoryQualityDAO() { this(new InventoryJdbc()); }
    public InventoryQualityDAO(InventoryJdbc jdbc) { this.jdbc = jdbc; }

    public static LocalDate today() { return LocalDate.now(ZoneId.of("America/Bogota")); }

    public int recordWaste(int lotId, Integer inspectionId, BigDecimal quantity,
                          String reason, String responsible, String operationKey) throws SQLException {
        if (quantity == null || quantity.signum() <= 0
                || quantity.stripTrailingZeros().scale() > 2
                || quantity.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("La merma debe ser positiva y respetar DECIMAL(10,2).");
        }
        if (inspectionId != null && inspectionId <= 0) { throw new IllegalArgumentException("El ID de inspección debe ser positivo."); }
        String checkedReason = InventoryJdbc.requiredText(reason, 10000, "La causa");
        String checkedUser = InventoryJdbc.requiredText(responsible, 100, "El responsable");
        String key = InventoryJdbc.requiredText(operationKey, 64, "La clave de operación");
        if (!key.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalArgumentException("La clave admite letras sin tildes, números, punto, guion, guion bajo y dos puntos.");
        }
        return jdbc.transaction(connection -> {
            Lot lot = jdbc.lockLot(connection, lotId);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT m.* FROM operaciones_merma o JOIN registro_mermas m ON m.id_registro=o.id_registro WHERE o.clave_operacion=?")) {
                statement.setString(1, key);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        Integer storedInspection = (Integer) result.getObject("id_inspeccion");
                        if (result.getInt("id_lote") != lotId || !java.util.Objects.equals(storedInspection, inspectionId)
                                || quantity.compareTo(result.getBigDecimal("cantidad_perdida")) != 0
                                || !checkedReason.equals(result.getString("motivo"))
                                || !checkedUser.equals(result.getString("usuario_registro"))) {
                            throw new SQLException("La clave de operación ya corresponde a otra merma.");
                        }
                        return result.getInt("id_registro");
                    }
                }
            }
            if (quantity.compareTo(lot.getQuantity()) > 0) { throw new SQLException("La merma supera las existencias del lote."); }
            if (inspectionId != null) {
                if (!hasOpenQuarantine(connection, lotId)) { throw new SQLException("La inspección requiere una cuarentena pendiente."); }
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT id_lote,resultado FROM inspecciones_calidad WHERE id_inspeccion=? "
                        + "AND id_inspeccion=(SELECT MAX(i.id_inspeccion) FROM inspecciones_calidad i WHERE i.id_lote=?) FOR UPDATE")) {
                    statement.setInt(1, inspectionId);
                    statement.setInt(2, lotId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || result.getInt(1) != lotId || !"Rechazado".equals(result.getString(2))) {
                            throw new SQLException("La merma debe referenciar la última inspección rechazada vigente del mismo lote.");
                        }
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM registro_mermas WHERE id_inspeccion=?")) {
                    statement.setInt(1, inspectionId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) { throw new SQLException("La inspección ya tiene una baja registrada."); }
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE lotes_inventario SET cantidad=cantidad-? WHERE id_lote=? AND cantidad>=?")) {
                statement.setBigDecimal(1, quantity);
                statement.setInt(2, lotId);
                statement.setBigDecimal(3, quantity);
                if (statement.executeUpdate() != 1) { throw new SQLException("No se pudo descontar la merma."); }
            }
            int id;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO registro_mermas(id_lote,id_inspeccion,cantidad_perdida,motivo,usuario_registro) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, lotId);
                if (inspectionId == null) { statement.setNull(2, Types.INTEGER); } else { statement.setInt(2, inspectionId); }
                statement.setBigDecimal(3, quantity);
                statement.setString(4, checkedReason);
                statement.setString(5, checkedUser);
                id = InventoryJdbc.insertId(statement);
            }
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO operaciones_merma(clave_operacion,id_registro) VALUES(?,?)")) {
                statement.setString(1, key);
                statement.setInt(2, id);
                statement.executeUpdate();
            }
            return id;
        });
    }

    public int inspect(int lotId, boolean approved, String inspector, String rejectionReason,
                       String notes) throws SQLException {
        String checkedInspector = InventoryJdbc.requiredText(inspector, 100, "El inspector");
        String checkedReason = approved ? null : InventoryJdbc.requiredText(rejectionReason, 10000, "El motivo de rechazo");
        String checkedNotes = notes == null || notes.isBlank() ? null
                : InventoryJdbc.requiredText(notes, 10000, "Las notas");
        return jdbc.transaction(connection -> {
            Lot lot = jdbc.lockLot(connection, lotId);
            if (!hasOpenQuarantine(connection, lotId)) {
                throw new SQLException("El lote no tiene una cuarentena pendiente de inspección.");
            }
            int id;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO inspecciones_calidad(id_lote,resultado,nombre_inspector,motivo_rechazo,notas) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, lotId);
                statement.setString(2, approved ? "Aprobado" : "Rechazado");
                statement.setString(3, checkedInspector);
                statement.setString(4, checkedReason);
                statement.setString(5, checkedNotes);
                id = InventoryJdbc.insertId(statement);
            }
            if (approved) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE registro_cuarentena SET resuelta=TRUE,fecha_resolucion=CURRENT_TIMESTAMP,"
                        + "notas_resolucion=? WHERE id_lote=? AND resuelta=FALSE")) {
                    statement.setString(1, "Inspección aprobada #" + id + ": " + checkedInspector);
                    statement.setInt(2, lotId);
                    if (statement.executeUpdate() == 0) { throw new SQLException("La cuarentena ya fue resuelta."); }
                }
                jdbc.setStatus(connection, lotId, LotStatus.forDate(lot.getExpirationDate(), today()));
            } else {
                jdbc.setStatus(connection, lotId, LotStatus.IN_QUARANTINE);
            }
            return id;
        });
    }

    public List<Inspection> inspectionHistory(int lotId) throws SQLException {
        if (lotId <= 0) { throw new IllegalArgumentException("El ID del lote debe ser positivo."); }
        List<Inspection> history = new ArrayList<>();
        try (Connection connection = jdbc.open(); PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM inspecciones_calidad WHERE id_lote=? ORDER BY id_inspeccion")) {
            statement.setInt(1, lotId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    history.add(new Inspection(result.getInt("id_inspeccion"), lotId,
                            result.getTimestamp("fecha_inspeccion").toLocalDateTime(), result.getString("resultado"),
                            result.getString("nombre_inspector"), result.getString("motivo_rechazo"), result.getString("notas")));
                }
            }
        }
        return history;
    }

    public int quarantine(int lotId, String reason, String responsible) throws SQLException {
        String checkedReason = InventoryJdbc.requiredText(reason, 10000, "El motivo");
        String checkedResponsible = InventoryJdbc.requiredText(responsible, 100, "El responsable");
        return jdbc.transaction(connection -> {
            Lot lot = jdbc.lockLot(connection, lotId);
            if (lot.getQuantity().signum() == 0) { throw new SQLException("El lote no tiene existencias."); }
            if (jdbc.status(connection, lot.getStatusId()) == LotStatus.IN_QUARANTINE
                    || hasOpenQuarantine(connection, lotId)) {
                throw new SQLException("El lote ya tiene una cuarentena pendiente.");
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO registro_cuarentena(id_lote,motivo,usuario) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, lotId);
                statement.setString(2, checkedReason);
                statement.setString(3, checkedResponsible);
                int id = InventoryJdbc.insertId(statement);
                jdbc.setStatus(connection, lotId, LotStatus.IN_QUARANTINE);
                return id;
            }
        });
    }

    private boolean hasOpenQuarantine(Connection connection, int lotId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM registro_cuarentena WHERE id_lote=? AND resuelta=FALSE")) {
            statement.setInt(1, lotId);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    public int refreshStatuses(LocalDate today) throws SQLException {
        return jdbc.transaction(connection -> {
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id_lote FROM lotes_inventario ORDER BY id_lote");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) { ids.add(result.getInt(1)); }
            }
            for (int id : ids) { jdbc.refreshLocked(connection, jdbc.lockLot(connection, id), today); }
            return ids.size();
        });
    }

    public LotStatus refreshLot(int lotId, LocalDate today) throws SQLException {
        return jdbc.transaction(connection -> jdbc.refreshLocked(connection, jdbc.lockLot(connection, lotId), today));
    }

    // The caller owns the sale transaction and must retain this row lock until commit.
    public void assertSellable(Connection connection, int lotId, BigDecimal quantity) throws SQLException {
        if (connection.getAutoCommit()) {
            throw new SQLException("La venta debe validar el lote dentro de su transacción.");
        }
        if (quantity == null || quantity.signum() <= 0 || quantity.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("La cantidad de venta debe ser positiva y tener máximo dos decimales.");
        }
        Lot lot = jdbc.lockLot(connection, lotId);
        LotStatus status = jdbc.refreshLocked(connection, lot, today());
        if (!status.allowsSale() || quantity.compareTo(lot.getQuantity()) > 0) {
            throw new SQLException("El lote está bloqueado para ventas o no tiene existencias suficientes.");
        }
    }
}
