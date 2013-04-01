/**
 * 
 */
package org.tdar.core.filestore;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ShapefileReaderTask;

/**
 * @author Adam Brin
 * 
 */
public class ShapefileITCase extends AbstractIntegrationTestCase {

    @Autowired
    private FileAnalyzer fileAnalyzer;

    protected Logger logger = Logger.getLogger(getClass());

    @Test
    @Rollback
    public void testGeoTiffArc10() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, "untitled.tif", new File(TestConstants.TEST_GEOTIFF), store);
        InformationResourceFileVersion supportingFile = generateAndStoreVersion(Geospatial.class, "untitled.tfw", new File(TestConstants.TEST_GEOTIFF_TFW),
                store);
        wc.setOriginalFile(originalFile);
        wc.getSupportingFiles().add(supportingFile);
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testGeoTiffCombined() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, "untitled.tif", new File(TestConstants.TEST_GEOTIFF_COMBINED),
                store);
        wc.setOriginalFile(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testPolyShapeWithData() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        String name = "Occ_3l";
        String string = TestConstants.TEST_SHAPEFILE_DIR + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".shp", new File(string + ".shp"), store);
        for (String ext : new String[]{".dbf",".sbn",".sbx",".shp.xml",".shx",".xml"}) {
            originalFile.getSupportingFiles().add( generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));

        }
        wc.setOriginalFile(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

    
    
    
    @Test
    @Ignore
    @Rollback
    public void testGeoTiffAUX() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        String name = "Ruins of Tikal map-v11";
        String string = "c:/Users/abrin/Desktop/" + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".tif", new File(string + ".tif"), store);
        for (String ext : new String[]{".aux", ".tif.xml", ".tif.aux.xml"}) {
            wc.getSupportingFiles().add( generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));

        }
        wc.setOriginalFile(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

}
