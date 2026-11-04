package frescuratotal.erp.suppliers.view;

import frescuratotal.erp.suppliers.controller.SupplierController;
import frescuratotal.erp.suppliers.model.CatalogProduct;
import frescuratotal.erp.suppliers.model.PurchaseRecord;
import frescuratotal.erp.suppliers.model.Supplier;
import frescuratotal.erp.suppliers.model.SupplierReliability;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Console view of the suppliers module. All user-facing text is in Spanish. */
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
            System.out.println("\n=== PROVEEDORES ===");
            System.out.println("1. Listar proveedores");
            System.out.println("2. Registrar proveedor");
            System.out.println("3. Actualizar proveedor");
            System.out.println("4. Activar / desactivar proveedor");
            System.out.println("5. Ver detalle y catálogo del proveedor");
            System.out.println("6. Agregar producto al catálogo");
            System.out.println("7. Quitar producto del catálogo");
            System.out.println("8. Historial de compras");
            System.out.println("9. Evaluación de confiabilidad");
            System.out.println("0. Volver");
            int option = readInt("Opción: ");
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
                    case 8:
                        showHistory();
                        break;
                    case 9:
                        showReliability();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error de validación: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
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
                readLine("Razón social: "), readLine("NIT (ej. 900123456-7): "),
                readLine("Persona de contacto: "), readLine("Correo electrónico: "),
                readLine("Teléfono: "), readLine("Dirección: "));
        System.out.println("Proveedor registrado con id " + controller.registerSupplier(s));
    }

    private void update() throws SQLException {
        Supplier s = controller.getSupplier(readInt("Id del proveedor: "));
        System.out.println("Deje un campo vacío para conservar su valor actual.");
        s.setBusinessName(readOrKeep("Razón social", s.getBusinessName()));
        s.setNit(readOrKeep("NIT", s.getNit()));
        s.setContactPerson(readOrKeep("Persona de contacto", s.getContactPerson()));
        s.setEmail(readOrKeep("Correo electrónico", s.getEmail()));
        s.setPhone(readOrKeep("Teléfono", s.getPhone()));
        s.setAddress(readOrKeep("Dirección", s.getAddress()));
        controller.updateSupplier(s);
        System.out.println("Proveedor actualizado.");
    }

    private void toggleActive() throws SQLException {
        Supplier s = controller.getSupplier(readInt("Id del proveedor: "));
        if (s.isActive()) {
            controller.deactivateSupplier(s.getId());
            System.out.println("Proveedor desactivado.");
        } else {
            controller.activateSupplier(s.getId());
            System.out.println("Proveedor activado.");
        }
    }

    private void showDetail() throws SQLException {
        Supplier s = controller.getSupplier(readInt("Id del proveedor: "));
        System.out.println(s);
        System.out.println("Contacto: " + s.getContactPerson() + " | " + s.getEmail() + " | " + s.getPhone());
        System.out.println("Dirección: " + s.getAddress());
        System.out.println("Catálogo:");
        if (s.getCatalog().isEmpty()) {
            System.out.println("  (vacío)");
        }
        for (CatalogProduct product : s.getCatalog()) {
            System.out.println("  " + product);
        }
    }

    private void addProduct() throws SQLException {
        int supplierId = readInt("Id del proveedor: ");
        for (CatalogProduct product : controller.listAllProducts()) {
            System.out.println("  " + product);
        }
        controller.addProductToCatalog(supplierId, readInt("Id del producto a agregar: "));
        System.out.println("Catálogo actualizado.");
    }

    private void removeProduct() throws SQLException {
        controller.removeProductFromCatalog(readInt("Id del proveedor: "), readInt("Id del producto a quitar: "));
        System.out.println("Catálogo actualizado.");
    }

    private void showHistory() throws SQLException {
        List<PurchaseRecord> history = controller.getPurchaseHistory(readInt("Id del proveedor: "));
        if (history.isEmpty()) {
            System.out.println("No hay entregas registradas para este proveedor.");
            return;
        }
        System.out.printf("%-14s %-22s %-22s %-12s %10s %10s %-16s%n",
                "Lote", "Producto", "Ingreso", "Vence", "Inicial", "Actual", "Estado");
        for (PurchaseRecord purchase : history) {
            System.out.printf("%-14s %-22s %-22s %-12s %10.2f %10.2f %-16s%n",
                    purchase.getLotNumber(), purchase.getProductName(), purchase.getEntryDate(),
                    purchase.getExpirationDate(), purchase.getInitialQuantity(),
                    purchase.getCurrentQuantity(), purchase.getLotStatus());
        }
    }

    private void showReliability() throws SQLException {
        SupplierReliability r = controller.evaluateSupplier(readInt("Id del proveedor: "));
        System.out.println("Lotes recibidos:          " + r.getTotalLots());
        System.out.println("Cantidad total:           " + r.getTotalQuantity());
        System.out.printf("Cantidad perdida (merma): %.2f (%.2f%%)%n", r.getWastedQuantity(), r.getWastePercentage());
        System.out.println("Inspecciones rechazadas:  " + r.getRejectedInspections());
        System.out.println("Calificación:             " + r.getRating());
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
                System.out.println("Por favor ingrese un número válido.");
            }
        }
    }
}