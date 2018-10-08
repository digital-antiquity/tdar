/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.core.service.workflow.workflows.FileArchiveWorkflow;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.PairtreeFilestore;

/**
 * @author Adam Brin
 * 
 */
public class FileArchiveITCase extends AbstractIntegrationTestCase {

    @Autowired
    private FileAnalyzer fileAnalyzer;

    @Autowired
    private MessageService messageService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testAnalyzerSuggestions() {
        assertEquals(ResourceType.DOCUMENT, fileAnalyzer.suggestTypeForFileExtension("doc", ResourceType.DOCUMENT));
        assertEquals(ResourceType.SENSORY_DATA, fileAnalyzer.suggestTypeForFileExtension("gif", ResourceType.SENSORY_DATA, ResourceType.IMAGE));
        assertEquals(ResourceType.IMAGE, fileAnalyzer.suggestTypeForFileExtension("gif", ResourceType.IMAGE, ResourceType.SENSORY_DATA));
        assertNull(fileAnalyzer.suggestTypeForFileExtension("xls", ResourceType.ONTOLOGY));
        assertEquals(ResourceType.CODING_SHEET, fileAnalyzer.suggestTypeForFileExtension("xls", ResourceType.ONTOLOGY, ResourceType.CODING_SHEET));
        assertFalse(fileAnalyzer.getExtensionsForType(ResourceType.ARCHIVE).contains("xml"));
        assertEquals(ResourceType.AUDIO, fileAnalyzer.suggestTypeForFileExtension("aiff", ResourceType.values()));
    }

    @Test
    @Rollback
    public void testFileAnalyzer() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        testArchiveFormat(store, "ark_hm_headpot_scans.tar",true);
        testArchiveFormat(store, "ark_hm_headpot_scans.zip",true);
        testArchiveFormat(store, "ark_hm_headpot_scans.tgz",true);
        testArchiveFormat(store, "ark_hm_headpot_scans.tar.bz2",true);
        testArchiveFormat(store, "ark_hm_headpot_scans-invalid.zip",false);
    }

    public void testArchiveFormat(PairtreeFilestore store, String filename, boolean expectOk) throws InstantiationException, IllegalAccessException, IOException, Exception {
        File f = TestConstants.getFile(TestConstants.TEST_SENSORY_DIR, filename);
        SensoryData doc = generateAndStoreVersion(SensoryData.class, filename, f, store);
        InformationResourceFileVersion originalVersion = doc.getLatestUploadedVersion();

        FileType fileType = fileAnalyzer.analyzeFile(originalVersion);
        assertEquals(FileType.FILE_ARCHIVE, fileType);
        Workflow workflow = fileAnalyzer.getWorkflow(originalVersion);
        assertEquals(FileArchiveWorkflow.class, workflow.getClass());
        boolean error  = false;
        try {
        error = messageService.sendFileProcessingRequest(workflow, originalVersion);
        } catch (Throwable t) {
            if (expectOk == true) {
                fail("got unexpected exception");
            }
        }
        
        if (expectOk == false && StringUtils.isNotBlank(originalVersion.getInformationResourceFile().getWorkflowContext().getExceptionAsString())) {
            logger.error("saw error and expected it");
            return;
        }
        
        
        InformationResourceFile informationResourceFile = originalVersion.getInformationResourceFile();
        informationResourceFile = genericService.find(InformationResourceFile.class, informationResourceFile.getId());

        boolean seen = false;
        for (InformationResourceFileVersion version : informationResourceFile.getLatestVersions()) {
            logger.info("{}", version);

            if (version.isTranslated()) {
                String contents = FileUtils
                        .readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
                assertTrue(contents.contains("Ark_HM_Headpot_01.txt"));
                assertTrue(contents.contains("Ark_HM_Headpot_mtrx_01.txt"));
                seen = true;
            }
        }

        assertTrue("Should have gotten through some translated files", seen);
        // FIXME: confirm that there is a resulting file, and that the file has the right contents
        // confirm x number of versions, confirm types
        // confirm contents
    }

}
