package frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public final class InvoiceLine {
    private final String productName;
    private final int lotId;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal discountRate;
    private final BigDecimal discount;
    private final BigDecimal taxRate;
    private final BigDecimal tax;
    private final BigDecimal subtotal;
    private final BigDecimal total;

    public InvoiceLine(String productName, int lotId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountRate, BigDecimal discount, BigDecimal taxRate, BigDecimal tax, BigDecimal subtotal, BigDecimal total) {
        this.productName = productName;
        this.lotId = lotId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.discount = discount;
        this.taxRate = taxRate;
        this.tax = tax;
        this.subtotal = subtotal;
        this.total = total;
    }

    public String productName() { return productName; }
    public String getProductName() { return productName; }
    public int lotId() { return lotId; }
    public int getLotId() { return lotId; }
    public BigDecimal quantity() { return quantity; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal unitPrice() { return unitPrice; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal discountRate() { return discountRate; }
    public BigDecimal getDiscountRate() { return discountRate; }
    public BigDecimal discount() { return discount; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal taxRate() { return taxRate; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal tax() { return tax; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal subtotal() { return subtotal; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal total() { return total; }
    public BigDecimal getTotal() { return total; }

    @Override public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof InvoiceLine)) { return false; }
        InvoiceLine value = (InvoiceLine) other;
        return java.util.Objects.equals(productName, value.productName)
                && lotId == value.lotId
                && java.util.Objects.equals(quantity, value.quantity)
                && java.util.Objects.equals(unitPrice, value.unitPrice)
                && java.util.Objects.equals(discountRate, value.discountRate)
                && java.util.Objects.equals(discount, value.discount)
                && java.util.Objects.equals(taxRate, value.taxRate)
                && java.util.Objects.equals(tax, value.tax)
                && java.util.Objects.equals(subtotal, value.subtotal)
                && java.util.Objects.equals(total, value.total);
    }
    @Override public int hashCode() { return java.util.Objects.hash(productName, lotId, quantity, unitPrice, discountRate, discount, taxRate, tax, subtotal, total); }
}
