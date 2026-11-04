package frescuratotal.erp.sales.view;

import frescuratotal.erp.sales.model.AvailableProduct;
import frescuratotal.erp.sales.model.SaleItem;
import frescuratotal.erp.sales.model.SaleReceipt;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SalesView {
    private final Scanner scanner;

    public SalesView(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        System.out.println("\n=== VENTAS Y FACTURACIÓN ===");
        System.out.println("1. Confirmar venta");
        System.out.println("2. Gestión de clientes");
        System.out.println("0. Volver");
    }

    public int readOption() {
        return readInt("Elija una opción: ");
    }

    public void showProducts(List<AvailableProduct> products) {
        if (products.isEmpty()) {
            showMessage("No se encontraron productos con existencias vendibles.");
            return;
        }
        System.out.println("ID | PRODUCTO | UNIDAD | DISPONIBLE | PRECIO DESDE");
        for (AvailableProduct product : products) {
            System.out.printf("%d | %s | %s | %s | %s%n", product.id(), product.name(),
                    product.unit(), product.availableQuantity(), product.startingPrice());
        }
    }

    public int readCustomerId() {
        return readPositiveInt("ID del cliente: ");
    }

    public String readCashier() {
        System.out.print("Nombre del cajero: ");
        return scanner.nextLine().trim();
    }

    public boolean confirmRegistration() {
        System.out.print("¿Confirmar esta venta? (S/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("S");
    }

    public List<SaleItem> readSaleItems() {
        List<SaleItem> items = new ArrayList<>();
        while (true) {
            int productId = readInt("ID del producto (0 para terminar): ");
            if (productId == 0) {
                return items;
            }
            if (productId < 0) {
                showMessage("El ID del producto debe ser positivo.");
                continue;
            }
            BigDecimal quantity = readDecimal("Cantidad: ");
            try {
                items.add(new SaleItem(productId, quantity));
            } catch (IllegalArgumentException e) {
                showMessage(e.getMessage());
            }
        }
    }

    public void showReceipt(SaleReceipt receipt) {
        System.out.println("Venta guardada: " + receipt.saleNumber());
        System.out.println("Factura: " + receipt.invoiceNumber());
        System.out.println("Subtotal: " + receipt.subtotal());
        System.out.println("Descuento: " + receipt.discount());
        System.out.println("IVA (19%): " + receipt.tax());
        System.out.println("Total: " + receipt.total());
        System.out.println("CUFE: " + receipt.cufe());
        System.out.println("PDF: " + receipt.pdfPath());
        System.out.println("QR: " + receipt.qrPath());
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            }
            showMessage("Ingrese un número positivo.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showMessage("Ingrese un número entero válido.");
            }
        }
    }

    private BigDecimal readDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(',', '.');
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                showMessage("Ingrese una cantidad válida, por ejemplo 2 o 1.50.");
            }
        }
    }
}
