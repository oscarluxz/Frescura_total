package frescuratotal.erp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class MainConsoleTest {
    private String run(String input) {
        InputStream previousInput = System.in;
        PrintStream previousOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally { System.setIn(previousInput); System.setOut(previousOutput); }
    }
    @Test public void sharesInputAcrossMainAndInventory() {
        String output = run("1\n3\n0\n0\n");
        assertTrue(output, output.contains("INVENTARIO FRESCURA TOTAL"));
        assertTrue(output, output.contains("¡Hasta pronto!"));
        assertFalse(output, output.contains("=== LOGÍSTICA ==="));
    }
    @Test public void exitsCleanlyAtEofInsideAnotherModule() {
        assertTrue(run("3\n").contains("Entrada finalizada"));
        assertTrue(run("").contains("¡Hasta pronto!"));
    }
}
