package frescuratotal.erp.logistics.view;

import frescuratotal.erp.logistics.controller.LogisticsController;
import frescuratotal.erp.logistics.model.DeliveryRoute;
import frescuratotal.erp.logistics.model.DispatchOrder;
import frescuratotal.erp.logistics.model.Driver;
import frescuratotal.erp.logistics.model.RouteStop;
import frescuratotal.erp.logistics.model.Vehicle;
import frescuratotal.erp.logistics.model.VehicleStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Console view of the logistics module. All visible text is in Spanish. */
public class LogisticsView {

    private final LogisticsController controller;
    private final Scanner scanner;
    private final String currentUser;

    public LogisticsView(LogisticsController controller, Scanner scanner, String currentUser) {
        this.controller = controller;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== LOGÍSTICA ===");
            System.out.println("1. Panel de despacho (pedidos En preparación)");
            System.out.println("2. Crear ruta (asignar pedidos, vehículo y conductor)");
            System.out.println("3. Iniciar ruta (pedidos -> En tránsito)");
            System.out.println("4. Confirmar entrega (pedido -> Entregada)");
            System.out.println("5. Cancelar ruta planificada");
            System.out.println("6. Listar rutas / detalle de ruta");
            System.out.println("7. Vehículos");
            System.out.println("8. Conductores");
            System.out.println("9. Pasar venta registrada a En preparación");
            System.out.println("0. Volver");
            int option = readInt("Opción: ");
            try {
                switch (option) {
                    case 1:
                        showPanel();
                        break;
                    case 2:
                        createRoute();
                        break;
                    case 3:
                        controller.startRoute(readInt("Id de la ruta: "));
                        System.out.println("Ruta iniciada. Sus pedidos pasaron a En tránsito.");
                        break;
                    case 4:
                        confirmDelivery();
                        break;
                    case 5:
                        controller.cancelRoute(readInt("Id de la ruta: "));
                        System.out.println("Ruta cancelada.");
                        break;
                    case 6:
                        showRoutes();
                        break;
                    case 7:
                        vehiclesMenu();
                        break;
                    case 8:
                        driversMenu();
                        break;
                    case 9:
                        controller.prepareSale(readInt("ID de la venta registrada: "));
                        System.out.println("Venta en preparación; ya aparece en el panel de despacho.");
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error de validación: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        }
    }

    // ---------- dispatch panel and routes ----------

    private void showPanel() throws SQLException {
        List<DispatchOrder> orders = controller.getDispatchPanel();
        if (orders.isEmpty()) {
            System.out.println("No hay pedidos En preparación.");
            return;
        }
        System.out.printf("%-6s %-14s %-26s %-28s %12s  %s%n",
                "Id", "Venta", "Cliente", "Dirección", "Total", "Ruta asignada");
        for (DispatchOrder order : orders) {
            String route = "Sin ruta";
            if (order.hasRoute()) {
                route = "Sí: " + order.getAssignedRouteName() + " (id " + order.getAssignedRouteId() + ")";
            }
            System.out.printf("%-6d %-14s %-26s %-28s %12.2f  %s%n", order.getSaleId(), order.getSaleNumber(),
                    shorten(order.getCustomerName(), 26), shorten(order.getDeliveryAddress(), 28),
                    order.getTotalAmount(), route);
        }
    }

    private void createRoute() throws SQLException {
        showPanel();
        List<Integer> saleIds = new ArrayList<>();
        String[] tokens = readLine("Ids de pedidos separados por coma (sin ruta asignada; el orden escrito es el orden de entrega): ")
                .split(",");
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                try {
                    saleIds.add(Integer.parseInt(token.trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("'" + token.trim() + "' no es un id de pedido válido.");
                }
            }
        }
        System.out.println("Vehículos disponibles:");
        for (Vehicle vehicle : controller.listAvailableVehicles()) {
            System.out.println("  " + formatVehicle(vehicle));
        }
        int vehicleId = readInt("Id del vehículo: ");
        System.out.println("Conductores disponibles:");
        for (Driver driver : controller.listAvailableDrivers()) {
            System.out.println("  " + formatDriver(driver));
        }
        int driverId = readInt("Id del conductor: ");
        String name = readLine("Nombre de la ruta: ");
        int id = controller.createRoute(name, vehicleId, driverId, saleIds, currentUser, null, null);
        System.out.println("Ruta creada con id " + id + " (Planificada).");
    }

    private void confirmDelivery() throws SQLException {
        int routeId = readInt("Id de la ruta: ");
        printRoute(controller.getRoute(routeId));
        int saleId = readInt("Id del pedido entregado: ");
        String receivedBy = readLine("Recibido por: ");
        String notes = readLine("Observaciones (opcional): ");
        controller.confirmDelivery(routeId, saleId, receivedBy, notes);
        System.out.println("Entrega confirmada.");
    }

    private void showRoutes() throws SQLException {
        List<DeliveryRoute> routes = controller.listRoutes();
        if (routes.isEmpty()) {
            System.out.println("No hay rutas registradas.");
            return;
        }
        for (DeliveryRoute route : routes) {
            System.out.println(formatRoute(route));
        }
        String id = readLine("Id de la ruta para ver el detalle (vacío para omitir): ");
        if (!id.isEmpty()) {
            try {
                printRoute(controller.getRoute(Integer.parseInt(id)));
            } catch (NumberFormatException e) {
                System.out.println("Id inválido.");
            }
        }
    }

    private void printRoute(DeliveryRoute route) {
        System.out.println(formatRoute(route));
        for (RouteStop stop : route.getStops()) {
            String received = "";
            if (stop.getDeliveredAt() != null) {
                received = " | recibido por " + stop.getReceivedBy();
            }
            System.out.println("  #" + stop.getDeliveryOrder() + " pedido " + stop.getSaleId()
                    + " (" + stop.getSaleNumber() + ") -> " + stop.getDestinationAddress()
                    + " | " + stop.getStatus().getLabel() + received);
        }
    }

    // ---------- vehicles and drivers ----------

    private void vehiclesMenu() throws SQLException {
        System.out.println("1. Listar  2. Registrar  3. Cambiar estado");
        int option = readInt("Opción: ");
        if (option == 1) {
            for (Vehicle vehicle : controller.listVehicles()) {
                System.out.println(formatVehicle(vehicle));
            }
        } else if (option == 2) {
            String plate = readLine("Placa (ABC123): ");
            String model = readLine("Modelo: ");
            double capacity = readDouble("Capacidad (kg): ");
            Vehicle vehicle = new Vehicle(plate, model, capacity);
            System.out.println("Vehículo registrado con id " + controller.registerVehicle(vehicle));
        } else if (option == 3) {
            int id = readInt("Id del vehículo: ");
            VehicleStatus[] statuses = VehicleStatus.values();
            for (int i = 0; i < statuses.length; i++) {
                System.out.println((i + 1) + ". " + statuses[i].getLabel());
            }
            int choice = readInt("Estado: ");
            if (choice < 1 || choice > statuses.length) {
                System.out.println("Estado inválido.");
                return;
            }
            controller.changeVehicleStatus(id, statuses[choice - 1]);
            System.out.println("Estado actualizado.");
        } else {
            System.out.println("Opción inválida.");
        }
    }

    private void driversMenu() throws SQLException {
        System.out.println("1. Listar  2. Registrar  3. Activar/desactivar");
        int option = readInt("Opción: ");
        if (option == 1) {
            for (Driver driver : controller.listDrivers()) {
                System.out.println(formatDriver(driver));
            }
        } else if (option == 2) {
            Driver driver = new Driver(readLine("Nombre: "), readLine("Número de licencia: "),
                    readLine("Teléfono: "), readLine("Correo: "));
            System.out.println("Conductor registrado con id " + controller.registerDriver(driver));
        } else if (option == 3) {
            int id = readInt("Id del conductor: ");
            boolean active = readLine("¿Activo? (s/n): ").equalsIgnoreCase("s");
            controller.setDriverActive(id, active);
            System.out.println("Conductor actualizado.");
        } else {
            System.out.println("Opción inválida.");
        }
    }

    // ---------- formatting and input helpers ----------

    private String formatVehicle(Vehicle vehicle) {
        return "[" + vehicle.getId() + "] " + vehicle.getPlate() + " | " + vehicle.getModel()
                + " | capacidad " + vehicle.getCapacity() + " kg | " + vehicle.getStatus().getLabel();
    }

    private String formatDriver(Driver driver) {
        String state = "Inactivo";
        if (driver.isActive()) {
            state = "Activo";
        }
        return "[" + driver.getId() + "] " + driver.getName() + " | licencia " + driver.getLicenseNumber()
                + " | " + state;
    }

    private String formatRoute(DeliveryRoute route) {
        String text = "[" + route.getId() + "] " + route.getName() + " | " + route.getStatus().getLabel();
        if (route.getVehicle() != null) {
            text = text + " | " + route.getVehicle().getPlate();
        }
        if (route.getDriver() != null) {
            text = text + " | " + route.getDriver().getName();
        }
        return text;
    }

    private String shorten(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }
}
