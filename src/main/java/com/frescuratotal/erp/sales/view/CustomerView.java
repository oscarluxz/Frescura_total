/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.frescuratotal.erp.sales.view;

import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerType;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author PC
 */
public class CustomerView {
    private final Scanner scan = new Scanner(System.in);
    
    public void showMenu(){
        System.out.println("=== CUSTOMER MENU ===\n");
        System.out.println("1. Register customer ");
        System.out.println("2. List customers ");
        System.out.println("3. Update customer");
        System.out.println("4. Delete customer");
        System.out.println("0. Back");
    }
    
    public int readOption(){
        System.out.print("Choose an option: ");
        String input = scan.nextLine();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid option, enter a number of the menu");
            return -1;
        }
    }
    
    public Customer readNewCustomer() {
        Customer customer = new Customer();
        
        System.out.println("Name: ");
        String name = scan.nextLine();
        customer.setName(name);
        
        System.out.print("Document: ");
        String document = scan.nextLine();
        customer.setDocument(document);
        
        System.out.print("Email: ");
        String email = scan.nextLine();
        customer.setEmail(email);
        
        System.out.print("Phone: ");
        String phone = scan.nextLine();
        customer.setPhone(phone);
        
        System.out.print("Address: ");
        String address = scan.nextLine();
        customer.setAddress(address);
        
        CustomerType customerType = null;

        while (customerType == null) {
            System.out.println("Customer type:");
            System.out.println("1. WHOLESALE");
            System.out.println("2. RETAIL");
            System.out.print("Choose a type: ");

            String typeInput = scan.nextLine();

            try {
                int typeId = Integer.parseInt(typeInput);
                customerType = CustomerType.fromDbId(typeId);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Enter 1 or 2.");
            }
        }

        customer.setCustomerType(customerType);
        
        return customer;
    }
    
    public void showMessage(String message) {
        System.out.println(message);
    }
    
    public void showCustomers(List<Customer> customers) {
        if (customers.isEmpty()) {
            showMessage("No customers found.");
            return;
        }

        for (Customer customer : customers) {
            showMessage(customer.toString());
        }
    }
    
    public int readCustomerId() {
        while (true) {
            System.out.print("Customer ID: ");
            String input = scan.nextLine();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showMessage("Enter a numeric customer ID.");
            }
        }
    }
    
}
