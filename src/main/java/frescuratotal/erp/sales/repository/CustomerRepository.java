/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package frescuratotal.erp.sales.repository;

import frescuratotal.erp.sales.model.Customer;
import java.util.List;

/**
 *
 * @author PC
 */
public interface CustomerRepository {
    void save(Customer customer);
    Customer findById(int id);
    List<Customer> findAll(); 
    void update(Customer customer);
    void delete(int id);
            
}
