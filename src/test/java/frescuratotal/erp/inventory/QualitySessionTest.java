package frescuratotal.erp.inventory;

import frescuratotal.erp.common.UserSession;
import frescuratotal.erp.inventory.controller.QualityController;
import frescuratotal.erp.inventory.model.dao.InventoryQualityDAO;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class QualitySessionTest {
    @Test public void requiresInspectorBeforeCallingDatabaseAndReviewsOncePerDay() throws Exception {
        int[] calls = new int[2];
        InventoryQualityDAO dao = new InventoryQualityDAO() {
            @Override public int refreshStatuses(LocalDate day) { calls[0]++; return 3; }
            @Override public int inspect(int lot, boolean approved, String inspector, String reason, String notes) { calls[1]++; return 7; }
        };
        UserSession session = new UserSession();
        QualityController controller = new QualityController(dao, session);
        try { controller.inspect(1, true, "Ana", null, null); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("Inspector")); }
        assertEquals(0, calls[1]);
        session.setRole(UserSession.Role.INSPECTOR);
        assertEquals(7, controller.inspect(1, true, "Ana", null, null));
        controller.reviewForToday();
        controller.reviewForToday();
        assertEquals(1, calls[0]);
        session.setRole(UserSession.Role.OPERATOR);
        try { controller.inspect(1, false, "Ana", "Daño", null); fail(); }
        catch (IllegalArgumentException expected) { assertEquals(1, calls[1]); }
    }
}
