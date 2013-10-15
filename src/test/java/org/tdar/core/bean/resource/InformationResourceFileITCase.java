package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.workflow.ActionMessageErrorListener;

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
        InformationResource ir = generateDocumentWithFileAndUser();
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
        InformationResource ir = generateDocumentWithFileAndUser();

        assertEquals(ir.getInformationResourceFiles().size(), 1);
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        assertNotNull("IrFile is null", irFile);
        assertEquals(1, irFile.getLatestVersion().intValue());
        assertEquals(irFile.getLatestVersions().size(), 6);
        InformationResourceFileVersion irFileVersion = irFile.getLatestVersions().iterator().next();
        assertNotNull("IrFileVersion is null", irFileVersion);
        assertEquals(1, irFile.getLatestVersion().intValue());

        assertEquals(irFile.getInformationResourceFileType(), FileType.DOCUMENT);

        Map<VersionType, InformationResourceFileVersion> map = new HashMap<VersionType, InformationResourceFileVersion>();
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            map.put(irfv.getFileVersionType(), irfv);
        }
        assertTrue(map.containsKey(VersionType.UPLOADED));
        assertTrue(map.containsKey(VersionType.METADATA));
        assertTrue(map.containsKey(VersionType.WEB_LARGE));
        assertTrue(map.containsKey(VersionType.WEB_MEDIUM));
        assertTrue(map.containsKey(VersionType.WEB_SMALL));
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));

        assertEquals(map.get(VersionType.UPLOADED).getFilename(), TestConstants.TEST_DOCUMENT_NAME);
    }

    @Test
    @Rollback(true)
    public void testIndexableTextExtractionInPDF() throws InstantiationException, IllegalAccessException, IOException {
        InformationResource ir = generateDocumentWithFileAndUser();
        List<Long> irfvids = new ArrayList<Long>();
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        Map<VersionType, InformationResourceFileVersion> map = new HashMap<VersionType, InformationResourceFileVersion>();
        logger.info("versions: {} ", irFile.getInformationResourceFileVersions());
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            map.put(irfv.getFileVersionType(), irfv);
            irfv.setTransientFile(TdarConfiguration.getInstance().getFilestore().retrieveFile(irfv));
            irfvids.add(irfv.getId());
        }
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));
        InformationResourceFileVersion fileVersion = map.get(VersionType.INDEXABLE_TEXT);
        String text = FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(fileVersion));
        assertTrue(text.contains("Tree-Ring Research, University of Arizona, Tucson"));
    }

    @Test
    @Rollback(true)
    public void testReprocessInformationResourceFile() throws Exception {
        InformationResource ir = generateDocumentWithFileAndUser();

        assertEquals(ir.getInformationResourceFiles().size(), 1);
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        assertNotNull("IrFile is null", irFile);
        assertEquals(1, irFile.getLatestVersion().intValue());
        assertEquals(6, irFile.getLatestVersions().size());
        InformationResourceFileVersion irFileVersion = irFile.getLatestVersions().iterator().next();
        assertNotNull("IrFileVersion is null", irFileVersion);
        assertEquals(1, irFile.getLatestVersion().intValue());

        assertEquals(irFile.getInformationResourceFileType(), FileType.DOCUMENT);
        List<Long> irfvids = new ArrayList<>();
        Map<VersionType, InformationResourceFileVersion> map = new HashMap<>();
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            map.put(irfv.getFileVersionType(), irfv);
            irfvids.add(irfv.getId());
        }
        assertTrue(map.containsKey(VersionType.METADATA));
        assertTrue(map.containsKey(VersionType.UPLOADED));
        assertTrue(map.containsKey(VersionType.WEB_LARGE));
        assertTrue(map.containsKey(VersionType.WEB_MEDIUM));
        assertTrue(map.containsKey(VersionType.WEB_SMALL));
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));

        assertEquals(map.get(VersionType.UPLOADED).getFilename(), TestConstants.TEST_DOCUMENT_NAME);

        genericService.synchronize();
        ActionMessageErrorListener listener = new ActionMessageErrorListener();
        informationResourceService.reprocessInformationResourceFiles(ir, listener);

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
        InformationResource ir = generateDocumentWithFileAndUser();
        int count = ir.getInformationResourceFiles().size();
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            Long id = irFile.getId();
            informationResourceFileService.delete(irFile);
            // genericService.synchronize();
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
        InformationResource ir = generateDocumentWithFileAndUser();
        for (InformationResourceFile file : ir.getInformationResourceFiles()) {
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
