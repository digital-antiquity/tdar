package org.tdar.db.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ShapefileReaderTask;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class ShapefileConverterITCase extends AbstractDataIntegrationTestCase {

    public String[] getDataImportDatabaseTables() {
        return new String[] {};
    };

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    @Test
    @Rollback(true)
    public void testSpatialDatabase() throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        String name = "Occ_3l";
        String string = TestConstants.TEST_SHAPEFILE_DIR + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".shp", new File(string + ".shp"), store);
        wc.getOriginalFiles().add(originalFile);
        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            wc.getOriginalFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));

        }
        DatasetConverter converter = DatasetConversionFactory.getConverter(originalFile, tdarDataImportDatabase);
        converter.execute();

        // wc.setOriginalFile(originalFile);
        // task.setWorkflowContext(wc);
        // task.run();
        //
        // DatasetConverter converter = convertDatabase("az-paleoindian-point-survey.mdb", 1129L);
        for (DataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

    }
}
