package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.model.LotStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class StatusMySqlTest extends QualityMySqlSupport {
    @Test public void persistsDateStatusesAndBlocksSales() throws Exception {
        LocalDate today = frescuratotal.erp.inventory.model.dao.InventoryQualityDAO.today();
        int expired = addLot(today);
        int near = addLot(today.plusDays(7));
        int available = addLot(today.plusDays(8));
        assertEquals(3, dao.refreshStatuses(today));
        assertEquals(LotStatus.EXPIRED, dao.refreshLot(expired, today));
        assertEquals(LotStatus.NEAR_EXPIRY, dao.refreshLot(near, today));
        assertEquals(LotStatus.AVAILABLE, dao.refreshLot(available, today));
        try (Connection connection = jdbc.open()) {
            connection.setAutoCommit(false);
            try {
                dao.assertSellable(connection, expired, BigDecimal.ONE);
                fail("Se esperaba bloquear la venta.");
            } catch (SQLException expected) { assertTrue(expected.getMessage().contains("bloqueado")); }
            dao.assertSellable(connection, near, BigDecimal.ONE);
            try {
                dao.assertSellable(connection, available, new BigDecimal("11"));
                fail("Se esperaba rechazar existencias insuficientes.");
            } catch (SQLException expected) { assertTrue(expected.getMessage().contains("existencias")); }
            connection.rollback();
        }
        try (Connection connection = jdbc.open()) {
            try { dao.assertSellable(connection, available, BigDecimal.ONE); fail(); }
            catch (SQLException expected) { assertTrue(expected.getMessage().contains("transacción")); }
        }
    }
}
