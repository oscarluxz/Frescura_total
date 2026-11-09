package frescuratotal.erp.suppliers.view;

import frescuratotal.erp.suppliers.analytics.SupplierAnalyticsService;
import frescuratotal.erp.suppliers.analytics.SupplierPerformanceReport;

import java.sql.SQLException;
import java.util.Optional;

/** Console view of the supplier performance report. All user-facing text is in Spanish. */
public class SupplierAnalyticsView {

    private final SupplierAnalyticsService service;

    public SupplierAnalyticsView(SupplierAnalyticsService service) {
        this.service = service;
    }

    public void run() {
        try {
            Optional<SupplierPerformanceReport> report = service.generateReport();
            if (report.isPresent()) {
                print(report.get());
            } else {
                System.out.println("No hay datos de inventario suficientes para generar estadísticas.");
            }
        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        }
    }

    private void print(SupplierPerformanceReport report) {
        System.out.println("\n*** Reporte de Rendimiento de Proveedores ***");
        System.out.println("\n>> Proveedor con Mayor Volumen de Suministro:");
        System.out.println(report.topVolumeSupplier());
        System.out.println("\n>> Proveedores con Lotes Vencidos Registrados:");
        if (report.suppliersWithExpiredLots().isEmpty()) {
            System.out.println("Ningún proveedor tiene lotes vencidos registrados.");
        } else {
            report.suppliersWithExpiredLots().forEach(name -> System.out.println("- " + name));
        }
    }
}
