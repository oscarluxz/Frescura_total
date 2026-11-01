/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.frescuratotal.erp.sales.service;

import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.repository.CustomerRepository;
import java.util.List;

/**
 *
 * @author PC
 */
public class CustomerService {
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
    }   
    
    public Customer findById(int id) {
        Customer customer = repository.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        return customer;
    }
    
    public List<Customer> findAll() {
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
    }   
    
    public void deleteCustomer(int id) {
        findById(id);
        repository.delete(id);
    }
}
    
