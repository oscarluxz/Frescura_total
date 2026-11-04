/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.frescuratotal.erp.sales.repository;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PC
 */
public class CustomerRepositoryImpl implements CustomerRepository {
    private static final Logger LOGGER = Logger.getLogger(CustomerRepositoryImpl.class.getName());

    @Override
    public void save(Customer customer) {
        String sql = "INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) "
           + "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getDocument());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAddress());
            ps.setInt(6, customer.getCustomerType().getDbId());
            ps.executeUpdate();
            
        }  catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Customer insert failed.", e);
            throw new IllegalStateException("Could not save customer", e);
        }
    }

    @Override
    public Customer findById(int id) {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Customer(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("documento"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    CustomerType.fromDbId(rs.getInt("id_tipo"))
                );   
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Customer lookup failed. ID=" + id, e);
            throw new IllegalStateException(
                    "Could not find customer with ID " + id, e
            );
        }
        return null;
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM clientes";
        
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {            
            
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("documento"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    CustomerType.fromDbId(rs.getInt("id_tipo"))
                );
                customers.add(customer);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Customer list query failed.", e);
            throw new IllegalStateException("Could not retrieve customers", e);
        }
        return customers;
        
    }
    

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE clientes SET nombre = ?, documento = ?, email = ?, "
                   + "telefono = ?, direccion = ?, id_tipo = ? WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {            
            
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getDocument());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
             ps.setString(5, customer.getAddress());
            ps.setInt(6, customer.getCustomerType().getDbId());
            ps.setInt(7, customer.getId());
            ps.executeUpdate();
           
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Customer update failed. ID=" + customer.getId(), e);
            throw new IllegalStateException(
                    "Could not update customer with ID " + customer.getId(), e
            );
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {            
            
            ps.setInt(1, id);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Customer delete failed. ID=" + id, e);
            throw new IllegalStateException(
                    "Could not delete customer with ID " + id, e
            );
        }
    }

    
    
    
    
}
    
