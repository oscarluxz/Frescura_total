package frescuratotal.erp.inventory.model;

import java.math.BigDecimal;

public class LotCustomer {
    private final int id;
    private final String name;
    private final String document;
    private final BigDecimal deliveredQuantity;
    public LotCustomer(int id, String name, String document, BigDecimal deliveredQuantity) {
        this.id = id; this.name = name; this.document = document; this.deliveredQuantity = deliveredQuantity;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDocument() { return document; }
    public BigDecimal getDeliveredQuantity() { return deliveredQuantity; }
}
