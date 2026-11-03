package frescuratotal.erp.inventory.model.dao;

import frescuratotal.erp.inventory.model.Lot;
import frescuratotal.erp.inventory.model.LotStatus;
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
