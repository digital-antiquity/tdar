package org.tdar.core.service;

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
import org.tdar.core.ArchiveEvaluator;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.download.DownloadFile;
import org.tdar.core.service.download.DownloadService;
import org.tdar.core.service.download.DownloadTransferObject;
import org.tdar.filestore.Filestore;

public class DownloadServiceITCase extends AbstractIntegrationTestCase {
    private static final File ROOT_DEST = new File("target/test/download-service-it-case");
    private static final File ROOT_SRC = new File("src/test/resources");
    @SuppressWarnings("unused")
    private Filestore filestore = TdarConfiguration.getInstance().getFilestore();
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
    @Test
    @Rollback
    public void testDownloadArchiveService() throws Exception {
        DownloadTransferObject dto = new DownloadTransferObject(downloadService);
        dto.setAuthenticatedUser(getBillingUser());
        List<File> files = new ArrayList<>();
        InformationResourceFile irf = new InformationResourceFile();
        irf.setInformationResource(new Document());
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.ARCHIVAL, "test.txt", irf);
        for (File file : FileUtils.listFiles(ROOT_SRC, null, false)) {
            dto.getDownloads().add(new DownloadFile(file, file.getName(), version));
            irf.setId(irf.getId() + 1);
            files.add(file);
        }
        File dest = new File(ROOT_DEST, "everything.zip");
        InputStream inputStream = dto.getInputStream();
        IOUtils.copy(inputStream, new FileOutputStream(dest));
        IOUtils.closeQuietly(inputStream);
        logger.debug("{}", dest);

        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
        ArchiveEvaluator.assertArchiveContents(files, dest);
    }

    // get some files from the test dir and put them into an archive stream
    @Test
    @Rollback
    public void testDownloadArchiveWithDups() throws Exception {
        DownloadTransferObject dto = new DownloadTransferObject(downloadService);
        dto.setAuthenticatedUser(getBillingUser());
        List<File> files = new ArrayList<>();
        InformationResourceFile irf = new InformationResourceFile();
        irf.setInformationResource(new Document());
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.ARCHIVAL, "test.txt", irf);
        for (File file : FileUtils.listFiles(ROOT_SRC, null, false)) {
            dto.getDownloads().add(new DownloadFile(file, file.getName(), version));
            irf.setId(irf.getId() + 1);
            files.add(file);
        }
        for (File file : FileUtils.listFiles(ROOT_SRC, null, false)) {
            dto.getDownloads().add(new DownloadFile(file, file.getName(), version));
            irf.setId(irf.getId() + 1);
            files.add(file);
        }
        File dest = new File(ROOT_DEST, "everything.zip");
        InputStream inputStream = dto.getInputStream();
        IOUtils.copy(inputStream, new FileOutputStream(dest));
        IOUtils.closeQuietly(inputStream);
        logger.debug("{}", dest);

        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
        ArchiveEvaluator.assertArchiveContents(files, dest);
    }

}
