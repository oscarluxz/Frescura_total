/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package frescuratotal.erp.sales.model;

/**
 *
 * @author PC
 */
public enum CustomerType {
    WHOLESALE(1, "Mayorista"),
    RETAIL(2, "Minorista");

    private final int dbId;
    private final String label;

    CustomerType(int dbId, String label) {
        this.dbId = dbId;
        this.label = label;
    }

    public int getDbId() {
        return dbId;
    }

    /** Spanish text shown to the user in the console. */
    public String getLabel() {
        return label;
    }

    public static CustomerType fromDbId(int dbId) {
        for (CustomerType type : values()) {
            if (type.getDbId() == dbId){
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de cliente desconocido: " + dbId);
    }
}
