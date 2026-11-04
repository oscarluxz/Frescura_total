package frescuratotal.erp.inventory.controller;

import frescuratotal.erp.inventory.model.LotTrace;
import frescuratotal.erp.inventory.model.dao.TraceabilityDAO;
import java.sql.SQLException;
import java.util.List;

public class TraceabilityController {
    private final TraceabilityDAO traceDAO;
    public TraceabilityController() { this(new TraceabilityDAO()); }
    public TraceabilityController(TraceabilityDAO traceDAO) { this.traceDAO = traceDAO; }
    public LotTrace traceLot(int lotId) throws SQLException { return traceDAO.traceLot(lotId); }
    public List<LotTrace> inventory() throws SQLException { return traceDAO.inventory(); }
    public List<LotTrace> lotsBySupplier(int supplierId) throws SQLException { return traceDAO.lotsBySupplier(supplierId); }
    public List<LotTrace> lotsByCustomer(int customerId) throws SQLException { return traceDAO.lotsByCustomer(customerId); }
}
