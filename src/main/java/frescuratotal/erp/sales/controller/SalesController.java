package frescuratotal.erp.sales.controller;

import frescuratotal.erp.sales.model.SaleItem;
import frescuratotal.erp.sales.model.SaleReceipt;
import frescuratotal.erp.sales.service.SalesService;
import frescuratotal.erp.common.AppLogger;
import frescuratotal.erp.sales.view.SalesView;
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
                case 0 -> salesView.showMessage("Volviendo al menú principal.");
                case 1 -> confirmSale();
                case 2 -> customerController.start();
                default -> salesView.showMessage("Opción de menú inválida.");
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
                salesView.showMessage("Venta cancelada; no se modificó el inventario ni los registros de venta.");
                return;
            }
            SaleReceipt receipt = salesService.confirmSale(customerId, items, cashier);
            salesView.showReceipt(receipt);
        } catch (IllegalArgumentException | IllegalStateException e) {
            salesView.showMessage("No se pudo confirmar la venta: " + e.getMessage());
        }
    }
}
