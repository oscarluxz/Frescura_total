package frescuratotal.erp.suppliers.model;

import java.sql.Date;
import java.sql.Timestamp;

/** One delivery received from a supplier. Each inventory lot is one purchase entry. */
public class PurchaseRecord {

    private int lotId;
    private String lotNumber;
    private int supplierId;
    private String productName;
    private Timestamp entryDate;
    private Date expirationDate;
    private double initialQuantity;
    private double currentQuantity;
    private double purchasePrice;
    private String lotStatus;

    public PurchaseRecord(int lotId, String lotNumber, int supplierId, String productName,
                          Timestamp entryDate, Date expirationDate, double initialQuantity,
                          double currentQuantity, double purchasePrice, String lotStatus) {
        this.lotId = lotId;
        this.lotNumber = lotNumber;
        this.supplierId = supplierId;
        this.productName = productName;
        this.entryDate = entryDate;
        this.expirationDate = expirationDate;
        this.initialQuantity = initialQuantity;
        this.currentQuantity = currentQuantity;
        this.purchasePrice = purchasePrice;
        this.lotStatus = lotStatus;
    }

    public int getLotId() { return lotId; }
    public String getLotNumber() { return lotNumber; }
    public int getSupplierId() { return supplierId; }
    public String getProductName() { return productName; }
    public Timestamp getEntryDate() { return entryDate; }
    public Date getExpirationDate() { return expirationDate; }
    public double getInitialQuantity() { return initialQuantity; }
    public double getCurrentQuantity() { return currentQuantity; }
    public double getPurchasePrice() { return purchasePrice; }
    public String getLotStatus() { return lotStatus; }

    public double getTotalCost() {
        return initialQuantity * purchasePrice;
    }
}