package com.frescuratotal.erp.sales.service;

import com.frescuratotal.erp.sales.model.AvailableProduct;
import com.frescuratotal.erp.sales.model.SaleItem;
import com.frescuratotal.erp.sales.model.SaleReceipt;
import com.frescuratotal.erp.sales.repository.SalesRepository;
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
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A sale must contain at least one product.");
        }
        if (cashier == null || cashier.isBlank()) {
            throw new IllegalArgumentException("Cashier name is required.");
        }
        Set<Integer> productIds = new HashSet<>();
        for (SaleItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Sale items cannot be null.");
            }
            if (!productIds.add(item.productId())) {
                throw new IllegalArgumentException("Add each product only once to the sale.");
            }
        }
        return repository.registerSale(customerId, List.copyOf(items), cashier.trim());
    }
}
