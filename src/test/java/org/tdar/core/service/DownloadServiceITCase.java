package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;
import org.tdar.struts.action.DownloadController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;

public class DownloadServiceITCase extends AbstractDataIntegrationTestCase {
    private static final File ROOT_DEST = new File("target/test/download-service-it-case");
    private static final File ROOT_SRC = new File(TestConstants.TEST_ROOT_DIR);

    // don't need injection (yet)
    DownloadService downloadService = new DownloadService();

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
        TConfig.get().setArchiveDetector(TArchiveDetector.ALL);
        TFile arc = new TFile(archive, TArchiveDetector.ALL);

        if (!arc.exists())
            fail("file does not exist:" + archive);
        if (!arc.isArchive())
            fail("file is not an archive:" + archive);

        HashMap<String, TFile> dmap = new HashMap<>();
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
            if (!FileUtils.contentEquals(expected, temp)) {
                errs.add(String.format("%s: item in archive %s does not have same content", actual, expected));
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
    public void testDownloadArchiveService() throws IOException {
        Collection<File> files = FileUtils.listFiles(ROOT_SRC, null, false);
        File dest = new File(ROOT_DEST, "everything.zip");
        downloadService.generateZipArchive(files, dest);
        assertTrue("file should have been created", dest.exists());
        assertTrue("file should be non-empty", dest.length() > 0);
        assertArchiveContents(files, dest);
    }

    @Test
    public void testDownloadArchiveController() throws IOException, InstantiationException, IllegalAccessException, TdarActionException {
        File file1 = new File(TestConstants.TEST_DOCUMENT_DIR + "/a2-15.pdf");
        List<File> files = new ArrayList<>();
        files.add(file1);
        files.add(new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME));
        Document document = generateDocumentWithFileAndUser();
        addFileToResource(document, file1);
        DownloadController controller = generateNewInitializedController(DownloadController.class, getAdminUser());
        controller.setInformationResourceId(document.getId());
        assertEquals(TdarActionSupport.SUCCESS, controller.downloadZipArchive());
        File file = File.createTempFile("test", ".zip");
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(controller.getInputStream(), output);
        IOUtils.closeQuietly(output);
        assertTrue("file should have been created", file.exists());
        assertTrue("file should be non-empty", file.length() > 0);
        assertArchiveContents(files, file);
    }

}
