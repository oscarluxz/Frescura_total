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

    public record Lot(int id, String productName, LocalDate expirationDate,
                      BigDecimal quantity, BigDecimal salePrice, boolean sellable) { }

    public record Allocation(int lotId, String productName, BigDecimal quantity, BigDecimal unitPrice,
                             BigDecimal discountRate, BigDecimal discount, BigDecimal taxRate,
                             BigDecimal tax, BigDecimal gross, BigDecimal lineTotal) { }
}
