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
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.fileprocessing.tasks.GisFileReaderTask;
import org.tdar.fileprocessing.workflows.Workflow;
import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.utils.FileStoreFileUtils;

/**
 * @author Adam Brin
 * 
 */
public class ShapefileITCase extends AbstractIntegrationTestCase {

    @SuppressWarnings("unused")
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
//        wc.setDataTableSupported(true);
        wc.setHasDimensions(true);
        
        Geospatial doc = generateAndStoreVersion(Geospatial.class, "untitled.tif", TestConstants.getFile(TestConstants.TEST_GEOTIFF), store);
        Geospatial doc2 = generateAndStoreVersion(Geospatial.class, "untitled.tfw", TestConstants.getFile(TestConstants.TEST_GEOTIFF_TFW),
                store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();
        InformationResourceFileVersion supportingFile = doc2.getLatestUploadedVersion();

        Workflow workflow = fileAnalyzer.getWorkflow(ResourceType.GEOSPATIAL, originalFile, supportingFile);
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalFile));
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(supportingFile));

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
        Geospatial doc = generateAndStoreVersion(Geospatial.class, "untitled.tif", TestConstants.getFile(TestConstants.TEST_GEOTIFF_COMBINED),
                store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();
        
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalFile));
        task.setWorkflowContext(wc);
        task.run();
    }

    @Test
    @Rollback
    public void testKml() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        GisFileReaderTask task = new GisFileReaderTask();
        WorkflowContext wc = new WorkflowContext(store, 11114L);
        Geospatial doc = generateAndStoreVersion(Geospatial.class, "doc.kml", TestConstants.getFile(TestConstants.TEST_KML),
                store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();

        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalFile));
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
        Geospatial doc = generateAndStoreVersion(Geospatial.class, name + ".shp", new File(string + ".shp"), store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalFile));
        for (String ext : new String[] { ".dbf", ".sbn", ".sbx", ".shp.xml", ".shx", ".xml" }) {
            Geospatial doc2 = generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store);
            InformationResourceFileVersion originalVersion = doc2.getLatestUploadedVersion();
            wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalVersion));

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
        Geospatial doc = generateAndStoreVersion(Geospatial.class, name + ".tif", new File(string + ".tif"), store);
        InformationResourceFileVersion originalFile = doc.getLatestUploadedVersion();
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalFile));
        for (String ext : new String[] { ".tif.aux.xml" }) {
            Geospatial doc2 = generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store);
            InformationResourceFileVersion originalVersion = doc2.getLatestUploadedVersion();
            wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalVersion));
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
        Geospatial doc = generateAndStoreVersion(Geospatial.class, name + ".kml", new File(string + ".kml"), store);
        InformationResourceFileVersion originalVersion = doc.getLatestUploadedVersion();
        // for (String ext : new String[] { ".tif.aux.xml" }) {
        // originalFile.getSupportingFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));
        // }
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalVersion));
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
        Geospatial doc = generateAndStoreVersion(Geospatial.class, name + ".kml", new File(string + ".kml"), store);
        InformationResourceFileVersion originalVersion = doc.getLatestUploadedVersion();

        // for (String ext : new String[] { ".tif.aux.xml" }) {
        // originalFile.getSupportingFiles().add(generateAndStoreVersion(Geospatial.class, name + ext, new File(string + ext), store));
        // }
        wc.getOriginalFiles().add(FileStoreFileUtils.copyVersionToFilestoreFile(originalVersion));
        task.setWorkflowContext(wc);
        task.run();
    }

}
