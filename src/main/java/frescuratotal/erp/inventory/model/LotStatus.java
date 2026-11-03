package frescuratotal.erp.inventory.model;

import java.time.LocalDate;

public enum LotStatus {
    AVAILABLE("Disponible"), NEAR_EXPIRY("Proximo a Vencer"),
    EXPIRED("Vencido"), IN_QUARANTINE("En Cuarentena");

    private final String databaseName;

    LotStatus(String databaseName) { this.databaseName = databaseName; }

    public String getDatabaseName() { return databaseName; }

    public static LotStatus fromDatabaseName(String name) {
        for (LotStatus status : values()) {
            if (status.databaseName.equals(name)) { return status; }
        }
        throw new IllegalArgumentException("Estado de lote desconocido: " + name);
    }

    public static LotStatus forDate(LocalDate expiration, LocalDate today) {
        if (expiration == null || today == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (!expiration.isAfter(today)) { return EXPIRED; }
        return expiration.isAfter(today.plusDays(7)) ? AVAILABLE : NEAR_EXPIRY;
    }

    public boolean allowsSale() { return this == AVAILABLE || this == NEAR_EXPIRY; }
}
