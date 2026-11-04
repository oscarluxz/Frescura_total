package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.logistics.model.Driver;

import java.sql.SQLException;
import java.util.List;

/** Persistence contract for drivers. */
public interface DriverRepository {
    int save(Driver driver) throws SQLException;
    void setActive(int driverId, boolean active) throws SQLException;
    boolean existsByLicense(String licenseNumber) throws SQLException;
    List<Driver> findAll() throws SQLException;
    /** Active drivers that are not assigned to a planned or in-transit route. */
    List<Driver> findAvailable() throws SQLException;
}