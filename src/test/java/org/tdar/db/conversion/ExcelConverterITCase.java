package org.tdar.db.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ExcelConverter;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class ExcelConverterITCase extends AbstractDataIntegrationTestCase {


    @Test
    @Rollback
    public void testBlankExceedingRows() throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(new File(getTestFilePath(), "Pundo faunal remains.xls"), 509);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        converter.execute();
        Set<DataTable> dataTables = converter.getDataTables();
        assertEquals(1, dataTables.size());
        DataTable table = dataTables.iterator().next();
        List<DataTableColumn> dataTableColumns = table.getDataTableColumns();
        assertEquals(5, dataTableColumns.size());
        int i = 1; // 0 is the tDAR ID Column which may not be returned
        for (DataTableColumn col : table.getSortedDataTableColumns()) {
            logger.debug("{} : {}", col.getSequenceNumber(), col);
            assertEquals(new Integer(i), col.getSequenceNumber());
            i++;
        }
    }

    @Test
    @Rollback
    public void testBlankExceedingRowsAndExtraColumnAtEnd() throws IOException {
        importSpreadsheetAndConfirmExceptionIsThrown(new File(getTestFilePath(), "Pundo_degenerate.xls"),
                "row #49 has more columns (6) than this sheet has column names (5) - Appendix 8 (2)");
    }

    @Test
    @Rollback
    public void testExtraColumnAtStartThrowsException() throws IOException {
        importSpreadsheetAndConfirmExceptionIsThrown(new File(getTestFilePath(), "no_first_column_name.xlsx"), "row #1 has more columns (0) than this sheet has column names (1) - Sheet1");
    }

    private void importSpreadsheetAndConfirmExceptionIsThrown(File spreadsheet, String expectedErrorMessage) throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(spreadsheet, 529);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        try {
            converter.execute();
            assertTrue("Should never get to this point in the code.", false);
        } catch (TdarRecoverableRuntimeException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    @Rollback
    public void testArtifactDatasetFromFilemaker() throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(new File(getTestFilePath(), "fmp_artifacts.xlsx"), 505);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        converter.execute();
        Set<DataTable> dataTables = converter.getDataTables();
        assertEquals(1, dataTables.size());
        DataTable dataTable = dataTables.iterator().next();
        assertNotNull(dataTable.getColumnByDisplayName("Period"));
        // assertNotNull(dataTable.getColumnByDisplayName("SumOfNo"));
        // assertNotNull(dataTable.getColumnByDisplayName("1.00"));
        // assertNotNull(dataTable.getColumnByDisplayName("ABC"));
        // assertNotNull(dataTable.getColumnByName("period"));
        // assertNotNull(dataTable.getColumnByName("sumofno"));
        // assertNotNull(dataTable.getColumnByName("c1_00"));
        // assertNotNull(dataTable.getColumnByName("abc"));
    }

    @Test
    @Rollback
    public void testColumnNameFormatIssues() throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(new File(getTestFilePath(), "weird_column_headings.xlsx"), 502);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        converter.execute();
        Set<DataTable> dataTables = converter.getDataTables();
        assertEquals(1, dataTables.size());
        DataTable dataTable = dataTables.iterator().next();
        assertNotNull(dataTable.getColumnByDisplayName("Period"));
        assertNotNull(dataTable.getColumnByDisplayName("SumOfNo"));
        assertNotNull(dataTable.getColumnByDisplayName("1.00"));
        assertNotNull(dataTable.getColumnByDisplayName("ABC"));
        assertNotNull(dataTable.getColumnByName("period"));
        assertNotNull(dataTable.getColumnByName("sumofno"));
        assertNotNull(dataTable.getColumnByName("c1_00"));
        assertNotNull(dataTable.getColumnByName("abc"));
    }

    @Test
    @Rollback
    public void testConverterAllStrings() throws Exception {
        InformationResourceFileVersion datasetTextOnly = makeFileVersion(new File(getTestFilePath(), "dataset_all_text.xls"), 500);
        File storedFile = filestore.retrieveFile(datasetTextOnly);
        assertTrue("text file exists", storedFile.exists());

        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, datasetTextOnly);
        converter.execute();
        DataTable table = converter.getDataTables().iterator().next();
        assertTrue("table created", table.getName().indexOf("dataset_all_text") > 0);

        // confirm that all the columns in the new table are varchar
        tdarDataImportDatabase.selectAllFromTableInImportOrder(table,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        assertEquals(Types.VARCHAR, meta.getColumnType(1));
                        assertEquals(Types.VARCHAR, meta.getColumnType(2));
                        assertEquals(Types.VARCHAR, meta.getColumnType(3));
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback
    public void testConverterWithInts() throws Exception {
        InformationResourceFileVersion datasetWithInts = makeFileVersion(new File(getTestFilePath(), "dataset_with_ints.xls"), 501);
        File storedFile = filestore.retrieveFile(datasetWithInts);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithInts, tdarDataImportDatabase);

        converter.execute();
        DataTable table = converter.getDataTables().iterator().next();
        assertTrue("table created", table.getName().indexOf("sheet1") == -1);
        assertTrue("table created", table.getName().indexOf("dataset_with_ints") > 0);

        // confirm that all the columns in the new table are ints
        tdarDataImportDatabase.selectAllFromTableInImportOrder(table,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        assertEquals(Types.BIGINT, meta.getColumnType(2));
                        assertEquals(Types.BIGINT, meta.getColumnType(3));
                        assertEquals(Types.BIGINT, meta.getColumnType(5));
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback
    public void testConverterWithDates() throws Exception {
        InformationResourceFileVersion datasetWithDates = makeFileVersion(new File(getTestFilePath(), "dataset_with_dates.xls"), 592);
        File storedFile = filestore.retrieveFile(datasetWithDates);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithDates, tdarDataImportDatabase);

        converter.execute();
        DataTable table = converter.getDataTables().iterator().next();
        assertTrue("table created", table.getName().indexOf("sheet1") == -1);
        assertTrue("table created", table.getName().indexOf("dataset_with_dates") > 0);

        // confirm that all the columns in the new table are dates
        tdarDataImportDatabase.selectAllFromTableInImportOrder(table,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        assertEquals(Types.VARCHAR, meta.getColumnType(1));
                        assertEquals(Types.TIMESTAMP, meta.getColumnType(2));
                        assertEquals(Types.BIGINT, meta.getColumnType(3));
                        assertEquals(Types.BIGINT, meta.getColumnType(4));
                        assertEquals(Types.DOUBLE, meta.getColumnType(5));
                        assertEquals(Types.VARCHAR, meta.getColumnType(6));
                        rs.next();
                        final Date date = rs.getDate(2);
                        // I know that getYear, getMonth and getDate are deprecated, but this just seemed the simplest.
                        assertTrue("Year should be 2003: " + date.getYear(), date.getYear() == 2003 - 1900);
                        assertTrue("Month should be 1: " + date.getMonth(), date.getMonth() == 1);
                        assertTrue("Day should be 1: " + date.getDate(), date.getDate() == 1);
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback
    public void testConverterWithFloats() throws Exception {
        InformationResourceFileVersion datasetWithFloats = makeFileVersion(new File(getTestFilePath(), "dataset_with_floats.xls"), 502);
        File storedFile = filestore.retrieveFile(datasetWithFloats);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithFloats, tdarDataImportDatabase);
        converter.execute();
        DataTable table = converter.getDataTables().iterator().next();
        assertTrue("table created", table.getName().indexOf("dataset_with_floats") > 0);

        // confirm that all the columns in the new table are ints
        tdarDataImportDatabase.selectAllFromTableInImportOrder(table,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        assertEquals(Types.VARCHAR, meta.getColumnType(1));
                        assertEquals(Types.DOUBLE, meta.getColumnType(2));
                        return null;
                    }
                }, false);
    }

    // TODO: break this into more granular tests.
    @Test
    @Rollback
    public void testConverterWithMultipleSheetsAndHiddenFields()
            throws Exception {
        InformationResourceFileVersion datasetWithHiddenFields = makeFileVersion(new File(getTestFilePath(), "england_woods.xlsx"), 503);
        File storedFile = filestore.retrieveFile(datasetWithHiddenFields);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithHiddenFields, tdarDataImportDatabase);
        converter.execute();
        DataTable dataTable = null;
        for (DataTable table : converter.getDataTables()) {
            if (table.getName().indexOf("englands_woods_catalog") > 0) {
                dataTable = table;
            }
        }
        assertNotNull("table created", dataTable);

        // confirm that all the columns in the new table are ints
        tdarDataImportDatabase.selectAllFromTableInImportOrder(dataTable,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        assertEquals(Types.VARCHAR, meta.getColumnType(1));
                        assertEquals(Types.BIGINT, meta.getColumnType(2));
                        assertEquals(Types.DOUBLE, meta.getColumnType(3));
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback
    public void testMalformedExcelDatasetFromScott() throws IOException {
        InformationResourceFileVersion datasetWithHiddenFields = makeFileVersion(new File(getTestFilePath(), 
                "PFRAA_fake_Ferengi_trading_post_data_for tDAR test.xls"), 509);
        File storedFile = filestore.retrieveFile(datasetWithHiddenFields);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithHiddenFields, tdarDataImportDatabase);
        try {
            converter.execute();
        } catch (TdarRecoverableRuntimeException ex) {
            logger.debug("caught exception", ex);
            assertTrue(ex.getMessage().contains("row #8 has more columns") || ex.getCause().getMessage().contains("row #8 has more columns"));
            assertTrue(ex.getMessage().contains("IFTI Context codes") || ex.getCause().getMessage().contains("IFTI Context codes"));
        }
        DataTable about = converter.getDataTableByName("e_509_about");
        assertEquals(2, about.getColumnNames().size());

        DataTable inventory = converter.getDataTableByName("e_509_pfraa_inveontory_data");
        assertEquals(35, inventory.getColumnNames().size());

        DataTable context = converter.getDataTableByName("e_509_ifti_context_codes");
        assertEquals(10, context.getColumnNames().size());

        DataTable data = converter.getDataTableByName("e_509_pfraa_ifti_data");
        assertEquals(17, data.getColumnNames().size());
    }

}
