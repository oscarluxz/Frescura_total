package frescuratotal.erp.inventory.model;

import java.time.LocalDateTime;

public class Inspection {
    private final int id;
    private final int lotId;
    private final LocalDateTime inspectedAt;
    private final String result;
    private final String inspector;
    private final String rejectionReason;
    private final String notes;

    public Inspection(int id, int lotId, LocalDateTime inspectedAt, String result,
                      String inspector, String rejectionReason, String notes) {
        this.id = id;
        this.lotId = lotId;
        this.inspectedAt = inspectedAt;
        this.result = result;
        this.inspector = inspector;
        this.rejectionReason = rejectionReason;
        this.notes = notes;
    }
    public int getId() { return id; }
    public int getLotId() { return lotId; }
    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public String getResult() { return result; }
    public String getInspector() { return inspector; }
    public String getRejectionReason() { return rejectionReason; }
    public String getNotes() { return notes; }
}
