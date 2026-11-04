package frescuratotal.erp.inventory.view;

import frescuratotal.erp.inventory.controller.*;
import frescuratotal.erp.inventory.model.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InventoryConsoleView {
    private final LotController lotController;
    private final QualityController qualityController;
    private final TraceabilityController traceController;

    public InventoryConsoleView() {
        this(new LotController(), new QualityController(), new TraceabilityController());
    }

    public InventoryConsoleView(LotController lotController, QualityController qualityController,
                                TraceabilityController traceController) {
        this.lotController = lotController;
        this.qualityController = qualityController;
        this.traceController = traceController;
    }

    public static void main(String[] args) { new InventoryConsoleView().run(); }
    public void run() { run(new Scanner(System.in)); }

    public void run(Scanner scanner) {
        while (true) {
            System.out.println("\n--- INVENTARIO FRESCURA TOTAL ---");
            System.out.println("1. Registrar entrada de lote");
            System.out.println("2. Actualizar estados y vencimientos");
            System.out.println("3. Consultar existencias y estados");
            System.out.println("4. Registrar cuarentena");
            System.out.println("5. Registrar inspección de calidad");
            System.out.println("6. Registrar merma");
            System.out.println("7. Rastrear lote: proveedor y clientes");
            System.out.println("8. Consultar lotes por proveedor");
            System.out.println("9. Consultar lotes recibidos por cliente");
            System.out.println("10. Consultar historial de inspecciones");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            if (!scanner.hasNextLine()) { return; }
            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "0": return;
                    case "1": registerLot(scanner); break;
                    case "2":
                        System.out.println("Lotes revisados: " + qualityController.refreshStatuses());
                        break;
                    case "3": printLots(traceController.inventory()); break;
                    case "4": quarantine(scanner); break;
                    case "5": inspect(scanner); break;
                    case "6": recordWaste(scanner); break;
                    case "7": printTrace(traceController.traceLot(readId(scanner, "ID del lote: "))); break;
                    case "8": printLots(traceController.lotsBySupplier(readId(scanner, "ID del proveedor: "))); break;
                    case "9": printLots(traceController.lotsByCustomer(readId(scanner, "ID del cliente: "))); break;
                    case "10": printInspections(qualityController.inspectionHistory(readId(scanner, "ID del lote: "))); break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException error) {
                System.out.println("Los ID y los valores numéricos deben ser válidos. Use punto decimal.");
            } catch (DateTimeParseException error) {
                System.out.println("La fecha debe tener el formato AAAA-MM-DD y ser una fecha válida.");
            } catch (IllegalArgumentException | SQLException error) {
                System.out.println("No se pudo completar la operación: " + error.getMessage());
            } catch (NoSuchElementException error) {
                System.out.println("Entrada finalizada. La operación no se completó.");
                return;
            }
        }
    }

    private String read(Scanner scanner, String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
    private int readId(Scanner scanner, String label) { return Integer.parseInt(read(scanner, label)); }

    private void registerLot(Scanner scanner) throws SQLException {
        int product = readId(scanner, "ID del producto: ");
        int supplier = readId(scanner, "ID del proveedor: ");
        LocalDate expiration = LocalDate.parse(read(scanner, "Fecha de vencimiento (AAAA-MM-DD): "));
        BigDecimal quantity = new BigDecimal(read(scanner, "Cantidad recibida: "));
        BigDecimal purchase = new BigDecimal(read(scanner, "Precio de compra: "));
        BigDecimal sale = new BigDecimal(read(scanner, "Precio de venta: "));
        String aisle = read(scanner, "Pasillo (opcional): ");
        String shelf = read(scanner, "Estante (opcional): ");
        Lot lot = lotController.registerLot(product, supplier, expiration, quantity, purchase, sale, aisle, shelf);
        System.out.println("Lote registrado correctamente: " + lot.getCode());
        System.out.println("Cantidad actual: " + lot.getQuantity());
    }

    private void quarantine(Scanner scanner) throws SQLException {
        int lot = readId(scanner, "ID del lote: ");
        String reason = read(scanner, "Motivo de la cuarentena: ");
        String user = read(scanner, "Responsable: ");
        System.out.println("Cuarentena registrada: " + qualityController.quarantine(lot, reason, user));
    }

    private void inspect(Scanner scanner) throws SQLException {
        int lot = readId(scanner, "ID del lote: ");
        String outcome = read(scanner, "Resultado (A = aprobado, R = rechazado): ");
        if (!"A".equalsIgnoreCase(outcome) && !"R".equalsIgnoreCase(outcome)) {
            throw new IllegalArgumentException("Seleccione A o R para el resultado.");
        }
        boolean approved = "A".equalsIgnoreCase(outcome);
        String inspector = read(scanner, "Nombre del inspector: ");
        String reason = approved ? null : read(scanner, "Motivo de rechazo: ");
        String notes = read(scanner, "Notas (opcional): ");
        System.out.println("Inspección registrada: " + qualityController.inspect(lot, approved, inspector, reason, notes));
    }

    private void recordWaste(Scanner scanner) throws SQLException {
        int lot = readId(scanner, "ID del lote: ");
        String inspection = read(scanner, "ID de inspección rechazada (opcional): ");
        Integer inspectionId = inspection.isBlank() ? null : Integer.valueOf(inspection);
        BigDecimal quantity = new BigDecimal(read(scanner, "Cantidad perdida: "));
        String reason = read(scanner, "Causa de la pérdida: ");
        String user = read(scanner, "Responsable: ");
        String key = read(scanner, "Clave única de operación (conserve la misma al reintentar): ");
        System.out.println("Merma registrada: " + qualityController.recordWaste(lot, inspectionId, quantity, reason, user, key));
    }

    private void printLots(List<LotTrace> lots) {
        if (lots.isEmpty()) { System.out.println("No hay lotes para esta consulta."); }
        for (LotTrace trace : lots) {
            Lot lot = trace.getLot();
            System.out.println("ID " + lot.getId() + " | " + lot.getCode() + " | " + trace.getProductName()
                    + " | Existencias: " + lot.getQuantity() + " | Vence: " + lot.getExpirationDate()
                    + " | Estado: " + trace.getStatus().getDatabaseName());
        }
    }

    private void printTrace(LotTrace trace) {
        printLots(List.of(trace));
        System.out.println("Proveedor original: " + trace.getLot().getSupplierId() + " | "
                + trace.getSupplierName() + " | NIT: " + trace.getSupplierNit() + " | " + trace.getSupplierEmail());
        System.out.println("Ubicación: " + trace.getLot().getAisle() + " / " + trace.getLot().getShelf());
        if (trace.getCustomers().isEmpty()) { System.out.println("No hay clientes con entregas confirmadas de este lote."); }
        for (LotCustomer customer : trace.getCustomers()) {
            System.out.println("Cliente " + customer.getId() + " | " + customer.getName() + " | "
                    + customer.getDocument() + " | Cantidad entregada: " + customer.getDeliveredQuantity());
        }
    }

    private void printInspections(List<Inspection> history) {
        if (history.isEmpty()) { System.out.println("El lote no tiene inspecciones registradas."); }
        for (Inspection inspection : history) {
            System.out.println("Inspección " + inspection.getId() + " | " + inspection.getInspectedAt()
                    + " | " + inspection.getResult() + " | Inspector: " + inspection.getInspector()
                    + " | Motivo: " + inspection.getRejectionReason() + " | Notas: " + inspection.getNotes());
        }
    }
}
