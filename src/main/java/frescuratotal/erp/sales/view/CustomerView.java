/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frescuratotal.erp.sales.view;

import frescuratotal.erp.sales.model.Customer;
import frescuratotal.erp.sales.model.CustomerType;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author PC
 */
public class CustomerView {
    private final Scanner scan;

    public CustomerView() {
        this(new Scanner(System.in));
    }

    public CustomerView(Scanner scan) {
        this.scan = scan;
    }

    public void showMenu(){
        System.out.println("=== MENÚ DE CLIENTES ===\n");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Actualizar cliente");
        System.out.println("4. Eliminar cliente");
        System.out.println("0. Volver");
    }

    public int readOption(){
        System.out.print("Elija una opción: ");
        String input = scan.nextLine();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Opción inválida, ingrese un número del menú");
            return -1;
        }
    }

    public Customer readNewCustomer() {
        Customer customer = new Customer();

        System.out.print("Nombre: ");
        String name = scan.nextLine();
        customer.setName(name);

        System.out.print("Documento: ");
        String document = scan.nextLine();
        customer.setDocument(document);

        System.out.print("Correo electrónico: ");
        String email = scan.nextLine();
        customer.setEmail(email);

        System.out.print("Teléfono: ");
        String phone = scan.nextLine();
        customer.setPhone(phone);

        System.out.print("Dirección: ");
        String address = scan.nextLine();
        customer.setAddress(address);

        CustomerType customerType = null;

        while (customerType == null) {
            System.out.println("Tipo de cliente:");
            System.out.println("1. MAYORISTA");
            System.out.println("2. MINORISTA");
            System.out.print("Elija un tipo: ");

            String typeInput = scan.nextLine();

            try {
                int typeId = Integer.parseInt(typeInput);
                customerType = CustomerType.fromDbId(typeId);
            } catch (IllegalArgumentException e) {
                System.out.println("Tipo inválido. Ingrese 1 o 2.");
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
            showMessage("No se encontraron clientes.");
            return;
        }

        for (Customer customer : customers) {
            showMessage(customer.toString());
        }
    }

    public int readCustomerId() {
        while (true) {
            System.out.print("ID del cliente: ");
            String input = scan.nextLine();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showMessage("Ingrese un ID de cliente numérico.");
            }
        }
    }

    public boolean confirmDelete() {
        System.out.print("¿Está seguro de eliminar este cliente? (S/N): ");
        String answer = scan.nextLine();
        return answer.equalsIgnoreCase("S");
    }

}
