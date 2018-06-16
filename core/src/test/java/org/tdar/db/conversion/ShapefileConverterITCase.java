package org.tdar.db.conversion;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.datatable.TDataTable;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.VersionType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ConvertDatasetTask;
import org.tdar.utils.FileStoreFileUtils;

public class ShapefileConverterITCase extends AbstractIntegrationTestCase {

    public String[] getDataImportDatabaseTables() {
        return new String[] {};
    };

    protected PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    @Test
    @Rollback(true)
    public void testSpatialDatabase() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ConvertDatasetTask task = new ConvertDatasetTask();
        WorkflowContext wc = new WorkflowContext();
        wc.setFilestore(TdarConfiguration.getInstance().getFilestore());
        wc.setInformationResourceId(12345L);
        wc.setInformationResourceId(111L);
        wc.setHasDimensions(true);;
        wc.setTargetDatabase(tdarDataImportDatabase);
        String name = "Occ_3l";
        String string = TestConstants.TEST_SHAPEFILE_DIR + name;
        Geospatial doc = generateAndStoreVersion(Geospatial.class, name + ".shp", TestConstants.getFile(string + ".shp"), store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalFile));

        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            Geospatial doc2 = generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store);
            wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(doc2.getLatestUploadedVersion()));

        }

        task.setWorkflowContext(wc);
        task.run();
//        Dataset dataset = (Dataset) wc.getTransientResource();
        FileStoreFile geoJson = null;
        for (FileStoreFile vers : wc.getVersions()) {
            if (vers.getVersionType() == VersionType.GEOJSON) {
                geoJson = vers;
            }
        }
        assertTrue(geoJson != null);
        // wc.setOriginalFile(originalFile);
        // task.setWorkflowContext(wc);
        // task.run();
        //
        // DatasetConverter converter = convertDatabase("az-paleoindian-point-survey.mdb", 1129L);
        for (TDataTable table : wc.getDataTables()) {
            logger.info("{}", table);
        }

    }
}
