package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/** A product and quantity requested by the customer. */
public record SaleItem(int productId, BigDecimal quantity) {
    public SaleItem {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive.");
        }
        if (quantity == null || quantity.signum() <= 0
                || quantity.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Quantity must be positive and have at most two decimals.");
        }
    }
}
