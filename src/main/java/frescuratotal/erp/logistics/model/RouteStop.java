package frescuratotal.erp.logistics.model;

import java.sql.Timestamp;

/** One order inside a delivery route (row of ventas_ruta). */
public class RouteStop {

    private int saleId;
    private String saleNumber;
    private int deliveryOrder;
    private String destinationAddress;
    private Timestamp deliveredAt;
    private String receivedBy;
    private DispatchStatus status;

    public RouteStop(int saleId, String saleNumber, int deliveryOrder, String destinationAddress,
                     Timestamp deliveredAt, String receivedBy, DispatchStatus status) {
        this.saleId = saleId;
        this.saleNumber = saleNumber;
        this.deliveryOrder = deliveryOrder;
        this.destinationAddress = destinationAddress;
        this.deliveredAt = deliveredAt;
        this.receivedBy = receivedBy;
        this.status = status;
    }

    public int getSaleId() { return saleId; }
    public String getSaleNumber() { return saleNumber; }
    public int getDeliveryOrder() { return deliveryOrder; }
    public String getDestinationAddress() { return destinationAddress; }
    public Timestamp getDeliveredAt() { return deliveredAt; }
    public String getReceivedBy() { return receivedBy; }
    public DispatchStatus getStatus() { return status; }
}