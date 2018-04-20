/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.WorkflowResult;
import org.tdar.core.service.workflow.workflows.ImageWorkflow;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.Filestore;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class ImageFileITCase extends AbstractIntegrationTestCase {

    @Autowired
    private FileAnalyzer fileAnalyzer;

    @Autowired
    private MessageService messageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.JAI_DISABLED })
    public void testMissingImageStatus() throws Exception {
        String filename = "grandcanyon_cmyk.jpg";
        InformationResourceFile informationResourceFile = testFileProcessing(filename, false);
        assertEquals(FileStatus.PROCESSING_WARNING, informationResourceFile.getStatus());
    }

    @Test
    @Rollback
    public void testMissingImageStatusWithJAI() throws Exception {
        String filename = "grandcanyon_cmyk.jpg";
        InformationResourceFile informationResourceFile = testFileProcessing(filename, true);
        assertEquals(FileStatus.PROCESSED, informationResourceFile.getStatus());
    }

    @Test
    @Rollback
    public void testGEoTIFF() throws Exception {
        InformationResourceFile informationResourceFile = testFileProcessing("PUEBLO_ALTO_50.tif", true);
        assertEquals(FileStatus.PROCESSED, informationResourceFile.getStatus());
    }

    @Test
    @Rollback
    public void testGPSImageStatus() throws Exception {
        String filename = "gps_photo.jpg";
        InformationResourceFile informationResourceFile = testFileProcessing(filename, true);
        assertEquals(FileStatus.PROCESSED, informationResourceFile.getStatus());
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.JAI_DISABLED })
    public void testImageFormatMissingStatus() throws Exception {
        String filename = "grandcanyon_cmyk.jpg";
        InformationResourceFile informationResourceFile = testFileProcessing(filename, false);
        assertEquals(FileStatus.PROCESSING_WARNING, informationResourceFile.getStatus());
    }

    @Test
    @Rollback
    public void testImageCorrupt() throws Exception {
        String filename = "grandcanyon_lzw_corrupt.tif";
        InformationResourceFile informationResourceFile = testFileProcessing(filename, false);
        assertEquals(FileStatus.PROCESSING_ERROR, informationResourceFile.getStatus());
    }

    // @Test
    // @Rollback
    // public void testImageLZW() throws Exception {
    // String filename = "grandcanyon_lzw.tif";
    // InformationResourceFile informationResourceFile = testFileProcessing(filename);
    // assertEquals(FileStatus.PROCESSING_ERROR, informationResourceFile.getStatus());
    // }

    private InformationResourceFile testFileProcessing(File f, boolean successful) throws InstantiationException, IllegalAccessException, IOException,
            Exception {
        Filestore store = TdarConfiguration.getInstance().getFilestore();

        SensoryData doc = generateAndStoreVersion(SensoryData.class, f.getName(), f, store);
        InformationResourceFileVersion originalVersion = doc.getLatestUploadedVersion();
        FileType fileType = fileAnalyzer.analyzeFile(originalVersion);
        assertEquals(FileType.IMAGE, fileType);
        Workflow workflow = fileAnalyzer.getWorkflow(originalVersion);
        assertEquals(ImageWorkflow.class, workflow.getClass());
        boolean result = messageService.sendFileProcessingRequest(workflow, originalVersion);
        FileProxy proxy = new FileProxy(f.getName(), f, VersionType.UPLOADED);
        proxy.setInformationResourceFileVersion(originalVersion);
        proxy.setInformationResourceFile(originalVersion.getInformationResourceFile());
        WorkflowResult workflowResult = new WorkflowResult(Arrays.asList(proxy));
        ErrorTransferObject errorsAndMessages = workflowResult.getActionErrorsAndMessages();
        logger.debug("ACTION ERRORS  : {}", errorsAndMessages.getActionErrors());
        logger.debug("ACTION Messages: {}", errorsAndMessages.getActionMessages());
        InformationResourceFile informationResourceFile = originalVersion.getInformationResourceFile();
        informationResourceFile = genericService.find(InformationResourceFile.class, informationResourceFile.getId());
        assertEquals(successful, result);
        return informationResourceFile;
    }

    private InformationResourceFile testFileProcessing(String filename, boolean successful) throws InstantiationException, IllegalAccessException, IOException,
            Exception {
        File f = TestConstants.getFile(TestConstants.TEST_IMAGE_DIR + "/sample_image_formats/", filename);
        return testFileProcessing(f, successful);
    }
}
