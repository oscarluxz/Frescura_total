package frescuratotal.erp.suppliers;

import frescuratotal.erp.suppliers.controller.SupplierController;
import frescuratotal.erp.suppliers.model.*;
import frescuratotal.erp.suppliers.repository.SupplierRepository;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplierControllerTest {
    @Test public void reservesSyntheticNitForUpdatingFixture33() throws Exception {
        MemoryRepository repository = new MemoryRepository();
        SupplierController controller = new SupplierController(repository, null);
        Supplier supplier = new Supplier("Proveedor de prueba", "SYN-FT-900427-AG", "Ana", "ana@example.invalid", "123", "Bogotá");
        supplier.setId(33);
        controller.updateSupplier(supplier);
        assertTrue(repository.updated);
        try { controller.registerSupplier(supplier); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("NIT")); }
        supplier.setId(34);
        try { controller.updateSupplier(supplier); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("NIT")); }
        supplier.setNit("900123456-7");
        assertEquals(1, controller.registerSupplier(supplier));
    }

    private static class MemoryRepository implements SupplierRepository {
        private boolean updated;
        public int save(Supplier supplier) { return 1; }
        public void update(Supplier supplier) { updated = true; }
        public boolean existsByNit(String nit) { return false; }
        public void setActive(int id, boolean active) { throw new UnsupportedOperationException(); }
        public Supplier findById(int id) { throw new UnsupportedOperationException(); }
        public List<Supplier> findAll() { throw new UnsupportedOperationException(); }
        public List<CatalogProduct> findCatalog(int id) { throw new UnsupportedOperationException(); }
        public List<CatalogProduct> findAllProducts() { throw new UnsupportedOperationException(); }
        public void addProductToCatalog(int supplier, int product) { throw new UnsupportedOperationException(); }
        public void removeProductFromCatalog(int supplier, int product) { throw new UnsupportedOperationException(); }
    }
}
