package frescuratotal.erp.sales.service;

import frescuratotal.erp.sales.model.AvailableProduct;
import frescuratotal.erp.sales.model.SaleItem;
import frescuratotal.erp.sales.model.SaleReceipt;
import frescuratotal.erp.sales.repository.SalesRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesService {
    private final SalesRepository repository;

    public SalesService(SalesRepository repository) {
        this.repository = repository;
    }

    public List<AvailableProduct> findAvailableProducts() {
        return repository.findAvailableProducts();
    }

    public SaleReceipt confirmSale(int customerId, List<SaleItem> items, String cashier) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("El ID del cliente debe ser positivo.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto.");
        }
        if (cashier == null || cashier.isBlank()) {
            throw new IllegalArgumentException("El nombre del cajero es obligatorio.");
        }
        Set<Integer> productIds = new HashSet<>();
        for (SaleItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Los productos de la venta no pueden ser nulos.");
            }
            if (!productIds.add(item.productId())) {
                throw new IllegalArgumentException("Agregue cada producto una sola vez a la venta.");
            }
        }
        return repository.registerSale(customerId, List.copyOf(items), cashier.trim());
    }
}
