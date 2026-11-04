package frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public final class AvailableProduct {
    private final int id;
    private final String name;
    private final String unit;
    private final BigDecimal availableQuantity;
    private final BigDecimal startingPrice;

    public AvailableProduct(int id, String name, String unit, BigDecimal availableQuantity, BigDecimal startingPrice) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.availableQuantity = availableQuantity;
        this.startingPrice = startingPrice;
    }

    public int id() { return id; }
    public int getId() { return id; }
    public String name() { return name; }
    public String getName() { return name; }
    public String unit() { return unit; }
    public String getUnit() { return unit; }
    public BigDecimal availableQuantity() { return availableQuantity; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public BigDecimal startingPrice() { return startingPrice; }
    public BigDecimal getStartingPrice() { return startingPrice; }

    @Override public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof AvailableProduct)) { return false; }
        AvailableProduct value = (AvailableProduct) other;
        return id == value.id
                && java.util.Objects.equals(name, value.name)
                && java.util.Objects.equals(unit, value.unit)
                && java.util.Objects.equals(availableQuantity, value.availableQuantity)
                && java.util.Objects.equals(startingPrice, value.startingPrice);
    }
    @Override public int hashCode() { return java.util.Objects.hash(id, name, unit, availableQuantity, startingPrice); }
}
