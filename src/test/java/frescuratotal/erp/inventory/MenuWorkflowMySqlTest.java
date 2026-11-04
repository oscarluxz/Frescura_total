package frescuratotal.erp.inventory;

import frescuratotal.erp.inventory.controller.*;
import frescuratotal.erp.inventory.model.LotStatus;
import frescuratotal.erp.inventory.model.dao.TraceabilityDAO;
import frescuratotal.erp.inventory.view.InventoryConsoleView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.Test;
import static org.junit.Assert.*;

public class MenuWorkflowMySqlTest extends QualityMySqlSupport {
    @Test public void runsQualityWorkflowFromConsoleThroughControllersAndJdbc() throws Exception {
        int lot = addLot(dao.today().plusDays(30));
        String input = "3\n4\n" + lot + "\nDaño\nJunior\n5\n" + lot
                + "\nR\nAna\nDeterioro\nPrimera revisión\n6\n" + lot
                + "\n1\n2\nDeterioro\nJunior\nmenu-op\n7\n" + lot + "\n10\n" + lot
                + "\n5\n" + lot + "\nA\nAna\nCorregido\n8\n1\n9\n999\n2\n0\n";
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            new InventoryConsoleView(new LotController(), new QualityController(dao),
                    new TraceabilityController(new TraceabilityDAO(jdbc))).run(new Scanner(input));
        } finally { System.setOut(original); }
        String text = output.toString(StandardCharsets.UTF_8);
        assertFalse(text, text.contains("No se pudo"));
        assertTrue(text.contains("Cuarentena registrada"));
        assertTrue(text.contains("Inspección registrada"));
        assertTrue(text.contains("Merma registrada"));
        assertTrue(text.contains("Proveedor original"));
        assertTrue(text.contains("Lotes revisados"));
        assertEquals(2, dao.inspectionHistory(lot).size());
        assertEquals(1, count("registro_mermas"));
        assertEquals(LotStatus.AVAILABLE, dao.refreshLot(lot, dao.today()));
    }
}
