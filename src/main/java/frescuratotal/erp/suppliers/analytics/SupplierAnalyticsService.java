package frescuratotal.erp.suppliers.analytics;

import frescuratotal.erp.inventory.model.LotStatus;
import frescuratotal.erp.suppliers.model.SupplierLot;
import frescuratotal.erp.suppliers.repository.SupplierLotRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Calculates the supplier performance statistics.
 * The lots come from a {@link SupplierLotRepository}; the calculations themselves
 * are done only with the Stream API.
 */
public class SupplierAnalyticsService {

    /** Identifies a supplier by id, because two suppliers could share the same name. */
    private record SupplierKey(int id, String name) {
    }

    private final SupplierLotRepository repository;

    public SupplierAnalyticsService(SupplierLotRepository repository) {
        this.repository = repository;
    }

    /** Reads the lots from the database and builds the report. */
    public Optional<SupplierPerformanceReport> generateReport() throws SQLException {
        return buildReport(repository.findAllLots());
    }

    /**
     * Builds the report from a list of lots.
     *
     * @return the report, or empty when there are no lots to analyze
     */
    public Optional<SupplierPerformanceReport> buildReport(List<SupplierLot> lots) {
        if (lots == null || lots.isEmpty()) {
            return Optional.empty();
        }
        return findTopVolumeSupplier(lots)
                .map(top -> new SupplierPerformanceReport(top, findSuppliersWithExpiredLots(lots)));
    }

    /**
     * Statistic 1: name of the supplier with the highest total of initial quantity across its lots.
     * If two suppliers tie, any of them may be returned.
     */
    Optional<String> findTopVolumeSupplier(List<SupplierLot> lots) {
        return lots.stream()
                .collect(Collectors.groupingBy(
                        lot -> new SupplierKey(lot.supplierId(), lot.supplierName()),
                        Collectors.reducing(BigDecimal.ZERO, SupplierLot::initialQuantity, BigDecimal::add)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().name());
    }

    /**
     * Statistic 2: names of the suppliers that have at least one expired lot.
     * Each name appears once, even if the supplier has several expired lots.
     */
    List<String> findSuppliersWithExpiredLots(List<SupplierLot> lots) {
        return lots.stream()
                .filter(lot -> LotStatus.EXPIRED.getDatabaseName().equalsIgnoreCase(lot.lotStatus()))
                .map(SupplierLot::supplierName)
                .distinct()
                .sorted()
                .toList();
    }
}
