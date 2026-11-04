package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.model.LotStatus;
import java.math.BigDecimal;
import java.sql.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuarantineMySqlTest extends QualityMySqlSupport {
    @Test public void recordsReasonAndBlocksSalesWithoutLiftingQuarantine() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        assertTrue(dao.quarantine(lot, " Envase dañado ", " Junior ") > 0);
        assertEquals(LotStatus.IN_QUARANTINE, dao.refreshLot(lot, dao.today().plusDays(60)));
        try (Connection connection = jdbc.open()) {
            connection.setAutoCommit(false);
            try { dao.assertSellable(connection, lot, BigDecimal.ONE); fail(); }
            catch (SQLException expected) { assertTrue(expected.getMessage().contains("bloqueado")); }
            connection.rollback();
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT motivo,usuario,resuelta FROM registro_cuarentena")) {
                assertTrue(result.next());
                assertEquals("Envase dañado", result.getString(1));
                assertEquals("Junior", result.getString(2));
                assertFalse(result.getBoolean(3));
            }
        }
        try { dao.quarantine(lot, "Duplicado", "Junior"); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("pendiente")); }
        assertEquals(1, count("registro_cuarentena"));
        try { dao.quarantine(lot, " ", "Junior"); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("motivo")); }
    }

    @Test public void rollsBackRecordIfStatusUpdateFails() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER fail_status BEFORE UPDATE ON lotes_inventario "
                    + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Forced failure'");
        }
        try { dao.quarantine(lot, "Daño", "Junior"); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("Forced failure")); }
        assertEquals(0, count("registro_cuarentena"));
    }
}
