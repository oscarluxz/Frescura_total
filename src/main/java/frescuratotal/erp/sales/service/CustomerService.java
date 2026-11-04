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
            throw new IllegalArgumentException("Name is required");       
        }
        if (customer.getDocument() == null || customer.getDocument().isBlank()) {
            throw new IllegalArgumentException("Document is required");
        }
        if (customer.getCustomerType() == null) {
            throw new IllegalArgumentException("Customer type is required");
        }
        repository.save(customer);
        LOGGER.info("Customer registered.");
    }   
    
    public Customer findById(int id) {
        Customer customer = repository.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        return customer;
    }
    
    public List<Customer> findAll() {
        LOGGER.info("Customer list requested.");
        return repository.findAll();
    }
    
    public void updateCustomer(Customer customer) {
        findById(customer.getId());
        
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");       
        }
        if (customer.getDocument() == null || customer.getDocument().isBlank()) {
            throw new IllegalArgumentException("Document is required");
        }
        if (customer.getCustomerType() == null) {
            throw new IllegalArgumentException("Customer type is required");
        }
        repository.update(customer);
        LOGGER.info(() -> "Customer updated. ID=" + customer.getId());
    }   
    
    public void deleteCustomer(int id) {
        findById(id);
        repository.delete(id);
        LOGGER.info(() -> "Customer deleted. ID=" + id);
    }
}
    
