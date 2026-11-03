package frescuratotal.erp.logistics.model;

import java.sql.Timestamp;

/** A sale shown in the dispatch panel (built from ventas + clientes). */
public class DispatchOrder {

    private int saleId;
    private String saleNumber;
    private String customerName;
    private String deliveryAddress;
    private Timestamp saleDate;
    private double totalAmount;
    private DispatchStatus status;
    // 0 means the order is not assigned to any active route yet.
    private int assignedRouteId;
    private String assignedRouteName;

    public DispatchOrder(int saleId, String saleNumber, String customerName, String deliveryAddress,
                         Timestamp saleDate, double totalAmount, DispatchStatus status) {
        this(saleId, saleNumber, customerName, deliveryAddress, saleDate, totalAmount, status, 0, null);
    }

    public DispatchOrder(int saleId, String saleNumber, String customerName, String deliveryAddress,
                         Timestamp saleDate, double totalAmount, DispatchStatus status,
                         int assignedRouteId, String assignedRouteName) {
        this.saleId = saleId;
        this.saleNumber = saleNumber;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.assignedRouteId = assignedRouteId;
        this.assignedRouteName = assignedRouteName;
    }

    public int getSaleId() { return saleId; }
    public String getSaleNumber() { return saleNumber; }
    public String getCustomerName() { return customerName; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Timestamp getSaleDate() { return saleDate; }
    public double getTotalAmount() { return totalAmount; }
    public DispatchStatus getStatus() { return status; }
    public int getAssignedRouteId() { return assignedRouteId; }
    public String getAssignedRouteName() { return assignedRouteName; }

    public boolean hasRoute() {
        return assignedRouteId > 0;
    }
}
