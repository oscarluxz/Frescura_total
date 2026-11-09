package frescuratotal.erp.suppliers;

import frescuratotal.erp.suppliers.analytics.SupplierAnalyticsService;
import frescuratotal.erp.suppliers.analytics.SupplierPerformanceReport;
import frescuratotal.erp.suppliers.model.SupplierLot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplierAnalyticsServiceTest {

    private final SupplierAnalyticsService service = new SupplierAnalyticsService(() -> List.of());

    private static SupplierLot lot(int id, String quantity, String status, int supplierId, String supplierName) {
        return new SupplierLot(id, new BigDecimal(quantity), status, supplierId, supplierName);
    }

    @Test public void findsSupplierWithHighestAccumulatedVolume() {
        List<SupplierLot> lots = List.of(
                lot(1, "100.50", "Disponible", 1, "Lácteos del Campo"),
                lot(2, "300.00", "Disponible", 1, "Lácteos del Campo"),
                lot(3, "250.00", "Vencido", 2, "Frutas del Valle"),
                lot(4, "50.00", "Disponible", 3, "Carnes La Excelencia"));
        SupplierPerformanceReport report = service.buildReport(lots).orElseThrow();
        assertEquals("Lácteos del Campo", report.topVolumeSupplier());
    }

    @Test public void listsSuppliersWithExpiredLotsWithoutDuplicates() {
        List<SupplierLot> lots = List.of(
                lot(1, "10", "Vencido", 2, "Frutas del Valle"),
                lot(2, "20", "Vencido", 2, "Frutas del Valle"),
                lot(3, "30", "Vencido", 3, "Carnes La Excelencia"),
                lot(4, "40", "Disponible", 1, "Lácteos del Campo"),
                lot(5, "50", "Proximo a Vencer", 4, "Verduras Sol"));
        SupplierPerformanceReport report = service.buildReport(lots).orElseThrow();
        assertEquals(List.of("Carnes La Excelencia", "Frutas del Valle"), report.suppliersWithExpiredLots());
    }

    @Test public void returnsEmptyListWhenNoLotIsExpired() {
        SupplierPerformanceReport report = service.buildReport(List.of(lot(1, "5", "Disponible", 1, "A"))).orElseThrow();
        assertTrue(report.suppliersWithExpiredLots().isEmpty());
    }

    @Test public void acceptsAnyOfTheTiedSuppliers() {
        List<SupplierLot> lots = List.of(lot(1, "5", "Disponible", 1, "A"), lot(2, "5", "Disponible", 2, "B"));
        String top = service.buildReport(lots).orElseThrow().topVolumeSupplier();
        assertTrue(top.equals("A") || top.equals("B"));
    }

    @Test public void doesNotMergeDifferentSuppliersThatShareAName() {
        List<SupplierLot> lots = List.of(
                lot(1, "60", "Disponible", 1, "Agro"),
                lot(2, "60", "Disponible", 2, "Agro"),
                lot(3, "100", "Disponible", 3, "Otro"));
        assertEquals("Otro", service.buildReport(lots).orElseThrow().topVolumeSupplier());
    }

    @Test public void emptyOrMissingLotsProduceNoReport() {
        assertEquals(Optional.empty(), service.buildReport(List.of()));
        assertEquals(Optional.empty(), service.buildReport(null));
    }

    @Test public void generateReportReadsLotsFromTheRepository() throws Exception {
        SupplierAnalyticsService fromRepository = new SupplierAnalyticsService(
                () -> List.of(lot(1, "10", "Vencido", 1, "Proveedor Uno")));
        SupplierPerformanceReport report = fromRepository.generateReport().orElseThrow();
        assertEquals("Proveedor Uno", report.topVolumeSupplier());
        assertEquals(List.of("Proveedor Uno"), report.suppliersWithExpiredLots());
    }
}
