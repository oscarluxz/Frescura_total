package frescuratotal.erp.logistics.model;

/** Driver registered to operate delivery vehicles. */
public class Driver {

    private int id;
    private String name;
    private String licenseNumber;
    private String phone;
    private String email;
    private boolean active = true;

    public Driver() { }

    public Driver(String name, String licenseNumber, String phone, String email) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | licencia " + licenseNumber + " | "
                + (active ? "Activo" : "Inactivo");
    }
}