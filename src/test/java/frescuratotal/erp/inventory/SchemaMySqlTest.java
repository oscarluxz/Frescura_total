package frescuratotal.erp.inventory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SchemaMySqlTest extends QualityMySqlSupport {
    @Test public void verifiesEveryDeclaredForeignKeyAndDecimalContract() throws Exception {
        String source = Files.readString(Path.of("docs/database.sql"));
        assertFalse(source.contains("DROP DATABASE"));
        Pattern tables = Pattern.compile("CREATE TABLE(?: IF NOT EXISTS)? (\\w+) \\(([\\s\\S]*?)\\) ENGINE=InnoDB;");
        Pattern foreign = Pattern.compile("FOREIGN KEY \\((\\w+)\\) REFERENCES (\\w+)\\((\\w+)\\)");
        Matcher table = tables.matcher(source);
        int tableCount = 0;
        int relationshipCount = 0;
        try (Connection connection = jdbc.open()) {
            DatabaseMetaData metadata = connection.getMetaData();
            while (table.find()) {
                tableCount++;
                Set<String> expected = new HashSet<>();
                Matcher key = foreign.matcher(table.group(2));
                while (key.find()) { expected.add(key.group(1) + ":" + key.group(2) + ":" + key.group(3)); }
                Set<String> actual = new HashSet<>();
                try (ResultSet result = metadata.getImportedKeys(database, null, table.group(1))) {
                    while (result.next()) {
                        actual.add(result.getString("FKCOLUMN_NAME") + ":" + result.getString("PKTABLE_NAME") + ":" + result.getString("PKCOLUMN_NAME"));
                    }
                }
                assertEquals(table.group(1), expected, actual);
                relationshipCount += actual.size();
            }
            assertEquals(23, tableCount);
            assertEquals(24, relationshipCount);
            try (ResultSet columns = metadata.getColumns(database, null, "lotes_inventario", "cantidad")) {
                assertTrue(columns.next());
                assertEquals(10, columns.getInt("COLUMN_SIZE"));
                assertEquals(2, columns.getInt("DECIMAL_DIGITS"));
            }
            Set<String> uniqueColumns = new HashSet<>();
            try (ResultSet indexes = metadata.getIndexInfo(database, null, "operaciones_merma", true, false)) {
                while (indexes.next()) { uniqueColumns.add(indexes.getString("COLUMN_NAME")); }
            }
            assertTrue(uniqueColumns.contains("clave_operacion"));
            assertTrue(uniqueColumns.contains("id_registro"));
        }
    }
}
