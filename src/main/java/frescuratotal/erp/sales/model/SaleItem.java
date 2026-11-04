package frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/** A product and quantity requested by the customer. */
public record SaleItem(int productId, BigDecimal quantity) {
    public SaleItem {
        if (productId <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser positivo.");
        }
        if (quantity == null || quantity.signum() <= 0
                || quantity.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("La cantidad debe ser positiva y tener máximo dos decimales.");
        }
    }
}
