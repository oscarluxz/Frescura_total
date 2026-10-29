/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.frescuratotal.erp.sales.model;

/**
 *
 * @author PC
 */
public enum CustomerType {
    WHOLESALE(1),
    RETAIL(2);
    
    private final int dbId;

    CustomerType(int dbId) {
        this.dbId = dbId;
    }

    public int getDbId() {
        return dbId;
    }  

    public static CustomerType fromDbId(int dbId) {
        for (CustomerType type : values()) {
            if (type.getDbId() == dbId){
                return type;
            }  
        }
        throw new IllegalArgumentException("Unknown customer type id: " + dbId);
    }
}