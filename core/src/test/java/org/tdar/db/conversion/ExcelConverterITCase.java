package org.tdar.db.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ExcelConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
public class ExcelConverterITCase extends AbstractIntegrationTestCase {

    protected PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDARDATA_SMALL_BATCH })
    public void testBatchImportRows() throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(new File(getTestFilePath(), "batch-import.xlsx"), 101);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        converter.execute();
        Set<DataTable> dataTables = converter.getDataTables();
        assertEquals(1, dataTables.size());
        DataTable table = dataTables.iterator().next();
        // List<DataTableColumn> dataTableColumns = table.getDataTableColumns();

        Integer total = tdarDataImportDatabase.selectAllFromTable(table, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int total = 0;
                while (rs.next()) {
                    String str = rs.getString(2);
                    logger.trace(str);
                    total += Integer.parseInt(rs.getString(2));
                }
                return total;
            }
        }, true);
        assertEquals(946, total.intValue());
    }

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
            logger.trace("{} : {}", col.getSequenceNumber(), col);
            assertEquals(Integer.valueOf(i), col.getSequenceNumber());
            i++;
        }
    }

    @Test
    @Rollback
    public void testPoiInfiniteLoop() throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(new File(getTestFilePath(), "infinite-loop.xlsx"), 519);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        converter.execute();
    }

    @Test
    @Rollback
    public void testStdeva() throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(new File(getTestFilePath(), "stdev.xlsx"), 520);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        String msg = null;
        try {
            converter.execute();
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(
                "you are using an excel function \"STDEVA\" that cannot be read at sheet \"Sheet1\" at cell: \"A7\", please replace it with the \"value\" of the calculation",
                msg);
    }

    @Test
    @Rollback
    public void testBlankExceedingRowsAndExtraColumnAtEnd() throws IOException {
        importSpreadsheetAndConfirmExceptionIsThrown(new File(getTestFilePath(), "Pundo_degenerate.xls"),
                "Appendix 8 (2) - row #49 has more columns (6) than this sheet has column names (5)");
    }

    private void importSpreadsheetAndConfirmExceptionIsThrown(File spreadsheet, String expectedErrorMessage) throws IOException {
        InformationResourceFileVersion weirdColumnsDataset = makeFileVersion(spreadsheet, 529);
        ExcelConverter converter = new ExcelConverter(tdarDataImportDatabase, weirdColumnsDataset);
        try {
            converter.execute();
            Assert.fail("Should never get to this point in the code.");
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
        assertNotNull(dataTable.getColumnByName("col_period"));
        assertNotNull(dataTable.getColumnByDisplayName("Stratigraphy"));
        assertNotNull(dataTable.getColumnByDisplayName("Sites::N Coordinates"));
        assertNotNull(dataTable.getColumnByDisplayName("Sites::E Coordinates"));
        assertNotNull(dataTable.getColumnByDisplayName("Layers::Calibrated dates"));
        assertNotNull(dataTable.getColumnByDisplayName("Dates::Uncalibrated date"));
        assertNotNull(dataTable.getColumnByDisplayName("Dates::Margin of error"));
        assertNotNull(dataTable.getColumnByDisplayName("Thesis?"));
        assertNotNull(dataTable.getColumnByDisplayName("Dates::Cal 95% Min"));

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
        assertNotNull(dataTable.getColumnByName("col_period"));
        assertNotNull(dataTable.getColumnByName("sumofno"));
        assertNotNull(dataTable.getColumnByName("c1_00"));
        assertNotNull(dataTable.getColumnByName("abc"));
    }

    @Test
    @Rollback
    public void testConverterAllStrings() throws Exception {
        InformationResourceFileVersion datasetTextOnly = makeFileVersion(new File(getTestFilePath(), "dataset_all_text.xls"), 500);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetTextOnly);
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
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetWithInts);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithInts, tdarDataImportDatabase);

        converter.execute();
        DataTable table = converter.getDataTables().iterator().next();
        assertEquals("table created", -1, table.getName().indexOf("sheet1"));
        assertTrue("table created", table.getName().indexOf("dataset_with_ints") > 0);

        // confirm that all the columns in the new table are ints
        tdarDataImportDatabase.selectAllFromTableInImportOrder(table,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        assertEquals(Types.VARCHAR, meta.getColumnType(1));
                        assertEquals(Types.BIGINT, meta.getColumnType(2));
                        assertEquals(Types.BIGINT, meta.getColumnType(3));
                        assertEquals(Types.VARCHAR, meta.getColumnType(4));
                        assertEquals(Types.BIGINT, meta.getColumnType(5));
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback
    public void testConverterWithDates() throws Exception {
        InformationResourceFileVersion datasetWithDates = makeFileVersion(new File(getTestFilePath(), "dataset_with_dates.xls"), 592);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetWithDates);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithDates, tdarDataImportDatabase);

        converter.execute();
        DataTable table = converter.getDataTables().iterator().next();
        assertEquals("table created", -1, table.getName().indexOf("sheet1"));
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
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        assertEquals("Year should be 2003: " + year, 2003, year);
                        assertEquals("Month should be 1: " + month, 1, month);
                        assertEquals("Day should be 1: " + day, 1, day);
                        return null;
                    }
                }, false);
    }

    @Test
    @Rollback
    public void testConverterWithFloats() throws Exception {
        InformationResourceFileVersion datasetWithFloats = makeFileVersion(new File(getTestFilePath(), "dataset_with_floats.xls"), 502);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetWithFloats);
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
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetWithHiddenFields);
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
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetWithHiddenFields);
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
        assertEquals(6, context.getColumnNames().size());

        DataTable data = converter.getDataTableByName("e_509_pfraa_ifti_data");
        assertEquals(17, data.getColumnNames().size());
    }

    @Test
    @Rollback
    public void testMalformedFloatParse() throws IOException {
        InformationResourceFileVersion datasetWithHiddenFields = makeFileVersion(new File(getTestFilePath(),
                "test_malformed_parse_float.xlsx"), 511);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, datasetWithHiddenFields);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(datasetWithHiddenFields, tdarDataImportDatabase);
        boolean exception = false;
        try {
            converter.execute();
        } catch (TdarRecoverableRuntimeException ex) {
            logger.debug("caught exception", ex);
            exception = true;
        }
        assertFalse(exception);
    }

}
