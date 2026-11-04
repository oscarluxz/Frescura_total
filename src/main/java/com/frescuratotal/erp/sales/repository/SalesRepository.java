package com.frescuratotal.erp.sales.repository;

import com.frescuratotal.erp.sales.model.AvailableProduct;
import com.frescuratotal.erp.sales.model.SaleItem;
import com.frescuratotal.erp.sales.model.SaleReceipt;
import java.util.List;

public interface SalesRepository {
    List<AvailableProduct> findAvailableProducts();
    SaleReceipt registerSale(int customerId, List<SaleItem> items, String cashier);
}
