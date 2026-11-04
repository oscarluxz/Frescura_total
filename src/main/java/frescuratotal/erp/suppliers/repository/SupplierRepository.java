package frescuratotal.erp.suppliers.repository;

import frescuratotal.erp.suppliers.model.CatalogProduct;
import frescuratotal.erp.suppliers.model.Supplier;

import java.sql.SQLException;
import java.util.List;

/** Persistence contract for suppliers and their product catalog. */
public interface SupplierRepository {
    int save(Supplier supplier) throws SQLException;
    void update(Supplier supplier) throws SQLException;
    void setActive(int supplierId, boolean active) throws SQLException;
    Supplier findById(int supplierId) throws SQLException;
    List<Supplier> findAll() throws SQLException;
    boolean existsByNit(String nit) throws SQLException;

    List<CatalogProduct> findCatalog(int supplierId) throws SQLException;
    List<CatalogProduct> findAllProducts() throws SQLException;
    void addProductToCatalog(int supplierId, int productId) throws SQLException;
    void removeProductFromCatalog(int supplierId, int productId) throws SQLException;
}