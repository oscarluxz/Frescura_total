package frescuratotal.erp.inventory.controller;

import frescuratotal.erp.inventory.model.dao.InventoryQualityDAO;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;
import frescuratotal.erp.inventory.model.Inspection;

public class QualityController {
    private final InventoryQualityDAO qualityDAO;
    private final frescuratotal.erp.common.UserSession session;
    private java.time.LocalDate lastReview;

    public QualityController() { this(new InventoryQualityDAO()); }
    public QualityController(InventoryQualityDAO qualityDAO) { this(qualityDAO, new frescuratotal.erp.common.UserSession()); }
    public QualityController(InventoryQualityDAO qualityDAO, frescuratotal.erp.common.UserSession session) {
        this.qualityDAO = qualityDAO;
        this.session = session;
    }

    public void reviewForToday() throws SQLException {
        java.time.LocalDate today = InventoryQualityDAO.today();
        if (!today.equals(lastReview)) {
            int count = qualityDAO.refreshStatuses(today);
            lastReview = today;
            System.out.println("Revisión diaria simulada completada: " + count + " lotes. Los vencidos con existencias quedan pendientes de baja.");
        }
    }

    public int refreshStatuses() throws SQLException {
        return qualityDAO.refreshStatuses(InventoryQualityDAO.today());
    }

    public int quarantine(int lotId, String reason, String responsible) throws SQLException {
        return qualityDAO.quarantine(lotId, reason, responsible);
    }

    public int inspect(int lotId, boolean approved, String inspector, String rejectionReason, String notes) throws SQLException {
        session.requireInspector();
        return qualityDAO.inspect(lotId, approved, inspector, rejectionReason, notes);
    }

    public List<Inspection> inspectionHistory(int lotId) throws SQLException {
        return qualityDAO.inspectionHistory(lotId);
    }

    public int recordWaste(int lotId, Integer inspectionId, BigDecimal quantity, String reason,
                          String responsible, String operationKey) throws SQLException {
        return qualityDAO.recordWaste(lotId, inspectionId, quantity, reason, responsible, operationKey);
    }
}
