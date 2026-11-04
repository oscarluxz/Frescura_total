package frescuratotal.erp.logistics.controller;

import frescuratotal.erp.logistics.model.DeliveryRoute;
import frescuratotal.erp.logistics.model.DispatchOrder;
import frescuratotal.erp.logistics.model.Driver;
import frescuratotal.erp.logistics.model.Vehicle;
import frescuratotal.erp.logistics.model.VehicleStatus;
import frescuratotal.erp.logistics.repository.DeliveryRouteRepository;
import frescuratotal.erp.logistics.repository.DispatchOrderRepository;
import frescuratotal.erp.logistics.repository.DriverRepository;
import frescuratotal.erp.logistics.repository.VehicleRepository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business rules of the logistics module: vehicles, drivers, dispatch panel and
 * the delivery cycle IN_PREPARATION -> IN_TRANSIT -> DELIVERED.
 */
public class LogisticsController {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final DispatchOrderRepository dispatchRepository;
    private final DeliveryRouteRepository routeRepository;

    public LogisticsController(VehicleRepository vehicleRepository,
                               DriverRepository driverRepository,
                               DispatchOrderRepository dispatchRepository,
                               DeliveryRouteRepository routeRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.dispatchRepository = dispatchRepository;
        this.routeRepository = routeRepository;
    }

    // ----- vehicles -----

    public int registerVehicle(Vehicle vehicle) throws SQLException {
        String plate = "";
        if (vehicle.getPlate() != null) {
            plate = vehicle.getPlate().trim().toUpperCase().replace("-", "");
        }
        if (!isValidPlate(plate)) {
            throw new IllegalArgumentException("Placa inválida. Formato esperado: ABC123.");
        }
        if (vehicle.getCapacity() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        if (vehicleRepository.existsByPlate(plate)) {
            throw new IllegalArgumentException("La placa " + plate + " ya está registrada.");
        }
        vehicle.setPlate(plate);
        return vehicleRepository.save(vehicle);
    }

    /** Three letters followed by three digits. */
    private boolean isValidPlate(String plate) {
        if (plate.length() != 6) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            char c = plate.charAt(i);
            if (i < 3 && !Character.isLetter(c)) {
                return false;
            }
            if (i >= 3 && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public void changeVehicleStatus(int vehicleId, VehicleStatus status) throws SQLException {
        vehicleRepository.updateStatus(vehicleId, status);
    }

    public List<Vehicle> listVehicles() throws SQLException {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> listAvailableVehicles() throws SQLException {
        return vehicleRepository.findAvailable();
    }

    // ----- drivers -----

    public int registerDriver(Driver driver) throws SQLException {
        if (driver.getName() == null || driver.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del conductor es obligatorio.");
        }
        if (driver.getLicenseNumber() == null || driver.getLicenseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de licencia es obligatorio.");
        }
        if (driverRepository.existsByLicense(driver.getLicenseNumber())) {
            throw new IllegalArgumentException("La licencia " + driver.getLicenseNumber() + " ya está registrada.");
        }
        return driverRepository.save(driver);
    }

    public void setDriverActive(int driverId, boolean active) throws SQLException {
        driverRepository.setActive(driverId, active);
    }

    public List<Driver> listDrivers() throws SQLException {
        return driverRepository.findAll();
    }

    public List<Driver> listAvailableDrivers() throws SQLException {
        return driverRepository.findAvailable();
    }

    // ----- dispatch panel and routes -----

    /** Dispatch panel: every sale IN_PREPARATION, with its route when it already has one. */
    public List<DispatchOrder> getDispatchPanel() throws SQLException {
        return dispatchRepository.findInPreparation();
    }

    public int createRoute(String routeName, int vehicleId, int driverId, List<Integer> saleIds,
                           String user, Timestamp estimatedDeparture, Timestamp estimatedArrival)
            throws SQLException {
        if (routeName == null || routeName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio.");
        }
        if (saleIds == null || saleIds.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un pedido para la ruta.");
        }
        if (new HashSet<>(saleIds).size() != saleIds.size()) {
            throw new IllegalArgumentException("Un pedido fue seleccionado más de una vez.");
        }

        Vehicle selectedVehicle = null;
        for (Vehicle vehicle : listAvailableVehicles()) {
            if (vehicle.getId() == vehicleId) {
                selectedVehicle = vehicle;
            }
        }
        if (selectedVehicle == null) {
            throw new IllegalArgumentException("El vehículo " + vehicleId + " no está disponible.");
        }

        Driver selectedDriver = null;
        for (Driver driver : listAvailableDrivers()) {
            if (driver.getId() == driverId) {
                selectedDriver = driver;
            }
        }
        if (selectedDriver == null) {
            throw new IllegalArgumentException("El conductor " + driverId + " no está disponible.");
        }

        // Only orders that are IN_PREPARATION and not yet on an active route can be added.
        Set<Integer> unassignedSaleIds = new HashSet<>();
        for (DispatchOrder order : getDispatchPanel()) {
            if (!order.hasRoute()) {
                unassignedSaleIds.add(order.getSaleId());
            }
        }
        for (int saleId : saleIds) {
            if (!unassignedSaleIds.contains(saleId)) {
                throw new IllegalArgumentException("El pedido " + saleId
                        + " no está En preparación o ya tiene una ruta asignada.");
            }
        }

        DeliveryRoute route = new DeliveryRoute();
        route.setName(routeName.trim());
        route.setCreatedBy(user);
        route.setEstimatedDeparture(estimatedDeparture);
        route.setEstimatedArrival(estimatedArrival);
        route.setVehicle(selectedVehicle);
        route.setDriver(selectedDriver);
        return routeRepository.create(route, saleIds);
    }

    public void startRoute(int routeId) throws SQLException {
        routeRepository.start(routeId);
    }

    public void confirmDelivery(int routeId, int saleId, String receivedBy, String observations)
            throws SQLException {
        if (receivedBy == null || receivedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de quien recibió el pedido es obligatorio.");
        }
        routeRepository.markDelivered(routeId, saleId, receivedBy.trim(), observations);
    }

    public void cancelRoute(int routeId) throws SQLException {
        routeRepository.cancel(routeId);
    }

    public List<DeliveryRoute> listRoutes() throws SQLException {
        return routeRepository.findAll();
    }

    public DeliveryRoute getRoute(int routeId) throws SQLException {
        DeliveryRoute route = routeRepository.findById(routeId);
        if (route == null) {
            throw new IllegalArgumentException("No se encontró la ruta " + routeId + ".");
        }
        return route;
    }
}
