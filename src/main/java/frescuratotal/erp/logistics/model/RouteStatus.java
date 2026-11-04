package frescuratotal.erp.logistics.model;

/** Status of a delivery route (rutas_entrega.estado). */
public enum RouteStatus {
    PLANNED("Planificada", "Planificada"),
    IN_TRANSIT("En Transito", "En tránsito"),
    COMPLETED("Completada", "Completada"),
    CANCELLED("Cancelada", "Cancelada");

    private final String dbName;
    private final String label;

    RouteStatus(String dbName, String label) {
        this.dbName = dbName;
        this.label = label;
    }

    public String getDbName() {
        return dbName;
    }

    public String getLabel() {
        return label;
    }

    public static RouteStatus fromDbName(String name) {
        for (RouteStatus status : values()) {
            if (status.dbName.equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de ruta desconocido: " + name);
    }

    @Override
    public String toString() {
        return label;
    }
}