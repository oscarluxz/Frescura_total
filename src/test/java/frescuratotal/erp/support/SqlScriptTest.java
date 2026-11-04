package frescuratotal.erp.support;

import org.junit.Test;
import static org.junit.Assert.*;

public class SqlScriptTest {
    @Test public void preservesQuotedSemicolonsEscapedQuotesAndComments() {
        var statements = SqlTestDatabase.statements("-- comentario\nINSERT INTO t VALUES ('texto; y ''comilla'' -- literal'); /* ; */ INSERT INTO t VALUES ('a\\'b'); # fin\n");
        assertEquals(2, statements.size());
        assertTrue(statements.get(0).contains("texto; y ''comilla'' -- literal"));
        assertTrue(statements.get(1).contains("a\\'b"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnclosedText() {
        SqlTestDatabase.statements("INSERT INTO t VALUES ('texto;");
    }
}
