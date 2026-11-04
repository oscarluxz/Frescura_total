package frescuratotal.erp.sales.repository;

import frescuratotal.erp.sales.model.AvailableProduct;
import frescuratotal.erp.sales.model.SaleItem;
import frescuratotal.erp.sales.model.SaleReceipt;
import java.util.List;

public interface SalesRepository {
    List<AvailableProduct> findAvailableProducts();
    SaleReceipt registerSale(int customerId, List<SaleItem> items, String cashier);
}
