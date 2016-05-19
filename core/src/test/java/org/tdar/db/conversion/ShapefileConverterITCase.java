package org.tdar.db.conversion;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ConvertDatasetTask;

public class ShapefileConverterITCase extends AbstractIntegrationTestCase {

    public String[] getDataImportDatabaseTables() {
        return new String[] {};
    };

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    @Override
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
        wc.setResourceType(ResourceType.GEOSPATIAL);
        wc.setTargetDatabase(tdarDataImportDatabase);
        String name = "Occ_3l";
        String string = TestConstants.TEST_SHAPEFILE_DIR + name;
        Geospatial doc = generateAndStoreVersion(Geospatial.class, name + ".shp", new File(string + ".shp"), store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();
        wc.getOriginalFiles().add(originalFile);

        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            Geospatial doc2 = generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store);
            wc.getOriginalFiles().add(doc2.getLatestUploadedVersion());

        }

        task.setWorkflowContext(wc);
        task.run();
        Dataset dataset = (Dataset) wc.getTransientResource();
        InformationResourceFileVersion geoJson = null;
        for (InformationResourceFileVersion vers : wc.getVersions()) {
            if (vers.getFileVersionType() == VersionType.GEOJSON) {
                geoJson = vers;
            }
        }
        assertTrue(geoJson != null);
        // wc.setOriginalFile(originalFile);
        // task.setWorkflowContext(wc);
        // task.run();
        //
        // DatasetConverter converter = convertDatabase("az-paleoindian-point-survey.mdb", 1129L);
        for (DataTable table : dataset.getDataTables()) {
            logger.info("{}", table);
        }

    }
}
