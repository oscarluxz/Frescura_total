package frescuratotal.erp.inventory.view;

import frescuratotal.erp.inventory.controller.LotController;
import frescuratotal.erp.inventory.model.Lot;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InventoryConsoleView {

    private final LotController controller;

    public InventoryConsoleView() {
        this.controller = new LotController();
    }

    public static void main(String[] args) {
        new InventoryConsoleView().run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n--- INVENTARIO FRESCURA TOTAL ---");
            System.out.println("1. Registrar entrada de lote");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String option = scanner.nextLine().trim();

            if ("1".equals(option)) {
                registerLot(scanner);
            } else if ("0".equals(option)) {
                running = false;
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }

    private void registerLot(Scanner scanner) {
        try {
            System.out.print("ID del producto: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("ID del proveedor: ");
            int supplierId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Fecha de vencimiento (AAAA-MM-DD): ");
            LocalDate expiration = LocalDate.parse(scanner.nextLine().trim());
            System.out.print("Cantidad recibida: ");
            BigDecimal quantity = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Precio de compra: ");
            BigDecimal purchasePrice = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Precio de venta: ");
            BigDecimal salePrice = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Pasillo (opcional): ");
            String aisle = scanner.nextLine();
            System.out.print("Estante (opcional): ");
            String shelf = scanner.nextLine();

            Lot lot = controller.registerLot(productId, supplierId,
                    expiration, quantity, purchasePrice, salePrice, aisle, shelf);
            System.out.println("Lote registrado correctamente: " + lot.getCode());
            System.out.println("Cantidad actual: " + lot.getQuantity());
        } catch (NumberFormatException error) {
            System.out.println("Los ID y los valores numéricos deben ser válidos.");
        } catch (DateTimeParseException error) {
            System.out.println("La fecha debe tener el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException | SQLException error) {
            System.out.println("No se pudo registrar el lote: " + error.getMessage());
        } catch (java.util.NoSuchElementException error) {
            System.out.println("Entrada finalizada. El lote no fue registrado.");
        }
    }
}
