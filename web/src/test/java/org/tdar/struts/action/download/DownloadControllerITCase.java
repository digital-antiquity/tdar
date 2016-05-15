package org.tdar.struts.action.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.PdfService;
import org.tdar.core.service.download.DownloadPdfFile;
import org.tdar.core.service.download.DownloadService;
import org.tdar.core.service.download.DownloadTransferObject;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;

public class DownloadControllerITCase extends AbstractDataIntegrationTestCase {
    private static final File ROOT_DEST = new File("target/test/download-service-it-case");
//    private static final File ROOT_SRC = new File(TestConstants.TEST_ROOT_DIR);

    // don't need injection (yet)
    @Autowired
    DownloadService downloadService;
    int COVER_PAGE_WIGGLE_ROOM = 155_000;

    @Autowired
    PdfService pdfService;

    @Before
    public void prepareDir() throws IOException {
        FileUtils.forceMkdir(ROOT_DEST);
        FileUtils.cleanDirectory(ROOT_DEST);
    }

    @After
    public void cleanup() throws IOException {
        try {
            System.gc();
            FileUtils.cleanDirectory(ROOT_DEST);
        } catch (Exception e) {
            logger.error("{} ", e);
        }
    }


    // get some files from the test dir and put them into an archive stream
    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testDownloadPdf() throws Exception {
        DownloadTransferObject dto = new DownloadTransferObject(downloadService);
        dto.setAuthenticatedUser(getBillingUser());
        List<File> files = new ArrayList<>();
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + "sample_pdf_formats/volume1-encrypted-test.pdf");
        Document document = generateAndStoreVersion(Document.class, file.getName(), file, filestore);
        InformationResourceFileVersion version = document.getLatestUploadedVersion();
        document.setTitle("test");
        document.setDescription("test");
        document.setDocumentType(DocumentType.BOOK);
        filestore.store(FilestoreObjectType.RESOURCE, file, version);
        logger.debug("{}", document);
        logger.debug("{}", version);
        logger.debug("{}", version.getTransientFile());
        DownloadPdfFile downloadPdfFile = new DownloadPdfFile(document, version, pdfService, getAdminUser(), MessageHelper.getInstance(), null);
        downloadPdfFile.setFile(file);
        dto.getDownloads().add(downloadPdfFile);

        File dest = new File(ROOT_DEST, "test.pdf");
        InputStream inputStream = dto.getInputStream();
        IOUtils.copy(inputStream, new FileOutputStream(dest));
        IOUtils.closeQuietly(inputStream);
        logger.debug("{}", dest);

        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
    }

    @Test
    @Rollback
    public void testDownloadArchiveController() throws Exception {

        List<File> files = new ArrayList<>();
        File file1 = new File(TestConstants.TEST_DOCUMENT_DIR + "/a2-15.pdf");
        files.add(file1);
        files.add(new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME));
        Document document = generateDocumentWithUser();
        for (File file : files) {
            addFileToResource(document, file);
        }

        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        controller.setInformationResourceId(document.getId());
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.downloadZipArchive());
        DownloadTransferObject downloadTransferObject = controller.getDownloadTransferObject();
        downloadTransferObject.setAuthenticatedUser(getBillingUser());
        logger.info(downloadTransferObject.getFileName());
        File file = File.createTempFile("test", ".zip");

        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(downloadTransferObject.getInputStream(), output);
        IOUtils.closeQuietly(output);
        IOUtils.closeQuietly(downloadTransferObject.getInputStream());
        assertTrue("file should have been created", file.exists());
        assertTrue("file should be non-empty", file.length() > 0);

        // don't do strict test since the downloaded pdf's will have a cover page
        assertArchiveContents(files, file, false);
    }

    @Test
    @Rollback
    public void testDownloadArchiveControllerWithDeleted() throws IOException, InstantiationException, IllegalAccessException {

        List<File> files = new ArrayList<>();
        File file1 = new File(TestConstants.TEST_DOCUMENT_DIR + "/a2-15.pdf");
        files.add(file1);
        // files.add(new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME));
        Document document = generateDocumentWithUser();
        for (File file : files) {
            addFileToResource(document, file);
        }

        InformationResourceFile deleted = document.getFirstInformationResourceFile();
        deleted.setDeleted(true);
        genericService.saveOrUpdate(deleted);

        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        controller.setInformationResourceId(document.getId());
        assertEquals(Action.ERROR, controller.downloadZipArchive());
    }

    @Test
    @Rollback
    public void testDownloadController() throws IOException, InstantiationException, IllegalAccessException {

        Document doc = generateDocumentWithFileAndUseDefaultUser();
        genericService.saveOrUpdate(doc);
        final Long id = doc.getId();
        logger.debug("{}", doc.getFirstInformationResourceFile().getLatestPDF());
        evictCache();

        Document document = genericService.find(Document.class, id);
        assertTrue(PersistableUtils.isNotNullOrTransient(document));
        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        // controller.setInformationResourceId(document.getId());

        controller.setInformationResourceFileVersionId(document.getFirstInformationResourceFile().getLatestPDF().getId());
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.execute());
        assertEquals(TestConstants.TEST_DOCUMENT_NAME, controller.getDownloadTransferObject().getFileName());

    }

    @Test
    @Rollback
    public void testDownloadControllerWithDeleted() throws IOException, InstantiationException, IllegalAccessException {

        Document doc = generateDocumentWithFileAndUseDefaultUser();
        doc.getFirstInformationResourceFile().setDeleted(true);
        genericService.saveOrUpdate(doc);
        final Long id = doc.getId();
        logger.debug("{}", doc.getFirstInformationResourceFile().getLatestPDF());
        evictCache();

        Document document = genericService.find(Document.class, id);
        assertTrue(PersistableUtils.isNotNullOrTransient(document));
        DownloadController controller = generateNewInitializedController(DownloadController.class, getBasicUser());

        controller.setInformationResourceFileVersionId(document.getFirstInformationResourceFile().getLatestPDF().getId());
        controller.prepare();
        assertEquals(Action.ERROR, controller.execute());

    }
}
