package frescuratotal.erp.logistics.repository;

import frescuratotal.erp.logistics.model.Vehicle;
import frescuratotal.erp.logistics.model.VehicleStatus;

import java.sql.SQLException;
import java.util.List;

/** Persistence contract for delivery vehicles. */
public interface VehicleRepository {
    int save(Vehicle vehicle) throws SQLException;
    void updateStatus(int vehicleId, VehicleStatus status) throws SQLException;
    boolean existsByPlate(String plate) throws SQLException;
    List<Vehicle> findAll() throws SQLException;
    /** Active vehicles that are not assigned to a planned or in-transit route. */
    List<Vehicle> findAvailable() throws SQLException;
}