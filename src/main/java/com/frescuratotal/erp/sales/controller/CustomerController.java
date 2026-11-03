/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.service.CustomerService;
import com.frescuratotal.erp.sales.view.CustomerView;
import java.util.List;

/**
 *
 * @author PC
 */
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerView customerView;

    public CustomerController(CustomerService customerService, CustomerView customerView) {
        this.customerService = customerService;
        this.customerView = customerView;
    }
    
    public void start() {       
        int option = -1;

        while (option != 0) {
            customerView.showMenu();
            option = customerView.readOption();  
            
            switch (option) {
                case 0:
                    System.out.println("Returning to the previous menu.");
                    break;
                case 1: {
                    try {
                        Customer customer = customerView.readNewCustomer();
                        customerService.registerCustomer(customer);
                        customerView.showMessage("Customer registered successfully.");
                    } catch (IllegalArgumentException | IllegalStateException e){
                        customerView.showMessage("Could not register customer: " + e.getMessage());
                    }
                    break;
                }
                case 2:
                    try {
                        List<Customer> customers = customerService.findAll();
                        customerView.showCustomers(customers);
                    } catch (IllegalStateException e) {
                        customerView.showMessage("Could not retrieve customers: " + e.getMessage());
                    }
                    break;
                case 3:
                    try {
                       int customerId = customerView.readCustomerId();
                       Customer updatedCustomer = customerView.readNewCustomer();

                       updatedCustomer.setId(customerId);
                       customerService.updateCustomer(updatedCustomer);

                       customerView.showMessage("Customer updated successfully.");
                   } catch  (IllegalArgumentException | IllegalStateException e){
                       customerView.showMessage("Could not update customer: " + e.getMessage());
                   }
                   break;
                case 4:
                    try {
                       int customerId = customerView.readCustomerId();
                       customerService.deleteCustomer(customerId);
                       customerView.showMessage("Customer deleted successfully.");
                   } catch (IllegalArgumentException | IllegalStateException e) {
                       customerView.showMessage("Could not delete customer: " + e.getMessage());
                   }
                   break;
                default:
                    System.out.println("Invalid menu option.");
                    break;
            }
        }
    }
}
