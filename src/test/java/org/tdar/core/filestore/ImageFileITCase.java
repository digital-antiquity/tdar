/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.service.workflow.ImageWorkflow;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.Workflow;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;

/**
 * @author Adam Brin
 * 
 */
public class ImageFileITCase extends AbstractIntegrationTestCase {

    public static final Long INFORMATION_RESOURCE_ID = 12345l;
    public static final Long INFORMATION_RESOURCE_FILE_ID = 1234l;
    public static final Long INFORMATION_RESOURCE_FILE_VERSION_ID = 1112l;
    public static String baseIrPath = File.separator + "12" + File.separator + "34" + File.separator + "5" + File.separator + PairtreeFilestore.CONTAINER_NAME
            + File.separator;

    @Autowired
    private FileAnalyzer fileAnalyzer;

    @Autowired
    private MessageService messageService;

    protected Logger logger = Logger.getLogger(getClass());

    @Test
    public void testBrokenImageStatus() throws Exception {
        String filename = "grandcanyon_32_bit_color.tif";
        File f = new File(TestConstants.TEST_IMAGE_DIR + "/sample_image_formats/", filename);
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);

        InformationResourceFileVersion originalVersion = generateAndStoreVersion(SensoryData.class, filename, f, store);
        FileType fileType = fileAnalyzer.analyzeFile(originalVersion);
        assertEquals(FileType.IMAGE, fileType);
        Workflow workflow = fileAnalyzer.getWorkflow(originalVersion);
        assertEquals(ImageWorkflow.class, workflow.getClass());
        boolean result = messageService.sendFileProcessingRequest(originalVersion, workflow);
        InformationResourceFile informationResourceFile = originalVersion.getInformationResourceFile();
        informationResourceFile = genericService.find(InformationResourceFile.class, informationResourceFile.getId());
        assertFalse(result);
        assertEquals(FileStatus.PROCESSING_ERROR, informationResourceFile.getStatus());
    }
}
