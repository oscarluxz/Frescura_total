package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.logistics.model.DispatchOrder;

import java.sql.SQLException;
import java.util.List;

/** Read model for the dispatch panel. */
public interface DispatchOrderRepository {
    /**
     * All sales in IN_PREPARATION. Each order carries its active route (id and name)
     * when it already has one, so the panel can show whether it is assigned.
     */
    List<DispatchOrder> findInPreparation() throws SQLException;
}
