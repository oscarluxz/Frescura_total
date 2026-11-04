package com.frescuratotal.erp.sales.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/** Adapter boundary for the inventory module's saleability rules. */
@FunctionalInterface
public interface LotSaleValidator {
    void assertSellable(Connection connection, int lotId, BigDecimal quantity) throws SQLException;
}
