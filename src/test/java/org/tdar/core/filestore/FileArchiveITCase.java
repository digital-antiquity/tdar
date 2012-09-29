/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.service.fileProcessing.MessageService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PairtreeFilestore;
import org.tdar.filestore.workflows.FileArchiveWorkflow;
import org.tdar.filestore.workflows.Workflow;

/**
 * @author Adam Brin
 * 
 */
public class FileArchiveITCase extends AbstractIntegrationTestCase {

    public static final Long INFORMATION_RESOURCE_ID = 12345l;
    public static final Long INFORMATION_RESOURCE_FILE_ID = 1234l;
    public static final Long INFORMATION_RESOURCE_FILE_VERSION_ID = 1112l;
    private static final Integer VERSION = 1;
    public static String baseIrPath = File.separator + "12" + File.separator + "34" + File.separator + "5" + File.separator + PairtreeFilestore.CONTAINER_NAME
            + File.separator;

    @Autowired
    private FileAnalyzer fileAnalyzer;

    @Autowired
    private MessageService messageService;

    protected Logger logger = Logger.getLogger(getClass());

    @Test
    public void testAnalyzerSuggestions() {
        assertEquals(ResourceType.DOCUMENT, fileAnalyzer.suggestTypeForFileExtension("doc", ResourceType.DOCUMENT));
        assertEquals(ResourceType.SENSORY_DATA, fileAnalyzer.suggestTypeForFileExtension("gif", ResourceType.SENSORY_DATA, ResourceType.IMAGE));
        assertEquals(ResourceType.IMAGE, fileAnalyzer.suggestTypeForFileExtension("gif", ResourceType.IMAGE, ResourceType.SENSORY_DATA));
        assertNull(fileAnalyzer.suggestTypeForFileExtension("xls", ResourceType.ONTOLOGY));
        assertEquals(ResourceType.CODING_SHEET, fileAnalyzer.suggestTypeForFileExtension("xls", ResourceType.ONTOLOGY, ResourceType.CODING_SHEET));
    }

    @Test
    @Rollback
    public void testFileAnalyzer() throws Exception {
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        testArchiveFormat(store, "ark_hm_headpot_scans.tar");
        testArchiveFormat(store, "ark_hm_headpot_scans.zip");
        testArchiveFormat(store, "ark_hm_headpot_scans.tgz");
    }

    public void testArchiveFormat(PairtreeFilestore store, String filename) throws InstantiationException, IllegalAccessException, IOException, Exception {
        File f = new File(TestConstants.TEST_SENSORY_DIR, filename);
        InformationResourceFileVersion originalVersion = generateVersion(SensoryData.class, filename);
        store.store(f, originalVersion);
        FileType fileType = fileAnalyzer.analyzeFile(originalVersion);
        assertEquals(FileType.FILE_ARCHIVE, fileType);
        Workflow workflow = fileAnalyzer.getWorkflow(originalVersion);
        assertEquals(FileArchiveWorkflow.class, workflow.getClass());
        messageService.sendFileProcessingRequest(originalVersion, workflow);
        InformationResourceFile informationResourceFile = originalVersion.getInformationResourceFile();
        informationResourceFile = genericService.find(InformationResourceFile.class, informationResourceFile.getId());

        for (InformationResourceFileVersion version : informationResourceFile.getLatestVersions()) {
            logger.info(version);
            if (!version.isUploaded()) {
                String contents = FileUtils.readFileToString(version.getFile());
                assertTrue(contents.contains("Ark_HM_Headpot_01.txt"));
                assertTrue(contents.contains("Ark_HM_Headpot_mtrx_01.txt"));
            }
        }

        // FIXME: confirm that there is a resulting file, and that the file has the right contents
        // confirm x number of versions, confirm types
        // confirm contents
    }

    private <R extends InformationResource> InformationResourceFileVersion generateVersion(Class<R> type, String name) throws InstantiationException,
            IllegalAccessException {
        InformationResource ir = createAndSaveNewInformationResource(type, false);
        InformationResourceFile irFile = new InformationResourceFile();
        irFile.setId(INFORMATION_RESOURCE_FILE_ID);
        irFile.setInformationResource(ir);
        irFile.setLatestVersion(VERSION);
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setVersion(VERSION);
        version.setId(INFORMATION_RESOURCE_FILE_VERSION_ID);
        version.setFilename(name);
        version.setExtension(FilenameUtils.getExtension(name));
        version.setInformationResourceFile(irFile);
        version.setDateCreated(new Date());
        version.setFileVersionType(VersionType.UPLOADED);
        irFile.getInformationResourceFileVersions().add(version);
        genericService.save(irFile);
        genericService.save(version);
        return version;
    }

}
