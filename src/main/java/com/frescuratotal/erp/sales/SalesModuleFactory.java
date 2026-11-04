package com.frescuratotal.erp.sales;

import com.frescuratotal.erp.sales.controller.CustomerController;
import com.frescuratotal.erp.sales.controller.SalesController;
import com.frescuratotal.erp.sales.repository.CustomerRepositoryImpl;
import com.frescuratotal.erp.sales.repository.LotSaleValidator;
import com.frescuratotal.erp.sales.repository.SalesRepositoryImpl;
import com.frescuratotal.erp.sales.service.CustomerService;
import com.frescuratotal.erp.sales.service.InvoiceFileService;
import com.frescuratotal.erp.sales.service.SalesService;
import com.frescuratotal.erp.sales.view.CustomerView;
import com.frescuratotal.erp.sales.view.SalesView;
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
