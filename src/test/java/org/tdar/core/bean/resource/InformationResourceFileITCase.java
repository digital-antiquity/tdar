package org.tdar.core.bean.resource;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.service.InformationResourceFileService;
import org.tdar.core.service.InformationResourceService;
import org.tdar.core.service.ResourceService;

import static org.junit.Assert.*;

public class InformationResourceFileITCase extends AbstractIntegrationTestCase {

    @Autowired
    InformationResourceFileService informationResourceFileService;

    @Autowired
    InformationResourceService informationResourceService;

    @Autowired
    ResourceService resourceService;

    @Test
    public void testXMLSave() {
        for (Document resource : resourceService.findAll(Document.class)) {
            logger.info(resource.getId() + " -- saving");
            resourceService.saveRecordToFilestore(resource);
        }
    }

    @Test
    @Rollback
    public void findByFilename() throws InstantiationException, IllegalAccessException {
        InformationResource ir = generateInformationResourceWithFile();
        InformationResourceFile foundFile = informationResourceService.findFileByFilename(ir, TestConstants.TEST_DOCUMENT_NAME);
        assertNotNull(foundFile);
        boolean found = false;
        for (InformationResourceFile file : ir.getInformationResourceFiles()) {
            if (file.equals(foundFile))
                found = true;
        }
        assertTrue(found);

    }

    @Test
    @Rollback(true)
    public void testCreateInformationResourceFile() throws InstantiationException, IllegalAccessException {
        InformationResource ir = generateInformationResourceWithFile();

        assertEquals(ir.getInformationResourceFiles().size(), 1);
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        assertNotNull("IrFile is null", irFile);
        assertEquals(1, irFile.getLatestVersion().intValue());
        assertEquals(irFile.getLatestVersions().size(), 5);
        InformationResourceFileVersion irFileVersion = irFile.getLatestVersions().iterator().next();
        assertNotNull("IrFileVersion is null", irFileVersion);
        assertEquals(1, irFile.getLatestVersion().intValue());

        assertEquals(irFile.getInformationResourceFileType(), FileType.DOCUMENT);

        Map<VersionType, InformationResourceFileVersion> map = new HashMap<InformationResourceFileVersion.VersionType, InformationResourceFileVersion>();
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            map.put(irfv.getFileVersionType(), irfv);
        }
        assertTrue(map.containsKey(VersionType.UPLOADED));
        assertTrue(map.containsKey(VersionType.WEB_LARGE));
        assertTrue(map.containsKey(VersionType.WEB_MEDIUM));
        assertTrue(map.containsKey(VersionType.WEB_SMALL));
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));

        assertEquals(map.get(VersionType.UPLOADED).getFilename(), TestConstants.TEST_DOCUMENT_NAME);
    }

    @Test
    @Rollback(true)
    public void testDeleteInformationResourceFile() throws InstantiationException, IllegalAccessException {
        InformationResource ir = generateInformationResourceWithFile();
        int count = ir.getInformationResourceFiles().size();
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            Long id = irFile.getId();
            informationResourceService.deleteInformationResourceFile(ir, irFile);
            InformationResourceFile irFile2 = informationResourceFileService.find(id);
            assertNull("testing whether the IrFile was actually deleted", irFile2);
            assertEquals(count - 1, ir.getInformationResourceFiles().size());
            count--;
        }

        assertEquals(0, ir.getInformationResourceFiles().size());
    }
    
    @Test
    @Rollback
    public void testFileStatus() throws Exception {
        InformationResource ir = generateInformationResourceWithFile();
        for (InformationResourceFile file: ir.getInformationResourceFiles()) {
            file.setStatus(FileStatus.QUEUED);
            assertFalse(file.isProcessed());
            genericService.save(file);
            flush();
            file.setStatus(FileStatus.PROCESSED);
            assertTrue(file.isProcessed());
            genericService.save(file);
            flush();
            file.setStatus(FileStatus.DELETED);
            genericService.save(file);
            flush();
        }
    }

}
