/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.workflow.MessageService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FileType;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.workflows.FileArchiveWorkflow;
import org.tdar.workflows.Workflow;

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
        assertEquals(ResourceType.DOCUMENT, fileAnalyzer.suggestTypeForFileName("doc", ResourceType.DOCUMENT));
        assertEquals(ResourceType.SENSORY_DATA, fileAnalyzer.suggestTypeForFileName("gif", ResourceType.SENSORY_DATA, ResourceType.IMAGE));
        assertEquals(ResourceType.IMAGE, fileAnalyzer.suggestTypeForFileName("gif", ResourceType.IMAGE, ResourceType.SENSORY_DATA));
        assertNull(fileAnalyzer.suggestTypeForFileName("xls", ResourceType.ONTOLOGY));
        assertEquals(ResourceType.CODING_SHEET, fileAnalyzer.suggestTypeForFileName("xls", ResourceType.ONTOLOGY, ResourceType.CODING_SHEET));
        assertFalse(fileAnalyzer.getExtensionsForType(ResourceType.ARCHIVE).contains("xml"));
        assertEquals(ResourceType.AUDIO, fileAnalyzer.suggestTypeForFileName("aiff", ResourceType.values()));
    }

    @Test
    @Rollback
    public void testFileAnalyzer() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        testArchiveFormat(store, "ark_hm_headpot_scans.tar");
        testArchiveFormat(store, "ark_hm_headpot_scans.zip");
        testArchiveFormat(store, "ark_hm_headpot_scans.tgz");
        testArchiveFormat(store, "ark_hm_headpot_scans.tar.bz2");
    }

    public void testArchiveFormat(PairtreeFilestore store, String filename) throws InstantiationException, IllegalAccessException, IOException, Exception {
        File f = TestConstants.getFile(TestConstants.TEST_SENSORY_DIR, filename);
        SensoryData doc = generateAndStoreVersion(SensoryData.class, filename, f, store);
        InformationResourceFileVersion originalVersion = doc.getLatestUploadedVersion();

        FileType fileType = fileAnalyzer.analyzeFile(originalVersion);
        assertEquals(FileType.FILE_ARCHIVE, fileType);
        Workflow workflow = fileAnalyzer.getWorkflow(ResourceType.SENSORY_DATA, originalVersion);
        assertEquals(FileArchiveWorkflow.class, workflow.getClass());
        messageService.sendFileProcessingRequest(workflow, originalVersion);
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
