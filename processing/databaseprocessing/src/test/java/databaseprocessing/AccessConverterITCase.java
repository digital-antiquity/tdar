package databaseprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractSimpleIntegrationTest;
import org.tdar.TestConstants;
import org.tdar.db.conversion.converters.AccessDatabaseConverter;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.datatable.DataTableUtils;
import org.tdar.db.datatable.ImportTable;
import org.tdar.db.datatable.TDataTable;
import org.tdar.db.datatable.TDataTableRelationship;
import org.tdar.db.postgres.PostgresImportDatabase;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FilestoreObjectType;

@Ignore
public class AccessConverterITCase extends AbstractSimpleIntegrationTest {


    @Test
    @Rollback(true)
    public void testDatabase() throws FileNotFoundException, IOException {
        DatasetConverter converter = convertDatabase(TestConstants.getFile( TestConstants.TEST_DATA_INTEGRATION_DIR,  "rpms_corrected.mdb"), 1224L);
        for (TDataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

    }

    protected PostgresImportDatabase tdarDataImportDatabase = new PostgresImportDatabase();

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    @Test
    @Rollback(true)
    public void testSpatialDatabase() throws FileNotFoundException, IOException {
        DatasetConverter converter = convertDatabase(TestConstants.getFile( TestConstants.TEST_DATA_INTEGRATION_DIR, "az-paleoindian-point-survey.mdb"), 1129L);
        for (TDataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

    }

    // only necessary when database hasn't been wiped out since last integration test
    @Test
    @Rollback(true)
    public void testConvertTableToCodingSheet() throws Exception {
        DatasetConverter converter = setupSpitalfieldAccessDatabase();

        ImportTable dataTable = converter.getDataTableByOriginalName("spital_abone_database_mdb_basic_int");
        assertNotNull(dataTable);

        logger.info("{}", converter.getRelationships());
        List<TDataTableRelationship> listRelationshipsForColumns = DataTableUtils.listRelationshipsForColumns(dataTable.getColumnByName("basic_int"), converter.getRelationships());
        assertEquals(1, listRelationshipsForColumns.size());
        // assertEquals("d_503_spital_abone_database_mdb_basic_int", listRelationshipsForColumns.get(0).getLocalTable().getName());
        // assertEquals("d_503_spital_abone_database_mdb_context_data", listRelationshipsForColumns.get(0).getForeignTable().getName());
        assertEquals("basic_int", listRelationshipsForColumns.get(0).getColumnRelationships().iterator().next().getLocalColumn().getName());
        assertEquals("basic_int", listRelationshipsForColumns.get(0).getColumnRelationships().iterator().next().getForeignColumn().getName());
    }
    

    public DatasetConverter convertDatabase(File file, Long irFileId) throws IOException, FileNotFoundException {
        FileStoreFile accessDatasetFileVersion = makeFileStoreFile(file, irFileId);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = new AccessDatabaseConverter();
        converter.setTargetDatabase(tdarDataImportDatabase);
        converter.setInformationResourceFileVersion( accessDatasetFileVersion);
        converter.execute();
        setDataImportTables((String[]) ArrayUtils.addAll(getDataImportTables(), converter.getTableNames().toArray(new String[0])));
        return converter;
    }
    

    public DatasetConverter setupSpitalfieldAccessDatabase() throws IOException {
        DatasetConverter converter = convertDatabase(TestConstants.getFile( TestConstants.TEST_DATA_INTEGRATION_DIR,  SPITAL_DB_NAME), 1001L);
        return converter;
    }
    
    String[] dataImportTables = new String[0];

    public String[] getDataImportTables() {
        return dataImportTables;
    }

    public void setDataImportTables(String[] dataImportTables) {
        this.dataImportTables = dataImportTables;
    }

    @Before
    public void dropDataImportDatabaseTables() throws Exception {
        for (String table : getDataImportTables()) {
            try {
                tdarDataImportDatabase.dropTable(table);
            } catch (Exception ignored) {
            }
        }

    }

}
