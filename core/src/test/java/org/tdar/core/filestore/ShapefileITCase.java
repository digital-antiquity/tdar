/**
 * 
 */
package org.tdar.core.filestore;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.GisFileReaderTask;

/**
 * @author Adam Brin
 * 
 */
public class ShapefileITCase extends AbstractIntegrationTestCase {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    FileAnalyzer fileAnalyzer;

    @Test
    @Rollback
    public void testGeoTiffArc10WithWorldFile() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext();
        wc.setFilestore(TdarConfiguration.getInstance().getFilestore());
        wc.setInformationResourceId(123456789L);
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, "untitled.tif", new File(TestConstants.TEST_GEOTIFF), store);
        InformationResourceFileVersion supportingFile = generateAndStoreVersion(Geospatial.class, "untitled.tfw", new File(TestConstants.TEST_GEOTIFF_TFW),
                store);
        Workflow workflow = fileAnalyzer.getWorkflow(originalFile, supportingFile);
        wc.getOriginalFiles().add(originalFile);
        wc.getOriginalFiles().add(supportingFile);

        workflow.run(wc);
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testGeoTiffCombined() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11115L);
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, "untitled.tif", new File(TestConstants.TEST_GEOTIFF_COMBINED),
                store);
        wc.getOriginalFiles().add(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testKml() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11114L);
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, "doc.kml", new File(TestConstants.TEST_KML),
                store);
        wc.getOriginalFiles().add(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testPolyShapeWithData() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11113L);
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
    @Ignore("not implemented")
    public void testGeoTiffAUX() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11112L);
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
    public void testFAIMSKML() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11111L);
        String name = "Tracklog";
        String string = TestConstants.TEST_ROOT_DIR + TestConstants.TEST_GIS_DIR + "/kml/" + name;
        InformationResourceFileVersion originalFile = generateAndStoreVersion(Geospatial.class, name + ".kml", new File(string + ".kml"), store);
        // for (String ext : new String[] { ".tif.aux.xml" }) {
        // originalFile.getSupportingFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));
        // }
        wc.getOriginalFiles().add(originalFile);
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testExtendedDataKml() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11110L);
        String name = "extendedData";
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
