package frescuratotal.erp;

import frescuratotal.erp.inventory.view.InventoryConsoleView;
import frescuratotal.erp.logistics.controller.LogisticsController;
import frescuratotal.erp.logistics.repository.JdbcDeliveryRouteRepository;
import frescuratotal.erp.logistics.repository.JdbcDispatchOrderRepository;
import frescuratotal.erp.logistics.repository.JdbcDriverRepository;
import frescuratotal.erp.logistics.repository.JdbcVehicleRepository;
import frescuratotal.erp.logistics.view.LogisticsView;
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
        Scanner scanner = new Scanner(System.in);

        SupplierView supplierView = new SupplierView(
                new SupplierController(new JdbcSupplierRepository(), new JdbcPurchaseHistoryRepository()),
                scanner);
        LogisticsView logisticsView = new LogisticsView(
                new LogisticsController(new JdbcVehicleRepository(), new JdbcDriverRepository(),
                        new JdbcDispatchOrderRepository(), new JdbcDeliveryRouteRepository()),
                scanner, "logistica");

        boolean exit = false;
        while (!exit) {
            System.out.println("\n===== ERP FRESCURA TOTAL =====");
            System.out.println("1. Inventario y calidad");
            System.out.println("2. Proveedores");
            System.out.println("3. Logística");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String option = scanner.nextLine().trim();
            if (option.equals("1")) {
                new InventoryConsoleView().run();
            } else if (option.equals("2")) {
                supplierView.run();
            } else if (option.equals("3")) {
                logisticsView.run();
            } else if (option.equals("0")) {
                exit = true;
            } else {
                System.out.println("Opción inválida.");
            }
        }
        System.out.println("¡Hasta pronto!");
    }
}
