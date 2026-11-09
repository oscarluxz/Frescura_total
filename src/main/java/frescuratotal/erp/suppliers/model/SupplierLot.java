package frescuratotal.erp.suppliers.model;

import java.math.BigDecimal;

/**
 * One inventory lot together with the supplier that delivered it and the name of its status.
 * It is the flat row returned by the analytics query, so the statistics can be calculated
 * with Streams without going back to the database.
 */
public record SupplierLot(int lotId, BigDecimal initialQuantity, String lotStatus,
                          int supplierId, String supplierName) {
}
