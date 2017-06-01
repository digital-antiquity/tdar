/**
 * 
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.tdar.TestConstants.TEST_DOCUMENT;
import static org.tdar.TestConstants.TEST_DOCUMENT_NAME;
import static org.tdar.TestConstants.TEST_IMAGE;
import static org.tdar.TestConstants.TEST_IMAGE_NAME;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.workflow.workflows.FileArchiveWorkflow;
import org.tdar.filestore.BaseFilestore;
import org.tdar.filestore.Filestore.StorageMethod;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.PairtreeFilestore;

import com.opensymphony.xwork2.interceptor.annotations.Before;

/**
 * @author Adam Brin
 * 
 */
public class FilestoreTest {

    public static final Long INFORMATION_RESOURCE_ID = 12345l;
    public static final Long INFORMATION_RESOURCE_FILE_ID = 1234l;
    public static final Long INFORMATION_RESOURCE_FILE_VERSION_ID = 1112l;
    private static final Integer VERSION = 1;
    public static String baseIrPath = File.separator + "12" + File.separator + "34" + File.separator + "5" + File.separator + PairtreeFilestore.CONTAINER_NAME
            + File.separator;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @SuppressWarnings("unused")
    private TdarConfiguration tdarConfig = TdarConfiguration.getInstance();

    @Before
    public void cleanup() {
        File f = new File(TestConstants.FILESTORE_PATH);
        System.out.println(f.getAbsolutePath());
        try {
            FileUtils.deleteDirectory(f);
            f = new File(TestConstants.FILESTORE_PATH);
            f.mkdirs();
            logger.debug(f.getAbsolutePath());
        } catch (IOException e) {
            logger.info("Couldn't cleanup filestore, perhaps you're on windows...", e);
        }
    }

    @Test
    public void filestorePairtreeTest() {
        assertEquals(PairtreeFilestore.toPairTree(1234567890), File.separator + 12 + File.separator + 34 + File.separator + 56 + File.separator + 78
                + File.separator + 90 + File.separator + PairtreeFilestore.CONTAINER_NAME + File.separator);
    }

    @Test
    @SuppressWarnings("static-method")
    public void sanitizeFilenameTest() {
        assertEquals("abc.txt", BaseFilestore.sanitizeFilename("abc.txt"));
        assertEquals("abc.txt", BaseFilestore.sanitizeFilename("abc'.txt"));
        assertEquals("abc.tar.gz", BaseFilestore.sanitizeFilename("abc.tar.gz"));
        assertEquals("abc.tar.bz2", BaseFilestore.sanitizeFilename("abc.tar.bz2"));
        assertEquals("abc-tar.bz2", BaseFilestore.sanitizeFilename("abc-tar.bz2"));
        assertEquals("abc.tar.bz2", BaseFilestore.sanitizeFilename("abc-.tar.bz2"));
        assertEquals("abc-a----------_----+-----.txt", BaseFilestore.sanitizeFilename("abc\"a!@#$%^&*()_{}[]+<>?/\\\\.txt"));
        for (String archiveExtension : FileArchiveWorkflow.ARCHIVE_EXTENSIONS_SUPPORTED) {
            String fileName = "test." + archiveExtension;
            String sanitizedFileName = BaseFilestore.sanitizeFilename(fileName);
            assertEquals("Oh-oh: filename should not have altered from: " + fileName + " to: " + sanitizedFileName, fileName, sanitizedFileName);
        }
    }

    @Test
    @Rollback(true)
    public void filestoreFilenameTest() {
        cleanup();
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        String name = "abc.txt";
        InformationResourceFileVersion version = generateVersion(name);
        String baseAssert = store.getFilestoreLocation() + File.separator + "resource" + baseIrPath + INFORMATION_RESOURCE_FILE_ID + File.separator + "v1" + File.separator;
        assertEquals(baseAssert + "archival" + File.separator + name, store.getAbsoluteFilePath(FilestoreObjectType.RESOURCE, version));
        version.setFileVersionType(VersionType.WEB_LARGE);
        assertEquals(baseAssert + PairtreeFilestore.DERIV + File.separator + name, store.getAbsoluteFilePath(FilestoreObjectType.RESOURCE, version));
        logger.info(store.getAbsoluteFilePath(FilestoreObjectType.RESOURCE, version));
    }

    @Test
    @Rollback(true)
    public void filestorePopulateVersionTest() throws IOException {
        cleanup();
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        InformationResourceFileVersion version = generateVersion(TEST_DOCUMENT_NAME);
        File f = new File(TEST_DOCUMENT);
        store.store(FilestoreObjectType.RESOURCE, f, version);
        assertNotNull(version.getChecksum());
        assertEquals("MD5", version.getChecksumType());
        assertNotNull(version.getDateCreated());
        assertEquals("pdf", version.getExtension().toLowerCase());
        // assertNotNull(version.getFile());
        assertEquals(TEST_DOCUMENT_NAME, version.getFilename());
        assertEquals(null, version.getFileType());
        assertEquals(null, version.getFormat());
        assertEquals(INFORMATION_RESOURCE_FILE_VERSION_ID, version.getId());
        assertEquals(INFORMATION_RESOURCE_FILE_ID, version.getInformationResourceFileId());
        version.getFileVersionType();
        assertEquals(INFORMATION_RESOURCE_ID, version.getInformationResourceId());
        assertEquals("application/pdf", version.getMimeType());
        // version.getPath();
        assertEquals(null, version.getPremisId());
        assertEquals(f.length(), version.getFileLength().intValue());
        assertEquals(VERSION, version.getVersion());
        ;
        // version.getHeight();
        // version.getWidth();
    }

    @Test
    @Rollback(true)
    public void filestoreRotateTest() throws IOException {
        cleanup();
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        InformationResourceFileVersion version = generateVersion(TEST_DOCUMENT_NAME);
        version.setFileVersionType(VersionType.LOG);
        File f = new File(TEST_DOCUMENT);
        StorageMethod rotate = StorageMethod.ROTATE;
        store.storeAndRotate(FilestoreObjectType.RESOURCE, f, version, rotate);
        version.setTransientFile(f);
        store.storeAndRotate(FilestoreObjectType.RESOURCE, f, version, rotate);

        File tmpFile = store.retrieveFile(FilestoreObjectType.RESOURCE, version);
        assertTrue(tmpFile.exists());
        File rotated = new File(tmpFile.getParentFile(), String.format("%s.1.%s", FilenameUtils.getBaseName(tmpFile.getName()),
                FilenameUtils.getExtension(tmpFile.getName())));
        assertTrue(rotated.exists());
    }

    // @Test
    // @Rollback(true)
    // public void filestoreDateRotationTest() throws IOException {
    // cleanup();
    // PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
    // InformationResourceFileVersion version = generateVersion(TEST_DOCUMENT_NAME);
    // File f = new File(TEST_DOCUMENT);
    // StorageMethod rotate = StorageMethod.DATE;
    // store.storeAndRotate(f, version, rotate);
    //
    // File tmpFile = version.getFile();
    // assertTrue(tmpFile.exists());
    // File rotated = new File(tmpFile.getParentFile(), String.format("%s.1.%s", FilenameUtils.getBaseName(tmpFile.getName()),
    // FilenameUtils.getExtension(tmpFile.getName())));
    // assertTrue(rotated.exists());
    // }

    private InformationResourceFileVersion generateVersion(String name) {
        Document ir = new Document();
        ir.setId(INFORMATION_RESOURCE_ID);
        InformationResourceFile irFile = new InformationResourceFile();
        irFile.setId(INFORMATION_RESOURCE_FILE_ID);
        irFile.setInformationResource(ir);
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setVersion(VERSION);
        version.setId(INFORMATION_RESOURCE_FILE_VERSION_ID);
        version.setFilename(name);
        version.setInformationResourceFile(irFile);
        version.setFileVersionType(VersionType.UPLOADED_ARCHIVAL);
        return version;
    }

    @Test
    @Rollback(true)
    public void testStorageAndRetrieval() throws IOException {
        cleanup();
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        InformationResourceFileVersion version = generateVersion(TEST_DOCUMENT_NAME);
        store.store(FilestoreObjectType.RESOURCE, new File(TEST_DOCUMENT), version);
        File f = new File(store.getAbsoluteFilePath(FilestoreObjectType.RESOURCE, version));
        assertTrue("file exists: " + f.getCanonicalPath(), f.exists());
        String expectedPath = store.getFilestoreLocation() + File.separator + "resource" + baseIrPath + INFORMATION_RESOURCE_FILE_ID + File.separator + "v1";
        assertEquals(expectedPath + File.separator + "archival" + File.separator + TEST_DOCUMENT_NAME, f.getAbsolutePath());
        try {
            store.purge(FilestoreObjectType.RESOURCE, version);
        } catch (IOException e) {
            if ((System.getProperty("os.name").indexOf("indows") == -1) && e.getMessage().contains("Unable to delete file")) {
                logger.info("couldn't delete file... windows");
            } else {
                e.printStackTrace();
            }
        }
        // windows isn't so keen about renaming files it thinks are open, even if
        // they're not
        if (System.getProperty("os.name").indexOf("indows") == -1) {
            assertFalse(f.exists());
        }
        String expectedDeletedPath = expectedPath + PairtreeFilestore.DELETED_SUFFIX + File.separator + "archival" + File.separator + TEST_DOCUMENT_NAME;
        logger.info(expectedDeletedPath);
        // + File.separator + TEST_DOCUMENT_NAME
        assertTrue("deleted folder does not exist", new File(expectedDeletedPath).exists());
    }

    @Test
    @Rollback(true)
    public void testStorageAndRetrievalDeleted() throws IOException {
        cleanup();
        File file = new File(TEST_IMAGE);
        assertTrue(String.format("%s exists", file.getCanonicalFile()),file.exists());
        PairtreeFilestore store = new PairtreeFilestore(TestConstants.FILESTORE_PATH);
        InformationResourceFileVersion version = generateVersion(TEST_IMAGE_NAME);
        version.getInformationResourceFile().setLatestVersion(2);
        version.setVersion(2);
        version.setFileVersionType(VersionType.UPLOADED);
        store.store(FilestoreObjectType.RESOURCE, new File(TEST_IMAGE), version);
        assertTrue(store.verifyFile(FilestoreObjectType.RESOURCE, version));
        File f = new File(store.getAbsoluteFilePath(FilestoreObjectType.RESOURCE, version));
        assertTrue("file exists: " + f.getCanonicalPath(), f.exists());
        String expectedPath = store.getFilestoreLocation() + File.separator + "resource" + baseIrPath + INFORMATION_RESOURCE_FILE_ID + File.separator + "v2";
        assertEquals(expectedPath + File.separator + TEST_IMAGE_NAME, f.getAbsolutePath());
        try {
            store.purge(FilestoreObjectType.RESOURCE, version);
        } catch (IOException e) {
            if ((System.getProperty("os.name").indexOf("indows") == -1) && e.getMessage().contains("Unable to delete file")) {
                logger.info("couldn't delete file... windows");
            } else {
                e.printStackTrace();
            }
        }
        // windows isn't so keen about renaming files it thinks are open, even if
        // they're not
        if (System.getProperty("os.name").indexOf("indows") == -1) {
            assertFalse(f.exists());
        }
        String expectedDeletedPath = expectedPath + PairtreeFilestore.DELETED_SUFFIX + File.separator + TEST_IMAGE_NAME;
        logger.info(expectedDeletedPath);
        // logger.info(f.getAbsolutePath());
        assertTrue("deleted folder does not exist", new File(expectedDeletedPath).exists());
    }

}
