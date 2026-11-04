package frescuratotal.erp.logistics.model;

/** Delivery vehicle identified by its license plate. */
public class Vehicle {

    private int id;
    private String plate;
    private String model;
    private double capacity;
    private VehicleStatus status = VehicleStatus.ACTIVE;

    public Vehicle() { }

    public Vehicle(String plate, String model, double capacity) {
        this.plate = plate;
        this.model = model;
        this.capacity = capacity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "[" + id + "] " + plate + " | " + model + " | capacidad " + capacity + " kg | " + status;
    }
}