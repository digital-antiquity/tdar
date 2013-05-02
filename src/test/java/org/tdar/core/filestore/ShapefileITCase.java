/**
 * 
 */
package org.tdar.core.filestore;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.ShapefileReaderTask;

/**
 * @author Adam Brin
 * 
 */
public class ShapefileITCase extends AbstractIntegrationTestCase {

    protected Logger logger = Logger.getLogger(getClass());

    @Test
    @Rollback
    public void testGeoTiffArc10WithWorldFile() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, "untitled.tif", new File(TestConstants.TEST_GEOTIFF), store);
        InformationResourceFileVersion supportingFile = generateAndStoreVersion(Geospatial.class, "untitled.tfw", new File(TestConstants.TEST_GEOTIFF_TFW),
                store);
        wc.getOriginalFiles().add(originalFile);
        wc.getOriginalFiles().add(supportingFile);
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
        wc.getOriginalFiles().add(originalFile);
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
        wc.getOriginalFiles().add(originalFile);
        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            wc.getOriginalFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));

        }
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testGeoTiffAUX() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        String name = "CAmpusMap1950new";
        String string = TestConstants.TEST_GEOTIFF_DIR + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".tif", new File(string + ".tif"), store);
        wc.getOriginalFiles().add(originalFile);
        for (String ext : new String[] { ".tif.aux.xml" }) {
            wc.getOriginalFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));
        }
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testKML() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        ShapefileReaderTask task = new ShapefileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        String name = "doc";
        String string = TestConstants.TEST_ROOT_DIR + TestConstants.TEST_GIS_DIR + "/kml/" + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".kml", new File(string + ".kml"), store);
        // for (String ext : new String[] { ".tif.aux.xml" }) {
        // originalFile.getSupportingFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));
        // }
        wc.getOriginalFiles().add(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

}
