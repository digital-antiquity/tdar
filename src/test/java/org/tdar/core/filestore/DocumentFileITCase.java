/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.fileProcessing.MessageService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.workflows.PDFWorkflow;
import org.tdar.filestore.workflows.Workflow;

/**
 * @author Adam Brin
 * 
 */
public class DocumentFileITCase extends AbstractIntegrationTestCase {

    public static final Long INFORMATION_RESOURCE_ID = 12345l;
    public static final Long INFORMATION_RESOURCE_FILE_ID = 1234l;
    public static final Long INFORMATION_RESOURCE_FILE_VERSION_ID = 1112l;

    @Autowired
    private FileAnalyzer fileAnalyzer;

    @Autowired
    private MessageService messageService;

    @Test
    public void testBrokenImageStatus() throws Exception {
        String filename = "volume1-encrypted-test.pdf";
        File f = new File(TestConstants.TEST_DOCUMENT_DIR + "/sample_pdf_formats/", filename);
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);

        InformationResourceFileVersion originalVersion = generateAndStoreVersion(Document.class, filename, f, store);
        FileType fileType = fileAnalyzer.analyzeFile(originalVersion);
        assertEquals(FileType.DOCUMENT, fileType);
        Workflow workflow = fileAnalyzer.getWorkflow(originalVersion);
        assertEquals(PDFWorkflow.class, workflow.getClass());
        boolean result = messageService.sendFileProcessingRequest(originalVersion, workflow);
        InformationResourceFile informationResourceFile = originalVersion.getInformationResourceFile();
        informationResourceFile = genericService.find(InformationResourceFile.class, informationResourceFile.getId());
        assertFalse(result);
        assertEquals(FileStatus.PROCESSING_ERROR, informationResourceFile.getStatus());
    }
}
