package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.controller.LotController;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.fail;

public class LotControllerTest {

    private void expectInvalid(int productId, int supplierId, LocalDate expiration,
                               String quantity, String purchase, String sale,
                               String aisle, String shelf) throws Exception {
        try {
            new LotController().registerLot(productId, supplierId, expiration,
                    decimal(quantity), decimal(purchase), decimal(sale), aisle, shelf);
            fail("Se esperaba rechazar el lote antes de acceder a MySQL.");
        } catch (IllegalArgumentException expected) {
            // Validation must reject invalid values before opening a connection.
        }
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    @Test public void rejectsInvalidIds() throws Exception {
        expectInvalid(0, 1, LocalDate.now().plusDays(10), "1", "0", "0", null, null);
        expectInvalid(1, -1, LocalDate.now().plusDays(10), "1", "0", "0", null, null);
    }

    @Test public void rejectsExpiredOrMissingDates() throws Exception {
        expectInvalid(1, 1, null, "1", "0", "0", null, null);
        expectInvalid(1, 1, LocalDate.now(), "1", "0", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().minusDays(1), "1", "0", "0", null, null);
    }

    @Test public void rejectsNonPositiveOrMissingQuantity() throws Exception {
        expectInvalid(1, 1, LocalDate.now().plusDays(10), null, "0", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "0", "0", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "-1", "0", "0", null, null);
    }

    @Test public void rejectsInvalidPrices() throws Exception {
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "-1", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "0", "-1", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", null, "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "0", null, null, null);
    }

    @Test public void rejectsValuesOutsideDecimalContract() throws Exception {
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1.001", "0", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "100000000", "0", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "100000000", "0", null, null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "0", "0.001", null, null);
    }

    @Test public void rejectsLongLocations() throws Exception {
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "0", "0", "x".repeat(51), null);
        expectInvalid(1, 1, LocalDate.now().plusDays(10), "1", "0", "0", null, "x".repeat(51));
    }
}
