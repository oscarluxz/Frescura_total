package frescuratotal.erp.logistics.model;

/** Operational status of a vehicle (vehiculos.estado). */
public enum VehicleStatus {
    ACTIVE("Activo", "Activo"),
    INACTIVE("Inactivo", "Inactivo"),
    MAINTENANCE("Mantenimiento", "En mantenimiento");

    private final String dbName;
    private final String label;

    VehicleStatus(String dbName, String label) {
        this.dbName = dbName;
        this.label = label;
    }

    public String getDbName() {
        return dbName;
    }

    public String getLabel() {
        return label;
    }

    public static VehicleStatus fromDbName(String name) {
        for (VehicleStatus status : values()) {
            if (status.dbName.equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de vehículo desconocido: " + name);
    }

    @Override
    public String toString() {
        return label;
    }
}