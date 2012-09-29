package org.tdar.db;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Test;
import org.tdar.db.model.PostgresDatabase;

public class PostgresDatabaseTest {

    @Test
    public void testNormalization() {
        PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();

        assertEquals("col_blank",tdarDataImportDatabase.normalizeTableOrColumnNames(""));
        assertEquals("col_select", tdarDataImportDatabase.normalizeTableOrColumnNames("SELECT"));
        assertEquals("ab_cd123__________asd", tdarDataImportDatabase.normalizeTableOrColumnNames("AB_cd123~@# &*()\\ asd   "));
        String original = "c123456789012345678901234567890123456789012345678901234567890123";
        String expected = original.substring(0, PostgresDatabase.MAX_NAME_SIZE );
        assertEquals(expected, tdarDataImportDatabase.normalizeTableOrColumnNames(original));
    }

}
