package frescuratotal.erp.sales.model;

/** Immutable company identity read from the official company table. */
public final class CompanyInfo {
    private final String name;
    private final String nit;
    private final String address;
    private final String phone;
    private final String email;

    public CompanyInfo(String name, String nit, String address, String phone, String email) {
        if (name == null || name.isBlank() || nit == null || nit.isBlank()) {
            throw new IllegalArgumentException("Configure el nombre y NIT de la empresa antes de facturar.");
        }
        this.name = name;
        this.nit = nit;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }
    public String getName() { return name; }
    public String getNit() { return nit; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
