package org.tdar.db.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.conversion.converters.ShapeFileDatabaseConverter;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ShapefileReaderTask;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

import static org.junit.Assert.*;

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
        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            originalFile.getSupportingFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));

        }
        DatasetConverter converter = DatasetConversionFactory.getConverter(originalFile, tdarDataImportDatabase);
        converter.execute();

//        wc.setOriginalFile(originalFile);
//        task.setWorkflowContext(wc);
//        task.run();
//
//        DatasetConverter converter = convertDatabase("az-paleoindian-point-survey.mdb", 1129L);
        for (DataTable table : converter.getDataTables()) {
            logger.info("{}", table);
        }

    }
}
