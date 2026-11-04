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
        int approved = dao.inspect(lot, true, "Luis", null, "Corregido");
        assertEquals(LotStatus.NEAR_EXPIRY, dao.refreshLot(lot, dao.today()));
        List<Inspection> history = dao.inspectionHistory(lot);
        assertEquals(2, history.size());
        assertEquals(rejected, history.get(0).getId());
        assertEquals("Rechazado", history.get(0).getResult());
        assertEquals("Envase dañado", history.get(0).getRejectionReason());
        assertEquals("Ana", history.get(0).getInspector());
        assertEquals(approved, history.get(1).getId());
        assertEquals("Aprobado", history.get(1).getResult());
        try { dao.inspect(lot, true, "Ana", null, null); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("pendiente")); }
    }

    @Test public void approvalNeverMakesExpiredLotSellable() throws Exception {
        int lot = addLot(dao.today());
        dao.quarantine(lot, "Vencimiento", "Junior");
        dao.inspect(lot, true, "Ana", null, null);
        assertEquals(LotStatus.EXPIRED, dao.refreshLot(lot, dao.today()));
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
