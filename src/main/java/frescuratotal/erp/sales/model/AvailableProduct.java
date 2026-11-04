package frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public record AvailableProduct(int id, String name, String unit, BigDecimal availableQuantity,
                               BigDecimal startingPrice) {
}
