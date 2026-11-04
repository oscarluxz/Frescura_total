package frescuratotal.erp.sales.model;

import java.math.BigDecimal;

/** A product and quantity requested by the customer. */
public final class SaleItem {
    private final int productId;
    private final BigDecimal quantity;

    public SaleItem(int productId, BigDecimal quantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser positivo.");
        }
        if (quantity == null || quantity.signum() <= 0
                || quantity.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("La cantidad debe ser positiva y tener máximo dos decimales.");
        }
        this.productId = productId;
        this.quantity = quantity;
    }
    public int productId() { return productId; }
    public int getProductId() { return productId; }
    public BigDecimal quantity() { return quantity; }
    public BigDecimal getQuantity() { return quantity; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof SaleItem)) { return false; }
        SaleItem value = (SaleItem) other;
        return productId == value.productId && quantity.equals(value.quantity);
    }
    @Override public int hashCode() { return java.util.Objects.hash(productId, quantity); }
}
