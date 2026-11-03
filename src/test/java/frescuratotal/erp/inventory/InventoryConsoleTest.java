package frescuratotal.erp.inventory;

import frescuratotal.erp.Main;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class InventoryConsoleTest {

    private String runConsole(String input) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
    }

    @Test public void startsInventoryFromMain() {
        assertTrue(runConsole("0\n").contains("Registrar entrada de lote"));
    }

    @Test public void recoversFromInvalidMenuOption() {
        assertTrue(runConsole("x\n0\n").contains("Opción no válida."));
    }

    @Test public void recoversFromInvalidNumber() {
        assertTrue(runConsole("1\nabc\n0\n").contains("valores numéricos deben ser válidos"));
    }

    @Test public void exitsAtEndOfInput() {
        assertTrue(runConsole("").contains("INVENTARIO FRESCURA TOTAL"));
        assertTrue(runConsole("1\n").contains("El lote no fue registrado."));
    }
}
