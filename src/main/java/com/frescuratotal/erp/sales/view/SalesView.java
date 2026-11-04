package com.frescuratotal.erp.sales.view;

import com.frescuratotal.erp.sales.model.AvailableProduct;
import com.frescuratotal.erp.sales.model.SaleItem;
import com.frescuratotal.erp.sales.model.SaleReceipt;
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
        System.out.println("\n=== SALES AND BILLING ===");
        System.out.println("1. Confirm sale");
        System.out.println("2. Customer management");
        System.out.println("0. Back");
    }

    public int readOption() {
        return readInt("Choose an option: ");
    }

    public void showProducts(List<AvailableProduct> products) {
        if (products.isEmpty()) {
            showMessage("No products with sellable stock were found.");
            return;
        }
        System.out.println("ID | PRODUCT | UNIT | AVAILABLE | STARTING PRICE");
        for (AvailableProduct product : products) {
            System.out.printf("%d | %s | %s | %s | %s%n", product.id(), product.name(),
                    product.unit(), product.availableQuantity(), product.startingPrice());
        }
    }

    public int readCustomerId() {
        return readPositiveInt("Customer ID: ");
    }

    public String readCashier() {
        System.out.print("Cashier name: ");
        return scanner.nextLine().trim();
    }

    public boolean confirmRegistration() {
        System.out.print("Confirm this sale? (Y/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("Y");
    }

    public List<SaleItem> readSaleItems() {
        List<SaleItem> items = new ArrayList<>();
        while (true) {
            int productId = readInt("Product ID (0 to finish): ");
            if (productId == 0) {
                return items;
            }
            if (productId < 0) {
                showMessage("Product ID must be positive.");
                continue;
            }
            BigDecimal quantity = readDecimal("Quantity: ");
            try {
                items.add(new SaleItem(productId, quantity));
            } catch (IllegalArgumentException e) {
                showMessage(e.getMessage());
            }
        }
    }

    public void showReceipt(SaleReceipt receipt) {
        System.out.println("Sale saved: " + receipt.saleNumber());
        System.out.println("Invoice: " + receipt.invoiceNumber());
        System.out.println("Subtotal: " + receipt.subtotal());
        System.out.println("Discount: " + receipt.discount());
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
            showMessage("Enter a positive number.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showMessage("Enter a valid whole number.");
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
                showMessage("Enter a valid quantity, for example 2 or 1.50.");
            }
        }
    }
}
