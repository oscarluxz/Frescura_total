/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.frescuratotal.erp.sales.view;

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
}
