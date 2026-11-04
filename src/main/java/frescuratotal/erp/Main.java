package frescuratotal.erp;

import frescuratotal.erp.inventory.model.dao.InventoryLotSaleValidator;
import frescuratotal.erp.inventory.view.InventoryConsoleView;
import frescuratotal.erp.logistics.controller.LogisticsController;
import frescuratotal.erp.logistics.repository.JdbcDeliveryRouteRepository;
import frescuratotal.erp.logistics.repository.JdbcDispatchOrderRepository;
import frescuratotal.erp.logistics.repository.JdbcDriverRepository;
import frescuratotal.erp.logistics.repository.JdbcVehicleRepository;
import frescuratotal.erp.logistics.view.LogisticsView;
import frescuratotal.erp.sales.SalesModuleFactory;
import frescuratotal.erp.sales.controller.SalesController;
import frescuratotal.erp.suppliers.controller.SupplierController;
import frescuratotal.erp.suppliers.repository.JdbcPurchaseHistoryRepository;
import frescuratotal.erp.suppliers.repository.JdbcSupplierRepository;
import frescuratotal.erp.suppliers.view.SupplierView;

import java.util.Scanner;

/**
 * Application entry point and main menu.
 * Each module is wired here: repositories -> controllers -> views.
 */
public class Main {

    public static void main(String[] args) {
        try { run(); }
        finally { frescuratotal.erp.common.DatabaseConnection.getInstance().shutdown(); }
    }

    private static void run() {
        Scanner scanner = new Scanner(System.in);
        frescuratotal.erp.common.UserSession session = new frescuratotal.erp.common.UserSession();
        frescuratotal.erp.inventory.controller.QualityController qualityController = new frescuratotal.erp.inventory.controller.QualityController(
                new frescuratotal.erp.inventory.model.dao.InventoryQualityDAO(), session);
        InventoryConsoleView inventoryView = new InventoryConsoleView(new frescuratotal.erp.inventory.controller.LotController(),
                qualityController, new frescuratotal.erp.inventory.controller.TraceabilityController());

        SupplierView supplierView = new SupplierView(
                new SupplierController(new JdbcSupplierRepository(), new JdbcPurchaseHistoryRepository()),
                scanner);
        LogisticsView logisticsView = new LogisticsView(
                new LogisticsController(new JdbcVehicleRepository(), new JdbcDriverRepository(),
                        new JdbcDispatchOrderRepository(), new JdbcDeliveryRouteRepository()),
                scanner, "logistica");
        // Sales checks with inventory whether a lot can be sold, through the adapter.
        SalesController salesController = SalesModuleFactory.create(scanner, new InventoryLotSaleValidator());

        boolean exit = false;
        while (!exit) {
            try { qualityController.reviewForToday(); }
            catch (java.sql.SQLException error) { System.out.println("No se pudo completar la revisión diaria: " + error.getMessage()); }
            System.out.println("\n===== ERP FRESCURA TOTAL =====");
            System.out.println("1. Inventario y calidad");
            System.out.println("2. Proveedores");
            System.out.println("3. Logística");
            System.out.println("4. Ventas y facturación");
            System.out.println("5. Cambiar rol de demostración (actual: " + session.getRole() + ")");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            if (!scanner.hasNextLine()) { break; }
            String option = scanner.nextLine().trim();
            try {
            if (option.equals("1")) {
                inventoryView.run(scanner);
            } else if (option.equals("2")) {
                supplierView.run();
            } else if (option.equals("3")) {
                logisticsView.run();
            } else if (option.equals("4")) {
                salesController.start();
            } else if (option.equals("5")) {
                System.out.print("Rol de demostración (1 = Operador, 2 = Inspector): ");
                String role = scanner.nextLine().trim();
                if (role.equals("1") || role.equals("2")) {
                    session.setRole(role.equals("2") ? frescuratotal.erp.common.UserSession.Role.INSPECTOR : frescuratotal.erp.common.UserSession.Role.OPERATOR);
                } else { System.out.println("Rol inválido; se conserva el rol anterior."); }
            } else if (option.equals("0")) {
                exit = true;
            } else {
                System.out.println("Opción inválida.");
            }
            } catch (java.util.NoSuchElementException error) {
                System.out.println("Entrada finalizada. La operación no se completó.");
                break;
            }
        }
        System.out.println("¡Hasta pronto!");
    }
}
