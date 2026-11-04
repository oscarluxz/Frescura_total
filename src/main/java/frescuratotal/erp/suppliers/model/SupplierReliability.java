package frescuratotal.erp.suppliers.model;

/** Aggregated quality indicators used to evaluate how reliable a supplier is. */
public class SupplierReliability {

    private int supplierId;
    private int totalLots;
    private double totalQuantity;
    private double wastedQuantity;
    private int rejectedInspections;

    public SupplierReliability(int supplierId, int totalLots, double totalQuantity,
                               double wastedQuantity, int rejectedInspections) {
        this.supplierId = supplierId;
        this.totalLots = totalLots;
        this.totalQuantity = totalQuantity;
        this.wastedQuantity = wastedQuantity;
        this.rejectedInspections = rejectedInspections;
    }

    public int getSupplierId() { return supplierId; }
    public int getTotalLots() { return totalLots; }
    public double getTotalQuantity() { return totalQuantity; }
    public double getWastedQuantity() { return wastedQuantity; }
    public int getRejectedInspections() { return rejectedInspections; }

    /** Percentage of received quantity that ended as waste (merma). */
    public double getWastePercentage() {
        if (totalQuantity == 0) {
            return 0;
        }
        return wastedQuantity * 100 / totalQuantity;
    }

    public String getRating() {
        if (totalLots == 0) {
            return "SIN DATOS";
        }
        double waste = getWastePercentage();
        if (waste <= 5 && rejectedInspections == 0) {
            return "EXCELENTE";
        } else if (waste <= 15 && rejectedInspections <= 2) {
            return "BUENO";
        }
        return "RIESGOSO";
    }
}