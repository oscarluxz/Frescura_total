package frescuratotal.erp.logistics.model;

/**
 * Life cycle of an order during dispatch. Maps to estados_venta:
 * IN_PREPARATION -> IN_TRANSIT -> DELIVERED (no skipping, no going back).
 */
public enum DispatchStatus {
    IN_PREPARATION("En Preparacion", "En preparación"),
    IN_TRANSIT("En Transito", "En tránsito"),
    DELIVERED("Entregada", "Entregada");

    private final String dbName;
    private final String label;

    DispatchStatus(String dbName, String label) {
        this.dbName = dbName;
        this.label = label;
    }

    /** Exact text stored in the database (estados_venta.nombre). */
    public String getDbName() {
        return dbName;
    }

    /** Spanish text shown to the user in the console. */
    public String getLabel() {
        return label;
    }

    public DispatchStatus next() {
        if (this == IN_PREPARATION) {
            return IN_TRANSIT;
        } else if (this == IN_TRANSIT) {
            return DELIVERED;
        }
        return null;
    }

    public boolean canTransitionTo(DispatchStatus target) {
        return next() == target;
    }

    /** Returns null when the database status is not part of the dispatch cycle (e.g. "Registrada"). */
    public static DispatchStatus fromDbName(String name) {
        for (DispatchStatus status : values()) {
            if (status.dbName.equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}