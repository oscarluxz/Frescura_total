package frescuratotal.erp.suppliers.controller;

import frescuratotal.erp.suppliers.model.CatalogProduct;
import frescuratotal.erp.suppliers.model.PurchaseRecord;
import frescuratotal.erp.suppliers.model.Supplier;
import frescuratotal.erp.suppliers.model.SupplierReliability;
import frescuratotal.erp.suppliers.repository.PurchaseHistoryRepository;
import frescuratotal.erp.suppliers.repository.SupplierRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Business rules for suppliers. Validation errors are reported with
 * IllegalArgumentException; database failures with SQLException.
 * The exception messages are shown to the user, so they are written in Spanish.
 */
public class SupplierController {

    private final SupplierRepository supplierRepository;
    private final PurchaseHistoryRepository historyRepository;

    public SupplierController(SupplierRepository supplierRepository,
                              PurchaseHistoryRepository historyRepository) {
        this.supplierRepository = supplierRepository;
        this.historyRepository = historyRepository;
    }

    public int registerSupplier(Supplier supplier) throws SQLException {
        validate(supplier);
        if (supplierRepository.existsByNit(supplier.getNit())) {
            throw new IllegalArgumentException("Ya existe un proveedor con el NIT " + supplier.getNit() + ".");
        }
        return supplierRepository.save(supplier);
    }

    public void updateSupplier(Supplier supplier) throws SQLException {
        validate(supplier);
        supplierRepository.update(supplier);
    }

    public void deactivateSupplier(int supplierId) throws SQLException {
        getSupplier(supplierId);
        supplierRepository.setActive(supplierId, false);
    }

    public void activateSupplier(int supplierId) throws SQLException {
        getSupplier(supplierId);
        supplierRepository.setActive(supplierId, true);
    }

    public Supplier getSupplier(int supplierId) throws SQLException {
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier == null) {
            throw new IllegalArgumentException("No se encontró el proveedor " + supplierId + ".");
        }
        return supplier;
    }

    public List<Supplier> listSuppliers() throws SQLException {
        return supplierRepository.findAll();
    }

    public List<CatalogProduct> listAllProducts() throws SQLException {
        return supplierRepository.findAllProducts();
    }

    public void addProductToCatalog(int supplierId, int productId) throws SQLException {
        getSupplier(supplierId);
        boolean productExists = false;
        for (CatalogProduct product : supplierRepository.findAllProducts()) {
            if (product.getProductId() == productId) {
                productExists = true;
            }
        }
        if (!productExists) {
            throw new IllegalArgumentException("El producto " + productId + " no existe.");
        }
        supplierRepository.addProductToCatalog(supplierId, productId);
    }

    public void removeProductFromCatalog(int supplierId, int productId) throws SQLException {
        getSupplier(supplierId);
        supplierRepository.removeProductFromCatalog(supplierId, productId);
    }

    public List<PurchaseRecord> getPurchaseHistory(int supplierId) throws SQLException {
        getSupplier(supplierId);
        return historyRepository.findBySupplier(supplierId);
    }

    public SupplierReliability evaluateSupplier(int supplierId) throws SQLException {
        getSupplier(supplierId);
        return historyRepository.calculateReliability(supplierId);
    }

    private void validate(Supplier supplier) {
        if (isBlank(supplier.getBusinessName())) {
            throw new IllegalArgumentException("La razón social es obligatoria.");
        }
        if (!isValidNit(supplier.getNit())) {
            throw new IllegalArgumentException(
                    "NIT inválido. Use solo dígitos y, opcionalmente, un guion antes del dígito de verificación. Ej: 900123456-7");
        }
        String email = supplier.getEmail();
        if (isBlank(email) || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Se requiere un correo electrónico válido.");
        }
    }

    /** A NIT has digits only, with an optional hyphen before the last digit (900123456-7). */
    private boolean isValidNit(String nit) {
        if (isBlank(nit) || nit.length() < 6 || nit.length() > 12) {
            return false;
        }
        for (int i = 0; i < nit.length(); i++) {
            char c = nit.charAt(i);
            boolean isHyphenBeforeLastDigit = c == '-' && i == nit.length() - 2;
            if (!Character.isDigit(c) && !isHyphenBeforeLastDigit) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}