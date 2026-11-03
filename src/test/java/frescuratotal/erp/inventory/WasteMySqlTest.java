package frescuratotal.erp.inventory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class WasteMySqlTest extends QualityMySqlSupport {
    private BigDecimal remaining(int lot) throws SQLException {
        try (Connection connection = jdbc.open()) { return jdbc.lockLot(connection, lot).getQuantity(); }
    }
    @Test public void deductsOnlyOnceAndRejectsDuplicateInspection() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        dao.quarantine(lot, "Daño", "Junior");
        int inspection = dao.inspect(lot, false, "Ana", "Daño", null);
        int first = dao.recordWaste(lot, inspection, new BigDecimal("3.25"), "Daño", "Junior", "op-1");
        assertEquals(first, dao.recordWaste(lot, inspection, new BigDecimal("3.25"), "Daño", "Junior", "op-1"));
        assertEquals(0, new BigDecimal("6.75").compareTo(remaining(lot)));
        try { dao.recordWaste(lot, inspection, BigDecimal.ONE, "Daño", "Junior", "op-2"); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("baja registrada")); }
        try { dao.recordWaste(lot, null, BigDecimal.ONE, "Daño", "Junior", "op-1"); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("otra merma")); }
        assertEquals(1, count("registro_mermas"));
    }
    @Test public void preventsNegativeStockAndRollsBackOnHistoryFailure() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        try { dao.recordWaste(lot, null, new BigDecimal("11"), "Daño", "Junior", "large"); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("existencias")); }
        try (Connection connection = jdbc.open(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER fail_waste BEFORE INSERT ON registro_mermas "
                    + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Forced waste failure'");
        }
        try { dao.recordWaste(lot, null, BigDecimal.ONE, "Daño", "Junior", "rollback"); fail(); }
        catch (SQLException expected) { assertTrue(expected.getMessage().contains("Forced waste failure")); }
        assertEquals(0, new BigDecimal("10").compareTo(remaining(lot)));
        assertEquals(0, count("registro_mermas"));
        assertEquals(0, count("operaciones_merma"));
    }
    @Test public void serializesConcurrentDeductions() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> a = workers.submit(() -> deductAfterSignal(start, lot, "concurrent-1"));
            Future<Boolean> b = workers.submit(() -> deductAfterSignal(start, lot, "concurrent-2"));
            start.countDown();
            assertNotEquals(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
            assertEquals(0, new BigDecimal("3").compareTo(remaining(lot)));
            assertEquals(1, count("registro_mermas"));
        } finally { workers.shutdownNow(); }
    }
    private boolean deductAfterSignal(CountDownLatch start, int lot, String key) throws Exception {
        start.await();
        try { dao.recordWaste(lot, null, new BigDecimal("7"), "Daño", "Junior", key); return true; }
        catch (SQLException expected) {
            if (!expected.getMessage().contains("existencias")) { throw expected; }
            return false;
        }
    }
}
