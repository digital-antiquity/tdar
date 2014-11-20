package org.tdar.db.conversion;

import java.io.File;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ConvertDatasetTask;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class ShapefileConverterITCase extends AbstractDataIntegrationTestCase {

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
        wc.setResourceType(ResourceType.GEOSPATIAL);
        wc.setTargetDatabase(tdarDataImportDatabase);
        String name = "Occ_3l";
        String string = TestConstants.TEST_SHAPEFILE_DIR + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".shp", new File(string + ".shp"), store);
        wc.getOriginalFiles().add(originalFile);

        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            wc.getOriginalFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));

        }

        task.setWorkflowContext(wc);
        task.run();
        Dataset dataset = (Dataset) wc.getTransientResource();
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
