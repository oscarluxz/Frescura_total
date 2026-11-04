package frescuratotal.erp.logistics.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Delivery route: one vehicle + one driver + an ordered list of stops (orders). */
public class DeliveryRoute {

    private int id;
    private String name;
    private Timestamp createdAt;
    private String createdBy;
    private Timestamp estimatedDeparture;
    private Timestamp estimatedArrival;
    private Timestamp actualDeparture;
    private Timestamp actualArrival;
    private RouteStatus status = RouteStatus.PLANNED;
    private Vehicle vehicle;
    private Driver driver;
    private List<RouteStop> stops = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Timestamp getEstimatedDeparture() { return estimatedDeparture; }
    public void setEstimatedDeparture(Timestamp estimatedDeparture) { this.estimatedDeparture = estimatedDeparture; }
    public Timestamp getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(Timestamp estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public Timestamp getActualDeparture() { return actualDeparture; }
    public void setActualDeparture(Timestamp actualDeparture) { this.actualDeparture = actualDeparture; }
    public Timestamp getActualArrival() { return actualArrival; }
    public void setActualArrival(Timestamp actualArrival) { this.actualArrival = actualArrival; }
    public RouteStatus getStatus() { return status; }
    public void setStatus(RouteStatus status) { this.status = status; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public List<RouteStop> getStops() { return stops; }
    public void setStops(List<RouteStop> stops) { this.stops = stops; }

    @Override
    public String toString() {
        String text = "[" + id + "] " + name + " | " + status;
        if (vehicle != null) {
            text += " | " + vehicle.getPlate();
        }
        if (driver != null) {
            text += " | " + driver.getName();
        }
        return text;
    }
}