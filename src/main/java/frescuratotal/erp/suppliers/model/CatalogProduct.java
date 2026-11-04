package frescuratotal.erp.suppliers.model;

/** A product that a supplier is able to provide (row of productos_proveedor + productos). */
public class CatalogProduct {

    private int productId;
    private String name;
    private String unit;

    public CatalogProduct(int productId, String name, String unit) {
        this.productId = productId;
        this.name = name;
        this.unit = unit;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getUnit() { return unit; }

    @Override
    public String toString() {
        return productId + " - " + name + " (" + unit + ")";
    }
}