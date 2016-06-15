package org.tdar.db.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.MessageHelper;

public class CsvConverterITCase extends AbstractIntegrationTestCase {

    @Autowired
    public DataTableService dataTableService;

    @Override
    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    /* this test is based on data that does not exist */
    // @Test
    // @Rollback
    // public void testMappingIssueWithFloats() throws IOException {
    // Dataset jswVersion = setupAndLoadResource("../coding sheet/mapping_test_jsw/sha-ceramics-with-feature-dates.csv", Dataset.class);
    // CodingSheet part_ = setupAndLoadResource("../coding sheet/mapping_test_jsw/dai---part.csv", CodingSheet.class);
    // CodingSheet size_ = setupAndLoadResource("../coding sheet/mapping_test_jsw/dai---size.csv", CodingSheet.class);
    // CodingSheet tsg_ = setupAndLoadResource("../coding sheet/mapping_test_jsw/dai---tsg.csv", CodingSheet.class);
    // CodingSheet tt_ = setupAndLoadResource("../coding sheet/mapping_test_jsw/dai--tt.csv", CodingSheet.class);
    // DataTable table = jswVersion.getDataTables().iterator().next();
    // DataTableColumn vpart = new DataTableColumn();
    // vpart.setName("vpart");
    // vpart.setDefaultCodingSheet(part_);
    // vpart.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
    //
    // DataTableColumn size = new DataTableColumn();
    // size.setName("size");
    // size.setDefaultCodingSheet(size_);
    // size.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
    //
    // DataTableColumn tsg = new DataTableColumn();
    // tsg.setName("tsg");
    // tsg.setDefaultCodingSheet(tsg_);
    // tsg.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
    //
    // DataTableColumn tt = new DataTableColumn();
    // tt.setName("tt");
    // tt.setDefaultCodingSheet(tt_);
    // tt.setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
    //
    // mapColumnsToDataset(jswVersion, table, vpart, tsg, tt, size);
    // }

    @Test
    @Rollback(true)
    public void testCsvConverterMalformedFile()
            throws Exception {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(new File(getTestFilePath(), "malformed_csv_dataset.csv"), 505);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        try {
            converter.execute();
        } catch (TdarRecoverableRuntimeException e) {
            if (!e.getMessage().contains("has more columns") && !e.getCause().getMessage().contains("has more columns")) {
                throw e;
            }
        }
    }

    @Test
    @Rollback(true)
    public void testCsvConverterWordQuotedFile()
            throws Exception {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(new File(getTestFilePath(), "word_formed_csv_dataset.csv"), 504);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        converter.execute();

        List<String> findAllDistinctValues = dataTableService.findAllDistinctValues(converter.getDataTableByName("csv_504_word_formed_csv_dataset")
                .getColumnByName("siteno22"));
        assertEquals(1, findAllDistinctValues.size());
        assertEquals("1", findAllDistinctValues.get(0));
    }

    @Test
    @Rollback(true)
    public void testCsvWithTooManyColumns()
            throws Exception {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(new File(getTestFilePath(), "too_many_columns.tab"), 504);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        Exception ex = null;
        try {
            converter.execute();
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        assertEquals(MessageHelper.getMessage("postgresDatabase.datatable_to_long"), ex.getMessage());
    }

    @Test
    @Rollback(true)
    public void testCsvConverterWithMultipleTables()
            throws Exception {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(new File(getTestFilePath(), "Workbook1.csv"), 503);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        converter.execute();

        tdarDataImportDatabase.selectAllFromTableInImportOrder(converter.getDataTableByName("csv_503_workbook1"),
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        ResultSetMetaData meta = rs.getMetaData();
                        logger.info("testing types");
                        assertEquals(Types.VARCHAR, meta.getColumnType(1));
                        assertEquals(Types.TIMESTAMP, meta.getColumnType(2));
                        assertEquals(Types.BIGINT, meta.getColumnType(3));
                        assertEquals(Types.BIGINT, meta.getColumnType(4));
                        assertEquals(Types.DOUBLE, meta.getColumnType(5));
                        assertEquals(Types.VARCHAR, meta.getColumnType(6));
                        assertEquals(Types.VARCHAR, meta.getColumnType(7));

                        logger.info("testing column names");
                        assertEquals("column_1", meta.getColumnName(1));
                        assertEquals("column_2", meta.getColumnName(2));
                        assertEquals("column_3", meta.getColumnName(3));
                        assertEquals("column_4", meta.getColumnName(4));
                        assertEquals("column_5", meta.getColumnName(5));
                        assertEquals("column_6", meta.getColumnName(6));
                        assertEquals("col_blank", meta.getColumnName(7));
                        rs.next();

                        logger.info("testing values");
                        assertEquals("aaaa", rs.getString(1));
                        final Date date = rs.getDate(2);
                        // I know that getYear, getMonth and getDate are deprecated, but this just seemed the simplest.
                        assertTrue("Year should be 2003: " + date.getYear(), date.getYear() == (2003 - 1900));
                        assertTrue("Month should be 1: " + date.getMonth(), date.getMonth() == (1 - 1));
                        assertTrue("Day should be 2: " + date.getDate(), date.getDate() == 2);
                        assertEquals(0, rs.getLong(3));
                        assertTrue(rs.wasNull());
                        assertEquals(1234, rs.getLong(4));
                        assertTrue(1.1234 == rs.getDouble(5));
                        assertEquals("1234", rs.getString(6));
                        assertEquals(null, rs.getString(7));
                        assertTrue(rs.wasNull());
                        return null;
                    }
                }, false);
    }

}
