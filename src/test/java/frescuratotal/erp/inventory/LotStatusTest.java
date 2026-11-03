package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.model.LotStatus;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class LotStatusTest {
    @Test public void classifiesBoundaryDates() {
        LocalDate today = LocalDate.of(2026, 12, 28);
        assertEquals(LotStatus.EXPIRED, LotStatus.forDate(today.minusDays(1), today));
        assertEquals(LotStatus.EXPIRED, LotStatus.forDate(today, today));
        assertEquals(LotStatus.NEAR_EXPIRY, LotStatus.forDate(today.plusDays(1), today));
        assertEquals(LotStatus.NEAR_EXPIRY, LotStatus.forDate(today.plusDays(7), today));
        assertEquals(LotStatus.AVAILABLE, LotStatus.forDate(today.plusDays(8), today));
    }

    @Test public void blocksUnsafeStatuses() {
        assertTrue(LotStatus.AVAILABLE.allowsSale());
        assertTrue(LotStatus.NEAR_EXPIRY.allowsSale());
        assertFalse(LotStatus.EXPIRED.allowsSale());
        assertFalse(LotStatus.IN_QUARANTINE.allowsSale());
        for (LotStatus status : LotStatus.values()) {
            assertEquals(status, LotStatus.fromDatabaseName(status.getDatabaseName()));
        }
    }
}
