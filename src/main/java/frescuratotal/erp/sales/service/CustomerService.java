/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frescuratotal.erp.sales.service;

import frescuratotal.erp.sales.model.Customer;
import frescuratotal.erp.sales.repository.CustomerRepository;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author PC
 */
public class CustomerService {
    private static final Logger LOGGER = Logger.getLogger(CustomerService.class.getName());
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }
    
    public void registerCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");       
        }
        if (customer.getDocument() == null || customer.getDocument().isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (customer.getCustomerType() == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        repository.save(customer);
        LOGGER.info("Cliente registrado.");
    }   
    
    public Customer findById(int id) {
        Customer customer = repository.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Cliente no encontrado: " + id);
        }
        return customer;
    }
    
    public List<Customer> findAll() {
        LOGGER.info("Se solicitó la lista de clientes.");
        return repository.findAll();
    }
    
    public void updateCustomer(Customer customer) {
        findById(customer.getId());
        
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");       
        }
        if (customer.getDocument() == null || customer.getDocument().isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (customer.getCustomerType() == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        repository.update(customer);
        LOGGER.info(() -> "Cliente actualizado. ID=" + customer.getId());
    }   
    
    public void deleteCustomer(int id) {
        findById(id);
        repository.delete(id);
        LOGGER.info(() -> "Cliente eliminado. ID=" + id);
    }
}
    
