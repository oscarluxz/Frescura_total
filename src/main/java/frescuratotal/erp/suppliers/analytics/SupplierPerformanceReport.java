package frescuratotal.erp.suppliers.analytics;

import java.util.List;

/**
 * Result of the supplier performance analysis.
 *
 * @param topVolumeSupplier        name of the supplier with the highest accumulated initial quantity
 * @param suppliersWithExpiredLots names of the suppliers that have at least one expired lot, without repeats
 */
public record SupplierPerformanceReport(String topVolumeSupplier, List<String> suppliersWithExpiredLots) {
}
