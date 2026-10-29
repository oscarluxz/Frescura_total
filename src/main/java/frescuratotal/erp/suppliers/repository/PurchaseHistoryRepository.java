package frescuratotal.erp.suppliers.repository;

import frescuratotal.erp.suppliers.model.PurchaseRecord;
import frescuratotal.erp.suppliers.model.SupplierReliability;

import java.sql.SQLException;
import java.util.List;

/** Read-only access to the purchase history (deliveries) of each supplier. */
public interface PurchaseHistoryRepository {
    List<PurchaseRecord> findBySupplier(int supplierId) throws SQLException;
    SupplierReliability calculateReliability(int supplierId) throws SQLException;
}