package frescuratotal.erp.logistics.model;

import java.sql.Timestamp;

/** A sale waiting in the dispatch panel (built from ventas + clientes). */
public class DispatchOrder {

    private int saleId;
    private String saleNumber;
    private String customerName;
    private String deliveryAddress;
    private Timestamp saleDate;
    private double totalAmount;
    private DispatchStatus status;

    public DispatchOrder(int saleId, String saleNumber, String customerName, String deliveryAddress,
                         Timestamp saleDate, double totalAmount, DispatchStatus status) {
        this.saleId = saleId;
        this.saleNumber = saleNumber;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getSaleId() { return saleId; }
    public String getSaleNumber() { return saleNumber; }
    public String getCustomerName() { return customerName; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Timestamp getSaleDate() { return saleDate; }
    public double getTotalAmount() { return totalAmount; }
    public DispatchStatus getStatus() { return status; }
}