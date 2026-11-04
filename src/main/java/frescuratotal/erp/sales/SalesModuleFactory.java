package frescuratotal.erp.sales;

import frescuratotal.erp.sales.controller.CustomerController;
import frescuratotal.erp.sales.controller.SalesController;
import frescuratotal.erp.sales.repository.CustomerRepositoryImpl;
import frescuratotal.erp.sales.repository.LotSaleValidator;
import frescuratotal.erp.sales.repository.SalesRepositoryImpl;
import frescuratotal.erp.sales.service.CustomerService;
import frescuratotal.erp.sales.service.InvoiceFileService;
import frescuratotal.erp.sales.service.SalesService;
import frescuratotal.erp.sales.view.CustomerView;
import frescuratotal.erp.sales.view.SalesView;
import java.util.Scanner;

/** Wires the sales and customer layers for the application entry point. */
public final class SalesModuleFactory {
    private SalesModuleFactory() {
    }

    public static SalesController create(Scanner scanner, LotSaleValidator lotSaleValidator) {
        CustomerService customerService = new CustomerService(new CustomerRepositoryImpl());
        CustomerController customerController = new CustomerController(
                customerService, new CustomerView(scanner));
        SalesService salesService = new SalesService(new SalesRepositoryImpl(
                lotSaleValidator, new InvoiceFileService()));
        return new SalesController(salesService, new SalesView(scanner), customerController);
    }
}
