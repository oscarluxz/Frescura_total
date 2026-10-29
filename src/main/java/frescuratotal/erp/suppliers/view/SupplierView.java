package frescuratotal.erp.suppliers.view;

import frescuratotal.erp.suppliers.controller.SupplierController;
import frescuratotal.erp.suppliers.model.CatalogProduct;
import frescuratotal.erp.suppliers.model.Supplier;

import java.sql.SQLException;
import java.util.Scanner;

/** Console view of the suppliers module. */
public class SupplierView {

    private final SupplierController controller;
    private final Scanner scanner;

    public SupplierView(SupplierController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== SUPPLIERS ===");
            System.out.println("1. List suppliers");
            System.out.println("2. Register supplier");
            System.out.println("3. Update supplier");
            System.out.println("4. Deactivate / activate supplier");
            System.out.println("5. View supplier detail and catalog");
            System.out.println("6. Add product to catalog");
            System.out.println("7. Remove product from catalog");
            System.out.println("0. Back");
            int option = readInt("Option: ");
            try {
                switch (option) {
                    case 1:
                        listSuppliers();
                        break;
                    case 2:
                        register();
                        break;
                    case 3:
                        update();
                        break;
                    case 4:
                        toggleActive();
                        break;
                    case 5:
                        showDetail();
                        break;
                    case 6:
                        addProduct();
                        break;
                    case 7:
                        removeProduct();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Validation error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void listSuppliers() throws SQLException {
        for (Supplier supplier : controller.listSuppliers()) {
            System.out.println(supplier);
        }
    }

    private void register() throws SQLException {
        Supplier s = new Supplier(
                readLine("Business name: "), readLine("NIT (e.g. 900123456-7): "),
                readLine("Contact person: "), readLine("Email: "),
                readLine("Phone: "), readLine("Address: "));
        System.out.println("Supplier registered with id " + controller.registerSupplier(s));
    }

    private void update() throws SQLException {
        Supplier s = controller.getSupplier(readInt("Supplier id: "));
        System.out.println("Leave a field empty to keep its current value.");
        s.setBusinessName(readOrKeep("Business name", s.getBusinessName()));
        s.setNit(readOrKeep("NIT", s.getNit()));
        s.setContactPerson(readOrKeep("Contact person", s.getContactPerson()));
        s.setEmail(readOrKeep("Email", s.getEmail()));
        s.setPhone(readOrKeep("Phone", s.getPhone()));
        s.setAddress(readOrKeep("Address", s.getAddress()));
        controller.updateSupplier(s);
        System.out.println("Supplier updated.");
    }

    private void toggleActive() throws SQLException {
        Supplier s = controller.getSupplier(readInt("Supplier id: "));
        if (s.isActive()) {
            controller.deactivateSupplier(s.getId());
            System.out.println("Supplier deactivated.");
        } else {
            controller.activateSupplier(s.getId());
            System.out.println("Supplier activated.");
        }
    }

    private void showDetail() throws SQLException {
        Supplier s = controller.getSupplier(readInt("Supplier id: "));
        System.out.println(s);
        System.out.println("Contact: " + s.getContactPerson() + " | " + s.getEmail() + " | " + s.getPhone());
        System.out.println("Address: " + s.getAddress());
        System.out.println("Catalog:");
        if (s.getCatalog().isEmpty()) {
            System.out.println("  (empty)");
        }
        for (CatalogProduct product : s.getCatalog()) {
            System.out.println("  " + product);
        }
    }

    private void addProduct() throws SQLException {
        int supplierId = readInt("Supplier id: ");
        for (CatalogProduct product : controller.listAllProducts()) {
            System.out.println("  " + product);
        }
        controller.addProductToCatalog(supplierId, readInt("Product id to add: "));
        System.out.println("Catalog updated.");
    }

    private void removeProduct() throws SQLException {
        controller.removeProductFromCatalog(readInt("Supplier id: "), readInt("Product id to remove: "));
        System.out.println("Catalog updated.");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readOrKeep(String label, String current) {
        String value = readLine(label + " [" + current + "]: ");
        if (value.isEmpty()) {
            return current;
        }
        return value;
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}