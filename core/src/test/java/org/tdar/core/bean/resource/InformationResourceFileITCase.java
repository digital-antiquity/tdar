package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.InformationResourceFileVersionDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;

public class InformationResourceFileITCase extends AbstractIntegrationTestCase {

    @Autowired
    InformationResourceFileService informationResourceFileService;
    @Autowired
    InformationResourceFileVersionDao informationResourceFileVersionDao;

    @Autowired
    InformationResourceService informationResourceService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    GenericService genericService;
    @Autowired
    SerializationService serializationService;

    @Test
    public void testXMLSave() throws InstantiationException, IllegalAccessException, Exception {
        for (Document resource : genericService.findAll(Document.class)) {
            serializationService.handleFilestoreEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }

        for (ResourceCollection resource : genericService.findAll(ResourceCollection.class)) {
            serializationService.handleFilestoreEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }

        for (Creator<?> resource : genericService.findAll(Creator.class)) {
            serializationService.handleFilestoreEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }
    }

    @Test
    @Rollback
    public void findByFilename() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();
        InformationResourceFile foundFile = informationResourceService.findFileByFilename(ir, TestConstants.TEST_DOCUMENT_NAME);
        assertNotNull(foundFile);
        boolean found = false;
        for (InformationResourceFile file : ir.getInformationResourceFiles()) {
            if (file.equals(foundFile)) {
                found = true;
            }
        }
        assertTrue(found);

    }

    @Test
    @Rollback(true)
    public void testCreateInformationResourceFile() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();

        assertEquals(ir.getInformationResourceFiles().size(), 1);
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        assertNotNull("IrFile is null", irFile);
        assertEquals(1, irFile.getLatestVersion().intValue());
        int size = irFile.getLatestVersions().size();
        if ((size != 3) && (size != 6)) {
            Assert.fail("wrong number of derivatives found");
        }

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
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();
        List<Long> irfvids = new ArrayList<Long>();
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        Map<VersionType, InformationResourceFileVersion> map = new HashMap<VersionType, InformationResourceFileVersion>();
        logger.info("versions: {} ", irFile.getInformationResourceFileVersions());
        for (InformationResourceFileVersion irfv : irFile.getInformationResourceFileVersions()) {
            map.put(irfv.getFileVersionType(), irfv);
            irfv.setTransientFile(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, irfv));
            irfvids.add(irfv.getId());
        }
        assertTrue(map.containsKey(VersionType.INDEXABLE_TEXT));
        InformationResourceFileVersion fileVersion = map.get(VersionType.INDEXABLE_TEXT);
        String text = FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, fileVersion));
        assertTrue(text.contains("Grand Canyon Adjacent Lands Project"));
    }

    @Test
    @Rollback(true)
    public void testReprocessInformationResourceFile() throws Exception {
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();

        assertEquals(ir.getInformationResourceFiles().size(), 1);
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        assertNotNull("IrFile is null", irFile);
        assertEquals(1, irFile.getLatestVersion().intValue());
        int size = irFile.getLatestVersions().size();
        if ((size != 3) && (size != 6)) {
            Assert.fail("wrong number of derivatives found");
        }

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

        evictCache();
        @SuppressWarnings("unused")
        ErrorTransferObject errors = informationResourceService.reprocessInformationResourceFiles(ir);

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
    public void testDeleteInformationResourceFile() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();
        boolean seen = false;
        try {
            InformationResourceFile irFile = ir.getFirstInformationResourceFile();
            Long id = irFile.getId();
            logger.info("{}", irFile);
            informationResourceFileService.delete(irFile);
            evictCache();
            InformationResourceFile irFile2 = informationResourceFileService.find(id);
            assertNull("testing whether the IrFile was actually deleted", irFile2);
            assertEquals(0, ir.getInformationResourceFiles().size());
        } catch (Throwable e) {
            logger.debug(e.getMessage());
            if (e.getMessage().contains("method not implemented")) {
                seen = true;
            }
        }
        assertTrue("should see exception", seen);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testFileStatus() throws Exception {
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();
        for (InformationResourceFile file : ir.getInformationResourceFiles()) {
            file.setStatus(FileStatus.QUEUED);
            assertFalse(file.isProcessed());
            genericService.saveOrUpdate(file);
            flush();
            file.setStatus(FileStatus.PROCESSED);
            assertTrue(file.isProcessed());
            genericService.saveOrUpdate(file);
            flush();
            genericService.synchronize();

        }
    }

    @Rollback(true)
    @Test
    public void testVersionDeletion() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        InformationResource ir = createAndSaveDocumentWithFileAndUseDefaultUser();
        InformationResourceFile irFile = ir.getInformationResourceFiles().iterator().next();
        InformationResourceFileVersion version = getVersion(irFile, VersionType.WEB_LARGE);
        File file = version.getTransientFile();
        informationResourceFileVersionDao.delete(version, false);
        assertTrue(file.exists());
        version = getVersion(irFile, VersionType.WEB_MEDIUM);
        file = version.getTransientFile();
        informationResourceFileVersionDao.delete(version, true);
        assertFalse(file.exists());

        version = getVersion(irFile, VersionType.WEB_SMALL);
        file = version.getTransientFile();
        informationResourceFileVersionDao.delete(version);
        assertTrue(file.exists());

    }

    private InformationResourceFileVersion getVersion(InformationResourceFile irFile, VersionType type) throws FileNotFoundException {
        InformationResourceFileVersion version = irFile.getCurrentVersion(type);
        assertNotNull(version);
        Filestore filestore = TdarConfiguration.getInstance().getFilestore();
        File file = filestore.retrieveFile(FilestoreObjectType.RESOURCE, version);
        version.setTransientFile(file);
        return version;
    }

}
