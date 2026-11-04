package frescuratotal.erp.inventory.model;

import java.util.List;

public class LotTrace {
    private final Lot lot;
    private final String productName;
    private final String supplierName;
    private final String supplierNit;
    private final String supplierEmail;
    private final LotStatus status;
    private final List<LotCustomer> customers;
    public LotTrace(Lot lot, String productName, String supplierName, String supplierNit,
                    String supplierEmail, LotStatus status, List<LotCustomer> customers) {
        this.lot = lot; this.productName = productName; this.supplierName = supplierName;
        this.supplierNit = supplierNit; this.supplierEmail = supplierEmail;
        this.status = status; this.customers = List.copyOf(customers);
    }
    public Lot getLot() { return lot; }
    public String getProductName() { return productName; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierNit() { return supplierNit; }
    public String getSupplierEmail() { return supplierEmail; }
    public LotStatus getStatus() { return status; }
    public List<LotCustomer> getCustomers() { return customers; }
}
