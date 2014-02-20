package org.tdar.core.service;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.DownloadController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;

public class DownloadServiceITCase extends AbstractDataIntegrationTestCase {
    private static final File ROOT_DEST = new File("target/test/download-service-it-case");
    private static final File ROOT_SRC = new File(TestConstants.TEST_ROOT_DIR);

    // don't need injection (yet)
    DownloadService downloadService = new DownloadService();
    int COVER_PAGE_WIGGLE_ROOM = 155_000;

    @Before
    public void prepareDir() throws IOException {
        FileUtils.forceMkdir(ROOT_DEST);
        FileUtils.cleanDirectory(ROOT_DEST);
    }

    @After
    public void cleanup() throws IOException {
        try {
            FileUtils.cleanDirectory(ROOT_DEST);
        } catch (Exception e) {
            logger.error("{} ", e);
        }
    }

    public void assertArchiveContents(Collection<File> expectedFiles, File archive) throws IOException {
        assertArchiveContents(expectedFiles, archive, true);
    }

    public void assertArchiveContents(Collection<File> expectedFiles, File archive, boolean strict) throws IOException {
        TConfig.get().setArchiveDetector(TArchiveDetector.ALL);
        TFile arc = new TFile(archive, TArchiveDetector.ALL);

        if (!arc.exists())
            fail("file does not exist:" + archive);
        if (!arc.isArchive())
            fail("file is not an archive:" + archive);

        HashMap<String, TFile> dmap = new HashMap<>();
        Assert.assertNotNull(arc);
        logger.info("files:{}", arc.list());
        for (TFile file : arc.listFiles()) {
            dmap.put(file.getName(), file);
        }

        List<String> errs = new ArrayList<String>();
        for (File expected : expectedFiles) {
            TFile actual = (TFile) dmap.get(expected.getName());
            if (actual == null) {
                errs.add("expected file not in archive:" + actual.getName());
                continue;
            }
            File temp = File.createTempFile("test123", ".tmp");
            actual.cp(temp);
            //if doing a strict test,  assert that file is exactly the same
            if(strict) {
                if (!FileUtils.contentEquals(expected, temp)) {
                    errs.add(String.format("%s: item in archive %s does not have same content", actual, expected));
                }
            //otherwise,  just make sure that the actual file is not empty
            } else {
                if(expected.length() > 0) {
                    assertThat(temp.length(), greaterThan(0L));
                }
            }
        }
        if (errs.size() > 0) {
            for (String err : errs) {
                logger.error(err);
            }
            fail("problems found in archive:" + archive);
        }
    }

    // get some files from the test dir and put them into an archive stream
    @Test
    @Rollback
    public void testDownloadArchiveService() throws IOException {
        Map<File,String> map = new HashMap<>();
        for (File file : FileUtils.listFiles(ROOT_SRC, null, false)) {
            map.put(file, null);
        }
        File dest = new File(ROOT_DEST, "everything.zip");
        
        downloadService.generateZipArchive(map, dest);
        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
        assertArchiveContents(map.keySet(), dest);
    }

    @Test
    @Rollback
    public void testDownloadArchiveController() throws IOException, InstantiationException, IllegalAccessException, TdarActionException {

        List<File> files = new ArrayList<>();
        File file1 = new File(TestConstants.TEST_DOCUMENT_DIR + "/a2-15.pdf");
        files.add(file1);
        files.add(new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME));
        Document document = generateDocumentWithUser();
        for(File file : files) {
            addFileToResource(document, file);
        }

        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        controller.setInformationResourceId(document.getId());
        assertEquals(TdarActionSupport.SUCCESS, controller.downloadZipArchive());
        logger.info(controller.getFileName());
        File file = File.createTempFile("test", ".zip");
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(controller.getInputStream(), output);
        IOUtils.closeQuietly(output);
        IOUtils.closeQuietly(controller.getInputStream());
        assertTrue("file should have been created", file.exists());
        assertTrue("file should be non-empty", file.length() > 0);
        TVFS.umount();

        //don't do strict test since the downloaded pdf's will have a cover page
        assertArchiveContents(files, file, false);
    }

    
    @Test
    @Rollback
    public void testDownloadArchiveControllerWithDeleted() throws IOException, InstantiationException, IllegalAccessException, TdarActionException {

        List<File> files = new ArrayList<>();
        File file1 = new File(TestConstants.TEST_DOCUMENT_DIR + "/a2-15.pdf");
        files.add(file1);
//        files.add(new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME));
        Document document = generateDocumentWithUser();
        for(File file : files) {
            addFileToResource(document, file);
        }
        
        InformationResourceFile deleted = document.getFirstInformationResourceFile();
        deleted.setStatus(FileStatus.DELETED);
        genericService.saveOrUpdate(deleted);
        
        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        controller.setInformationResourceId(document.getId());
        assertEquals(TdarActionSupport.ERROR, controller.downloadZipArchive());
        logger.info(controller.getFileName());
    }


    @Test
    @Rollback
    public void testDownloadController() throws IOException, InstantiationException, IllegalAccessException, TdarActionException {

        Document doc = generateDocumentWithFileAndUser();
        genericService.saveOrUpdate(doc);
        final Long id = doc.getId();
        logger.debug("{}", doc.getFirstInformationResourceFile().getLatestPDF());
        genericService.synchronize();

        Document document =genericService.find(Document.class, id);
        assertTrue(Persistable.Base.isNotNullOrTransient(document));
        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        // controller.setInformationResourceId(document.getId());

        controller.setInformationResourceFileId(document.getFirstInformationResourceFile().getLatestPDF().getId());
        try {
            assertEquals(TdarActionSupport.SUCCESS, controller.execute());
            assertEquals(TestConstants.TEST_DOCUMENT_NAME, controller.getFileName());
        } catch (TdarActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}
