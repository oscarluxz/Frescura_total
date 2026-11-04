package frescuratotal.erp.sales.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SaleAllocationPolicyTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 3);

    @Test
    public void allocatesAcrossLotsInFefoOrderAndAppliesNearExpiryDiscount() {
        var laterLot = lot(12, 15, "10.00", "10.00", true);
        var nearExpiryLot = lot(8, 5, "5.00", "10.00", true);

        List<SaleAllocationPolicy.Allocation> result = SaleAllocationPolicy.allocate(
                new BigDecimal("7.00"), List.of(laterLot, nearExpiryLot), TODAY);

        assertEquals(2, result.size());
        assertEquals(8, result.get(0).lotId());
        assertEquals(new BigDecimal("5.00"), result.get(0).quantity());
        assertEquals(new BigDecimal("25.00"), result.get(0).discount());
        assertEquals(new BigDecimal("4.75"), result.get(0).tax());
        assertEquals(12, result.get(1).lotId());
        assertEquals(new BigDecimal("2.00"), result.get(1).quantity());
        assertEquals(new BigDecimal("0.00"), result.get(1).discount());
        assertEquals(new BigDecimal("3.80"), result.get(1).tax());
    }

    @Test
    public void excludesExpiredTodayAndUnsellableLots() {
        List<SaleAllocationPolicy.Lot> lots = List.of(
                lot(1, 0, "10.00", "10.00", true),
                lot(2, 2, "10.00", "10.00", false),
                lot(3, -1, "10.00", "10.00", true));

        assertThrows(IllegalArgumentException.class, () -> SaleAllocationPolicy.allocate(
                BigDecimal.ONE, lots, TODAY));
    }

    @Test
    public void appliesDiscountThroughDaySevenButNotOnDayEight() {
        var daySeven = SaleAllocationPolicy.allocate(BigDecimal.ONE,
                List.of(lot(7, 7, "1.00", "100.00", true)), TODAY).get(0);
        var dayEight = SaleAllocationPolicy.allocate(BigDecimal.ONE,
                List.of(lot(8, 8, "1.00", "100.00", true)), TODAY).get(0);

        assertEquals(new BigDecimal("50.00"), daySeven.discount());
        assertEquals(new BigDecimal("9.50"), daySeven.tax());
        assertEquals(new BigDecimal("0.00"), dayEight.discount());
        assertEquals(new BigDecimal("19.00"), dayEight.tax());
    }

    @Test
    public void rejectsInsufficientSellableStock() {
        assertThrows(IllegalArgumentException.class, () -> SaleAllocationPolicy.allocate(
                new BigDecimal("2.00"), List.of(lot(1, 8, "1.00", "100.00", true)), TODAY));
    }

    private SaleAllocationPolicy.Lot lot(int id, long daysUntilExpiry,
                                         String quantity, String price, boolean sellable) {
        return new SaleAllocationPolicy.Lot(id, "Product " + id, TODAY.plusDays(daysUntilExpiry),
                new BigDecimal(quantity), new BigDecimal(price), sellable);
    }
}
