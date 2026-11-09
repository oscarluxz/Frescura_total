package frescuratotal.erp.suppliers.repository;

import frescuratotal.erp.suppliers.model.SupplierLot;

import java.sql.SQLException;
import java.util.List;

/** Read-only contract that provides the lots with their supplier for the analytics. */
public interface SupplierLotRepository {
    List<SupplierLot> findAllLots() throws SQLException;
}
