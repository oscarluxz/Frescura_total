package frescuratotal.erp.inventory.controller;

import frescuratotal.erp.inventory.model.dao.InventoryQualityDAO;
import java.sql.SQLException;

public class QualityController {
    private final InventoryQualityDAO qualityDAO;

    public QualityController() { this(new InventoryQualityDAO()); }
    public QualityController(InventoryQualityDAO qualityDAO) { this.qualityDAO = qualityDAO; }

    public int refreshStatuses() throws SQLException {
        return qualityDAO.refreshStatuses(InventoryQualityDAO.today());
    }

    public int quarantine(int lotId, String reason, String responsible) throws SQLException {
        return qualityDAO.quarantine(lotId, reason, responsible);
    }
}
