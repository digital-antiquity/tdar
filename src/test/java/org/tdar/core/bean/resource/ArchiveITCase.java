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
import org.tdar.core.service.workflow.ActionMessageErrorListener;
import org.tdar.core.service.workflow.ActionMessageErrorSupport;

public class ArchiveITCase extends AbstractIntegrationTestCase {

    public Archive generateArchiveFileAndUser() {
        Archive result = createAndSaveNewInformationResource(Archive.class, false);
        assertTrue(result.getResourceType() == ResourceType.ARCHIVE);
        File file = new File(TestConstants.TEST_ARCHIVE_DIR + TestConstants.FAULTY_ARCHIVE);
        assertTrue("testing " + TestConstants.FAULTY_ARCHIVE + " exists", file.exists());
        result = (Archive) addFileToResource(result, file); // now take the file through the work flow.
        return result;
    }

    @Test
    @Rollback(true)
    public void testReprocessFaultyArchive() {
        InformationResource ir = generateArchiveFileAndUser();
        final Set<InformationResourceFile> irFiles = ir.getInformationResourceFiles();
        assertEquals(irFiles.size(), 1);

        InformationResourceFile irFile = irFiles.iterator().next();
        irFile = genericService.find(InformationResourceFile.class, irFile.getId());
        assertNotNull("IrFile is null", irFile);
        assertEquals(FileStatus.PROCESSING_WARNING, irFile.getStatus());
        assertEquals(1, irFile.getLatestVersion().intValue());
        assertEquals(3, irFile.getLatestVersions().size());
        InformationResourceFileVersion irFileVersion = irFile.getLatestVersions().iterator().next();
        assertNotNull("IrFileVersion is null", irFileVersion);
        assertEquals(1, irFile.getLatestVersion().intValue());

        assertEquals(irFile.getInformationResourceFileType(), FileType.FILE_ARCHIVE);
        List<Long> irfvids = new ArrayList<>();
        Map<VersionType, InformationResourceFileVersion> map = new HashMap<>();
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            map.put(irfv.getFileVersionType(), irfv);
            irfvids.add(irfv.getId());
        }
        assertTrue(map.containsKey(VersionType.METADATA));
        assertTrue(map.containsKey(VersionType.UPLOADED));
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));

        assertEquals(map.get(VersionType.UPLOADED).getFilename(), TestConstants.FAULTY_ARCHIVE);

        genericService.synchronize();
        ActionMessageErrorListener listener = new ActionMessageErrorListener();
        informationResourceService.reprocessInformationResourceFiles(irFiles,listener);
        
        map = new HashMap<>();
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            logger.debug("version: {}", irfv);
            map.put(irfv.getFileVersionType(), irfv);
            if (irfv.isArchival() || irfv.isUploaded()) {
                assertTrue(irfvids.contains(irfv.getId()));
            } else {
                assertFalse(irfvids.contains(irfv.getId()));
            }
        }
        assertTrue(map.containsKey(VersionType.METADATA));
        assertTrue(map.containsKey(VersionType.UPLOADED));
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));

        assertEquals(map.get(VersionType.UPLOADED).getFilename(), TestConstants.FAULTY_ARCHIVE);
    }

}
