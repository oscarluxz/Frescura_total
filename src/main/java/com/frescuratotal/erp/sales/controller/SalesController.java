package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.sales.model.SaleItem;
import com.frescuratotal.erp.sales.model.SaleReceipt;
import com.frescuratotal.erp.sales.service.SalesService;
import com.frescuratotal.erp.common.AppLogger;
import com.frescuratotal.erp.sales.view.SalesView;
import java.util.List;

public class SalesController {
    private final SalesService salesService;
    private final SalesView salesView;
    private final CustomerController customerController;

    public SalesController(SalesService salesService, SalesView salesView,
                           CustomerController customerController) {
        this.salesService = salesService;
        this.salesView = salesView;
        this.customerController = customerController;
    }

    public void start() {
        AppLogger.configure();
        int option;
        do {
            salesView.showMenu();
            option = salesView.readOption();
            switch (option) {
                case 0 -> salesView.showMessage("Returning to the main menu.");
                case 1 -> confirmSale();
                case 2 -> customerController.start();
                default -> salesView.showMessage("Invalid menu option.");
            }
        } while (option != 0);
    }

    private void confirmSale() {
        try {
            salesView.showProducts(salesService.findAvailableProducts());
            int customerId = salesView.readCustomerId();
            List<SaleItem> items = salesView.readSaleItems();
            String cashier = salesView.readCashier();
            if (!salesView.confirmRegistration()) {
                salesView.showMessage("Sale cancelled; no stock or sale records were changed.");
                return;
            }
            SaleReceipt receipt = salesService.confirmSale(customerId, items, cashier);
            salesView.showReceipt(receipt);
        } catch (IllegalArgumentException | IllegalStateException e) {
            salesView.showMessage("Sale could not be confirmed: " + e.getMessage());
        }
    }
}
