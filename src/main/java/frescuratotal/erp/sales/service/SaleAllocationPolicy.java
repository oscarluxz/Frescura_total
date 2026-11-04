package frescuratotal.erp.sales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Applies FEFO allocation, near-expiry discount, and IVA to sale lines. */
public final class SaleAllocationPolicy {
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.50");
    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private SaleAllocationPolicy() { }

    public static List<Allocation> allocate(BigDecimal requested, List<Lot> lots, LocalDate today) {
        if (requested == null || requested.signum() <= 0 || lots == null || today == null) {
            throw new IllegalArgumentException("La cantidad solicitada, los lotes y la fecha son obligatorios.");
        }
        List<Lot> ordered = lots.stream()
                .filter(lot -> lot != null && lot.sellable() && lot.quantity() != null
                        && lot.quantity().signum() > 0 && lot.expirationDate() != null
                        && lot.expirationDate().isAfter(today))
                .sorted(Comparator.comparing(Lot::expirationDate).thenComparingInt(Lot::id))
                .toList();

        List<Allocation> allocations = new ArrayList<>();
        BigDecimal remaining = requested;
        for (Lot lot : ordered) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal quantity = remaining.min(lot.quantity());
            BigDecimal discountRate = !lot.expirationDate().isAfter(today.plusDays(7))
                    ? DISCOUNT_RATE : ZERO;
            BigDecimal gross = money(quantity.multiply(lot.salePrice()));
            BigDecimal discount = money(gross.multiply(discountRate));
            BigDecimal tax = money(gross.subtract(discount).multiply(IVA_RATE));
            allocations.add(new Allocation(lot.id(), lot.productName(), quantity, lot.salePrice(),
                    discountRate, discount, IVA_RATE, tax, gross,
                    money(gross.subtract(discount).add(tax))));
            remaining = remaining.subtract(quantity);
        }
        if (remaining.signum() > 0) {
            throw new IllegalArgumentException("Existencias vendibles insuficientes. Cantidad faltante: " + remaining);
        }
        return List.copyOf(allocations);
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static final class Lot {
    private final int id;
    private final String productName;
    private final LocalDate expirationDate;
    private final BigDecimal quantity;
    private final BigDecimal salePrice;
    private final boolean sellable;

    public Lot(int id, String productName, LocalDate expirationDate, BigDecimal quantity, BigDecimal salePrice, boolean sellable) {
        this.id = id;
        this.productName = productName;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.sellable = sellable;
    }

    public int id() { return id; }
    public int getId() { return id; }
    public String productName() { return productName; }
    public String getProductName() { return productName; }
    public LocalDate expirationDate() { return expirationDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public BigDecimal quantity() { return quantity; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal salePrice() { return salePrice; }
    public BigDecimal getSalePrice() { return salePrice; }
    public boolean sellable() { return sellable; }
    public boolean isSellable() { return sellable; }

    @Override public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof Lot)) { return false; }
        Lot value = (Lot) other;
        return id == value.id
                && java.util.Objects.equals(productName, value.productName)
                && java.util.Objects.equals(expirationDate, value.expirationDate)
                && java.util.Objects.equals(quantity, value.quantity)
                && java.util.Objects.equals(salePrice, value.salePrice)
                && sellable == value.sellable;
    }
    @Override public int hashCode() { return java.util.Objects.hash(id, productName, expirationDate, quantity, salePrice, sellable); }
}

    public static final class Allocation {
    private final int lotId;
    private final String productName;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal discountRate;
    private final BigDecimal discount;
    private final BigDecimal taxRate;
    private final BigDecimal tax;
    private final BigDecimal gross;
    private final BigDecimal lineTotal;

    public Allocation(int lotId, String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountRate, BigDecimal discount, BigDecimal taxRate, BigDecimal tax, BigDecimal gross, BigDecimal lineTotal) {
        this.lotId = lotId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.discount = discount;
        this.taxRate = taxRate;
        this.tax = tax;
        this.gross = gross;
        this.lineTotal = lineTotal;
    }

    public int lotId() { return lotId; }
    public int getLotId() { return lotId; }
    public String productName() { return productName; }
    public String getProductName() { return productName; }
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
    public BigDecimal gross() { return gross; }
    public BigDecimal getGross() { return gross; }
    public BigDecimal lineTotal() { return lineTotal; }
    public BigDecimal getLineTotal() { return lineTotal; }

    @Override public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof Allocation)) { return false; }
        Allocation value = (Allocation) other;
        return lotId == value.lotId
                && java.util.Objects.equals(productName, value.productName)
                && java.util.Objects.equals(quantity, value.quantity)
                && java.util.Objects.equals(unitPrice, value.unitPrice)
                && java.util.Objects.equals(discountRate, value.discountRate)
                && java.util.Objects.equals(discount, value.discount)
                && java.util.Objects.equals(taxRate, value.taxRate)
                && java.util.Objects.equals(tax, value.tax)
                && java.util.Objects.equals(gross, value.gross)
                && java.util.Objects.equals(lineTotal, value.lineTotal);
    }
    @Override public int hashCode() { return java.util.Objects.hash(lotId, productName, quantity, unitPrice, discountRate, discount, taxRate, tax, gross, lineTotal); }
}
}
