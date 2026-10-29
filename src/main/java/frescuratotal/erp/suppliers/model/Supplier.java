package frescuratotal.erp.suppliers.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Supplier of the warehouse, together with the catalog of products it can supply. */
public class Supplier {

    private int id;
    private String businessName;
    private String nit;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private boolean active = true;
    private Timestamp registrationDate;
    private List<CatalogProduct> catalog = new ArrayList<>();

    public Supplier() { }

    public Supplier(String businessName, String nit, String contactPerson,
                    String email, String phone, String address) {
        this.businessName = businessName;
        this.nit = nit;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }
    public List<CatalogProduct> getCatalog() { return catalog; }
    public void setCatalog(List<CatalogProduct> catalog) { this.catalog = catalog; }

    @Override
    public String toString() {
        return "[" + id + "] " + businessName + " | NIT " + nit
                + " | " + (active ? "Activo" : "Inactivo");
    }
}