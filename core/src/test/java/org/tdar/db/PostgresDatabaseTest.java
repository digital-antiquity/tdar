package org.tdar.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.tdar.db.model.PostgresDatabase;

public class PostgresDatabaseTest {

    @Test
    public void testTableNormalization() {
        PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();

        assertEquals("col_blank", tdarDataImportDatabase.normalizeTableNames(""));
        assertEquals("__", tdarDataImportDatabase.normalizeTableNames("()"));
        assertEquals("col_select", tdarDataImportDatabase.normalizeTableNames("SELECT"));
        assertEquals("ab_cd123__________asd", tdarDataImportDatabase.normalizeTableNames("AB_cd123~@# &*()\\ asd   "));
        // should be 53 chars
        assertEquals("c123456789012345678901234567890123456789012345678901",
                tdarDataImportDatabase.normalizeTableNames("1234567890123456789012345678901234567890123456789012345678901234567890"));
    }

    @Test
    public void testColumnNormalization() {
        PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();

        assertEquals("col_blank", tdarDataImportDatabase.normalizeColumnNames(""));
        assertEquals("__", tdarDataImportDatabase.normalizeColumnNames("()"));
        assertEquals("col_select", tdarDataImportDatabase.normalizeColumnNames("SELECT"));
        assertEquals("ab_cd123__________asd", tdarDataImportDatabase.normalizeColumnNames("AB_cd123~@# &*()\\ asd   "));
        // should be 43 chars
        assertEquals("c1234567890123456789012345678901234567890123456789012",
                tdarDataImportDatabase.normalizeColumnNames("1234567890123456789012345678901234567890123456789012345678901234567890",true));
        // should be 63 chars
        assertEquals("c123456789012345678901234567890123456789012",
                tdarDataImportDatabase.normalizeColumnNames("1234567890123456789012345678901234567890123456789012345678901234567890"));
    }

}
