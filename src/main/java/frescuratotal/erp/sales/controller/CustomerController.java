/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frescuratotal.erp.sales.controller;

import frescuratotal.erp.common.AppLogger;
import frescuratotal.erp.sales.model.Customer;
import frescuratotal.erp.sales.service.CustomerService;
import frescuratotal.erp.sales.view.CustomerView;
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
        AppLogger.configure();
        int option = -1;

        while (option != 0) {
            customerView.showMenu();
            option = customerView.readOption();  
            
            switch (option) {
                case 0:
                    customerView.showMessage("Volviendo al menú anterior.");
                    break;
                case 1: {
                    try {
                        Customer customer = customerView.readNewCustomer();
                        customerService.registerCustomer(customer);
                        customerView.showMessage("Cliente registrado correctamente.");
                    } catch (IllegalArgumentException | IllegalStateException e){
                        customerView.showMessage("No se pudo registrar el cliente: " + e.getMessage());
                    }
                    break;
                }
                case 2:
                    try {
                        List<Customer> customers = customerService.findAll();
                        customerView.showCustomers(customers);
                    } catch (IllegalStateException e) {
                        customerView.showMessage("No se pudieron consultar los clientes: " + e.getMessage());
                    }
                    break;
                case 3:
                    try {
                       int customerId = customerView.readCustomerId();
                       Customer updatedCustomer = customerView.readNewCustomer();

                       updatedCustomer.setId(customerId);
                       customerService.updateCustomer(updatedCustomer);

                       customerView.showMessage("Cliente actualizado correctamente.");
                   } catch  (IllegalArgumentException | IllegalStateException e){
                       customerView.showMessage("No se pudo actualizar el cliente: " + e.getMessage());
                   }
                   break;
                case 4:
                    try {
                        int customerId = customerView.readCustomerId();

                        if (customerView.confirmDelete()) {
                            customerService.deleteCustomer(customerId);
                            customerView.showMessage("Cliente eliminado correctamente.");
                        } else {
                            customerView.showMessage("Eliminación de cliente cancelada.");
                        }
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        customerView.showMessage("No se pudo eliminar el cliente: " + e.getMessage());
                    }
                    break;
                default:
                    customerView.showMessage("Opción de menú inválida.");
                    break;
            }
        }
    }
}
