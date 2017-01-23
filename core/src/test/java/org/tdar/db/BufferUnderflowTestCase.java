package org.tdar.db;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

public class BufferUnderflowTestCase {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TEST_FOLDER = TestConstants.TEST_DATA_INTEGRATION_DIR;
    private static final String TEST_FILE = "buffer-underflow-example.mdb";
    private static final String TABLE_NAME = "Site File";

    private Database database;

    @Before
    public void prepare() throws IOException {
        database = DatabaseBuilder.open(new File(TEST_FOLDER, TEST_FILE));
    }

    @Test
    public void testTableDisplayThrowsException() throws IOException {
        // FIXME: MAKE ASSERTION
        database.getTable(TABLE_NAME).toString();
    }

    @Test
    public void testTableGetNextRowThrowsException() throws IOException {
        // FIXME: MAKE ASSERTION
        Table currentTable = database.getTable(TABLE_NAME);
        int rowCount = currentTable.getRowCount();

        for (int i = 0; i < rowCount; ++i) {
            logger.debug("processing row {}", i);
            Map<String, Object> currentRow = currentTable.getNextRow();
            if (currentRow == null) {
                continue;
            }
            for (Object currentObject : currentRow.entrySet()) {
                if (currentObject != null) {
                    String currentObjectAsString = currentObject.toString();
                    logger.debug("\t{}", currentObjectAsString);
                }
            }
        }
    }

}
