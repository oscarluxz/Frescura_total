package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.model.Inspection;
import frescuratotal.erp.inventory.model.LotStatus;
import java.sql.*;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class InspectionMySqlTest extends QualityMySqlSupport {
    @Test public void preservesRejectionAndApprovalHistory() throws Exception {
        int lot = addLot(dao.today().plusDays(7));
        dao.quarantine(lot, "Revisión", "Junior");
        int rejected = dao.inspect(lot, false, "Ana", "Envase dañado", "Primera revisión");
        assertEquals(LotStatus.IN_QUARANTINE, dao.refreshLot(lot, dao.today()));
        try (Connection connection = jdbc.open()) {
            assertEquals(0, jdbc.lockLot(connection, lot).getQuantity().signum());
        }
        List<Inspection> history = dao.inspectionHistory(lot);
        assertEquals(1, history.size());
        assertEquals(rejected, history.get(0).getId());
        assertEquals("Rechazado", history.get(0).getResult());
        assertEquals("Envase dañado", history.get(0).getRejectionReason());
        assertEquals("Ana", history.get(0).getInspector());
        assertEquals(1, count("registro_mermas"));
        try { dao.inspect(lot, true, "Ana", null, null); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("pendiente")); }
        int otherLot = addLot(dao.today().plusDays(7));
        dao.quarantine(otherLot, "Revisión", "Junior");
        dao.inspect(otherLot, true, "Luis", null, "Sin defectos");
        assertEquals(LotStatus.NEAR_EXPIRY, dao.refreshLot(otherLot, dao.today()));
        assertEquals("Aprobado", dao.inspectionHistory(otherLot).get(0).getResult());
    }

    @Test public void approvalNeverMakesExpiredLotSellable() throws Exception {
        int lot = addLot(dao.today());
        dao.quarantine(lot, "Vencimiento", "Junior");
        dao.inspect(lot, true, "Ana", null, null);
        assertEquals(LotStatus.EXPIRED, dao.refreshLot(lot, dao.today()));
    }

    @Test public void rejectionAndWasteRollbackTogetherWhenWasteInsertFails() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        dao.quarantine(lot, "Revisión", "Junior");
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER fail_rejected_waste BEFORE INSERT ON registro_mermas FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Forced rejection failure'");
        }
        try { dao.inspect(lot, false, "Ana", "Daño", null); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("Forced rejection failure")); }
        assertEquals(0, count("inspecciones_calidad"));
        assertEquals(0, count("registro_mermas"));
        try (Connection connection = jdbc.open()) {
            assertEquals(0, new java.math.BigDecimal("10").compareTo(jdbc.lockLot(connection, lot).getQuantity()));
        }
    }

    @Test public void validatesRejectionAndRollsBackFailedApproval() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        dao.quarantine(lot, "Daño", "Junior");
        try { dao.inspect(lot, false, "Ana", " ", null); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("rechazo")); }
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER fail_inspection BEFORE UPDATE ON lotes_inventario "
                    + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Forced inspection failure'");
        }
        try { dao.inspect(lot, true, "Ana", null, null); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("Forced inspection failure")); }
        assertEquals(0, count("inspecciones_calidad"));
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT resuelta FROM registro_cuarentena")) {
            result.next(); assertFalse(result.getBoolean(1));
        }
    }
}
