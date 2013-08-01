package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.workflow.ActionMessageErrorListener;

public class ArchiveITCase extends AbstractIntegrationTestCase {

    public Archive generateArchiveFileAndUser(String archive) {
        Archive result = createAndSaveNewInformationResource(Archive.class, false);
        assertTrue(result.getResourceType() == ResourceType.ARCHIVE);
        File file = new File(TestConstants.TEST_ARCHIVE_DIR + archive);
        assertTrue("testing " + TestConstants.FAULTY_ARCHIVE + " doesn't exis?", file.exists());
        result = (Archive) addFileToResource(result, file); // now take the file through the work flow.
        return result;
    }

    @Test
    @Rollback(true)
    public void replicateFaultyArchiveIssue() throws Exception {
        InformationResource ir = generateArchiveFileAndUser(TestConstants.FAULTY_ARCHIVE);
        // Martin: in my scenario, the file results in a processing error.
        final Set<InformationResourceFile> irFiles = ir.getInformationResourceFiles();
        assertEquals(irFiles.size(), 1);

        InformationResourceFile irFile = irFiles.iterator().next();
        irFile = genericService.find(InformationResourceFile.class, irFile.getId());
        assertNotNull("IrFile is null", irFile);
        assertEquals(FileStatus.PROCESSING_WARNING, irFile.getStatus());
        assertEquals(irFile.getInformationResourceFileType(), FileType.FILE_ARCHIVE);
        

        genericService.saveOrUpdate(irFile);
        genericService.synchronize();
        
        // however, whatever caused the processing error is fixed
        File fileInStore = TdarConfiguration.getInstance().getFilestore().retrieveFile(irFile.getLatestUploadedVersion());
        File sourceFile = new File(TestConstants.TEST_ARCHIVE_DIR + TestConstants.GOOD_ARCHIVE);
        org.apache.commons.io.FileUtils.copyFile(sourceFile, fileInStore);

        // and the file is reprocessed 
        ActionMessageErrorListener listener = new ActionMessageErrorListener();
        informationResourceService.reprocessInformationResourceFiles(irFile, listener);
        
        // then in memory, the following is true:
        irFile = genericService.find(InformationResourceFile.class, irFile.getId());
        assertEquals(FileStatus.PROCESSED, irFile.getStatus());
        
        // however, in the database the file status has not been persisted...
        // I'm not sure how to demonstrate this in the test environment, where everything is rolled back
    }

    @Test
    @Rollback(true)
    public void testReprocessFaultyArchive() throws Exception {
        InformationResource ir = generateArchiveFileAndUser(TestConstants.FAULTY_ARCHIVE);
        final Set<InformationResourceFile> irFiles = ir.getInformationResourceFiles();
        assertEquals(irFiles.size(), 1);
        InformationResourceFile irFile = irFiles.iterator().next();
        irFile = genericService.find(InformationResourceFile.class, irFile.getId());
        assertNotNull("IrFile is null", irFile);
        assertEquals(FileStatus.PROCESSING_WARNING, irFile.getStatus());

        irFile.setStatus(FileStatus.DELETED);
        irFile.setErrorMessage("blah");
        genericService.saveOrUpdate(irFile);
        genericService.synchronize();
        ActionMessageErrorListener listener = new ActionMessageErrorListener();
        informationResourceService.reprocessInformationResourceFiles(irFile, listener);
        genericService.synchronize();

        irFile = genericService.find(InformationResourceFile.class, irFile.getId());
        assertNotNull("IrFile is null", irFile);
        assertEquals(FileStatus.PROCESSING_WARNING, irFile.getStatus());

    }

}
