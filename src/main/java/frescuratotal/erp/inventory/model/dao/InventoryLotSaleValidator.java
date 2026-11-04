package frescuratotal.erp.inventory.model.dao;

import frescuratotal.erp.sales.repository.LotSaleValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adapter between the sales module and the inventory module.
 * Sales asks "can this lot be sold?" and inventory answers with its own quality rules.
 */
public class InventoryLotSaleValidator implements LotSaleValidator {

    private final InventoryQualityDAO qualityDao;

    public InventoryLotSaleValidator() {
        this(new InventoryQualityDAO());
    }

    public InventoryLotSaleValidator(InventoryQualityDAO qualityDao) {
        this.qualityDao = qualityDao;
    }

    @Override
    public void assertSellable(Connection connection, int lotId, BigDecimal quantity) throws SQLException {
        qualityDao.assertSellable(connection, lotId, quantity);
    }
}
