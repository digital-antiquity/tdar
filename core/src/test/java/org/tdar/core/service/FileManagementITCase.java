package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.CurationState;
import org.tdar.core.bean.file.FileComment;
import org.tdar.core.bean.file.Mark;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.DirSummary;
import org.tdar.core.dao.RecentFileSummary;


public class FileManagementITCase extends AbstractIntegrationTestCase implements TestBillingAccountHelper {

    private static final String DIR = "dir";
    private static final String SUBDIR = "subdir";
    @Autowired
    private PersonalFilestoreService pfs;

    public List<AbstractFile> setupSomeFilesAndDirs(BillingAccount act, BillingAccount act2) throws FileNotFoundException {
        List<AbstractFile> files = new ArrayList<>();
        files.add(new TdarFile(TestConstants.getFile(TestConstants.TEST_DOCUMENT_SMALL), getBasicUser(), act));
        files.add(new TdarFile(TestConstants.getFile(TestConstants.TEST_DOCUMENT), getBasicUser(), act));
        TdarFile img = new TdarFile(TestConstants.getFile(TestConstants.TEST_IMAGE), getBasicUser(), act);
        files.add(img);
        TdarFile img2 = new TdarFile(TestConstants.getFile(TestConstants.TEST_IMAGE2), getBasicUser(), act);
        files.add(img2);
        files.add(new TdarFile(TestConstants.getFile(TestConstants.TEST_DATASET), getBasicUser(), act));
        TdarDir dir = new TdarDir(getBasicUser(), act, DIR);
        TdarDir subdir = new TdarDir(getBasicUser(), act, SUBDIR);
        subdir.setParent(dir);
        img.setParent(dir);
        img2.setParent(dir);
        files.add(dir);
        files.add(subdir);
        TdarFile gis1 = new TdarFile(TestConstants.getFile(TestConstants.TEST_GEOTIFF), getBasicUser(), act);
        TdarFile gis2 = new TdarFile(TestConstants.getFile(TestConstants.TEST_GEOTIFF_TFW), getBasicUser(), act);
        gis1.setParent(subdir);
        gis2.setParent(subdir);
        files.add(gis1);
        files.add(gis2);
        // setup a second account with one file
        TdarFile gis3 = new TdarFile(TestConstants.getFile(TestConstants.TEST_GEOTIFF), getBasicUser(), act2);
        files.add(gis3);
        genericService.saveOrUpdate(files);
        return files;
    }

    @Test
    @Rollback
    public void testListDirForUsers() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        setupSomeFilesAndDirs(act, act2);
        genericService.synchronize();
        List<AbstractFile> listFiles = pfs.listFiles(null, act, null,null,  getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(4, listFiles.size());

        listFiles = pfs.listFiles(null, act, null,null,  getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(4, listFiles.size());

        listFiles = pfs.listFiles(null, act2, null,null,  getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(1, listFiles.size());

        listFiles = pfs.listFiles(null, act2, null,null,  getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(1, listFiles.size());

    }

    @Test
    @Rollback
    public void testFindFilesForUsers() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        setupSomeFilesAndDirs(act, act2);
        genericService.synchronize();
        List<AbstractFile> listFiles = pfs.listFiles(null, act, "Pundo", null, getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(1, listFiles.size());

        listFiles = pfs.listFiles(null, act, "untitled", null, getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(2, listFiles.size());

    }

    @Test(expected = FileAlreadyExistsException.class)
    @Rollback
    public void testCreateDirAlreadyExists() throws FileNotFoundException, FileAlreadyExistsException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        setupSomeFilesAndDirs(act, act2);
        pfs.createDirectory(null, DIR, act, getBasicUser());
    }

    @Test
    @Rollback
    public void testCreateDirDoesntExists() throws FileNotFoundException, FileAlreadyExistsException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        setupSomeFilesAndDirs(act, act2);
        TdarDir dir = pfs.createDirectory(null, SUBDIR, act, getBasicUser());
        assertNotNull(dir);
    }

    @Test
    @Rollback
    public void testEditMetadata() throws FileNotFoundException {
        TdarFile file = new TdarFile(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, TestConstants.PUNDO_FAUNAL_REMAINS_XLS), getBasicUser(),
                setupAccountForPerson(getBasicUser()));
        genericService.saveOrUpdate(file);
        assertEquals(null, file.getNote());
        assertEquals(false, file.getRequiresOcr());
        assertEquals(CurationState.CHOOSE, file.getCuration());
        String note = "test note";
        pfs.editMetadata(file, note, true, CurationState.WILL_CURATE, getAdminUser());
        Long fileId = file.getId();
        file = null;
        genericService.synchronize();
        file = genericService.find(TdarFile.class, fileId);
        assertEquals(note, file.getNote());
        assertEquals(true, file.getRequiresOcr());
        assertEquals(CurationState.WILL_CURATE, file.getCuration());
    }

    @Test
    @Rollback
    public void testAddComments() throws FileNotFoundException {
        TdarFile file = new TdarFile(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, TestConstants.PUNDO_FAUNAL_REMAINS_XLS), getBasicUser(),
                setupAccountForPerson(getBasicUser()));
        genericService.saveOrUpdate(file);
        String comment = "my test comment";
        pfs.addComment(file, comment, getAdminUser());
        String comment2 = "second comment";
        pfs.addComment(file, comment2, getBasicUser());
        Long fileId = file.getId();
        file = null;
        genericService.synchronize();
        file = genericService.find(TdarFile.class, fileId);
        assertEquals(2, file.getComments().size());
        for (FileComment c: file.getComments()) {
            if (StringUtils.equals(comment, c.getComment())) {
                comment = null;
            } else  if (StringUtils.equals(comment2, c.getComment())) {
                comment2 = null;
            } else {
                fail("unknown comment");
            }
        }
        assertNull(comment);
        assertNull(comment2);
    }

    @Test
    @Rollback
    public void testResolveComments() throws FileNotFoundException {
        TdarFile file = new TdarFile(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, TestConstants.PUNDO_FAUNAL_REMAINS_XLS), getBasicUser(),
                setupAccountForPerson(getBasicUser()));
        genericService.saveOrUpdate(file);
        Long commentId = pfs.addComment(file, "my test comment", getAdminUser()).getId();
        Long fileId = file.getId();
        file = null;
        genericService.synchronize();
        file = genericService.find(TdarFile.class, fileId);
        assertEquals(1, file.getComments().size());
        FileComment comment = genericService.find(FileComment.class, commentId);
        pfs.resolveComment(file, comment, getBasicUser());
        file = null;
        genericService.synchronize();
        file = genericService.find(TdarFile.class, fileId);
        assertEquals(1, file.getComments().size());
        comment = genericService.find(FileComment.class, commentId);
        assertEquals(true, comment.getResolved());
        assertEquals(getBasicUser(), comment.getResolver());
        assertNotNull(comment.getDateResolved());

    }

    @Test
    @Rollback
    public void testListDirectories() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        setupSomeFilesAndDirs(act, act2);
        genericService.synchronize();
        List<TdarDir> listDirectories = pfs.listDirectories(null, act, getBasicUser());
        logger.debug("{}", listDirectories);
        assertEquals(2, listDirectories.size());

        listDirectories = pfs.listDirectories(null, act, getBasicUser());
        logger.debug("{}", listDirectories);
        assertEquals(2, listDirectories.size());

        listDirectories = pfs.listDirectories(null, act2, getBasicUser());
        logger.debug("{}", listDirectories);
        assertEquals(0, listDirectories.size());

    }

    @Test
    @Rollback
    public void testMoveFilesToAccount() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        List<AbstractFile> files = setupSomeFilesAndDirs(act, act2);
        List<AbstractFile> pdf = new ArrayList<>();
        for (AbstractFile f : files) {
            if (f.getName().contains("pdf")) {
                pdf.add((TdarFile) f);
            }
        }

        pfs.moveFilesBetweenAccounts(pdf, act2, getAdminUser());

        List<AbstractFile> listFiles = pfs.listFiles(null, act2, null, null, getAdminUser());
        assertEquals(3, listFiles.size());

    }

    @Test
    @Rollback
    public void testMoveFolderToAccount() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        List<AbstractFile> files = setupSomeFilesAndDirs(act, act2);
        List<AbstractFile> pdf = new ArrayList<>();
        for (AbstractFile f : files) {
            if (f.getName().contains(SUBDIR)) {
                pdf.add(f);
            }
        }

        List<AbstractFile> moved = pfs.moveFilesBetweenAccounts(pdf, act2, getAdminUser());
        assertEquals(3, moved.size());
        for (AbstractFile af : moved) {
            logger.debug("{} - {}", af.getAccount().getId(), af);
            assertEquals(act2, af.getAccount());
        }
        moved = null;
        files = null;
        genericService.synchronize();
        List<AbstractFile> listFiles = pfs.listFiles(null, act2, null,null,  getAdminUser());
        logger.debug("{}", listFiles);
        // dir and initial file
        assertEquals(2, listFiles.size());
    }

    @Test
    @Rollback
    public void testMarkUnmark() throws FileNotFoundException {
        TdarFile file = new TdarFile(TestConstants.getFile(TestConstants.TEST_DATA_INTEGRATION_DIR, TestConstants.PUNDO_FAUNAL_REMAINS_XLS), getBasicUser(),
                setupAccountForPerson(getBasicUser()));
        genericService.saveOrUpdate(file);
        for (Mark mark : Mark.values()) {
            pfs.mark(Arrays.asList(file), mark, getBillingUser());
            switch (mark) {
                case CURATED:
                    assertEquals(file.getCuratedBy(), getBillingUser());
                    assertNotNull(file.getDateCurated());
                    break;
                case EXTERNAL_REVIEWED:
                    assertEquals(file.getExternalReviewedBy(), getBillingUser());
                    assertNotNull(file.getDateExternalReviewed());
                    break;
                case INITIAL_REVIEWED:
                    assertEquals(file.getInitialReviewedBy(), getBillingUser());
                    assertNotNull(file.getDateInitialReviewed());
                    break;
                case REVIEWED:
                    assertEquals(file.getReviewedBy(), getBillingUser());
                    assertNotNull(file.getDateReviewed());
                    break;
                default:
                    break;
            }
            pfs.unMark(Arrays.asList(file), mark, getBillingUser());
            switch (mark) {
                case CURATED:
                    assertNull(file.getCuratedBy());
                    assertNull(file.getDateCurated());
                    break;
                case EXTERNAL_REVIEWED:
                    assertNull(file.getExternalReviewedBy());
                    assertNull(file.getDateExternalReviewed());
                    break;
                case INITIAL_REVIEWED:
                    assertNull(file.getInitialReviewedBy());
                    assertNull(file.getDateInitialReviewed());
                    break;
                case REVIEWED:
                    assertNull(file.getReviewedBy());
                    assertNull(file.getDateReviewed());
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    @Rollback
    public void testListSubDirForUsers() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        List<AbstractFile> files = setupSomeFilesAndDirs(act, act2);
        TdarDir dir = null;
        for (AbstractFile file : files) {
            if (file.getName().equals(SUBDIR)) {
                dir = (TdarDir) file;
            }
        }

        genericService.synchronize();
        List<AbstractFile> listFiles = pfs.listFiles(dir, act, null,null,  getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(2, listFiles.size());

        listFiles = pfs.listFiles(dir, act, null, null, getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(2, listFiles.size());
    }
    
    @Test
    @Rollback
    public void testRecentFiles() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        List<AbstractFile> files = setupSomeFilesAndDirs(act, act);
        
        genericService.synchronize();
        RecentFileSummary recentByAccount = pfs.recentByAccount(act, DateTime.now().minusWeeks(1).toDate(), null, getAdminUser());
        assertTrue(files.containsAll(recentByAccount.getFiles()));
    }

    @Test
    @Rollback
    public void testSummaryFiles() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        List<AbstractFile> files = new ArrayList<>();
        files.add(new TdarFile(TestConstants.getFile(TestConstants.TEST_DOCUMENT_SMALL), getBasicUser(), act));
        files.add(new TdarFile(TestConstants.getFile(TestConstants.TEST_DOCUMENT), getBasicUser(), act));
        TdarFile img = new TdarFile(TestConstants.getFile(TestConstants.TEST_IMAGE), getBasicUser(), act);
        files.add(img);
        TdarFile img2 = new TdarFile(TestConstants.getFile(TestConstants.TEST_IMAGE2), getBasicUser(), act);
        files.add(img2);
        files.add(new TdarFile(TestConstants.getFile(TestConstants.TEST_DATASET), getBasicUser(), act));
        TdarDir dir = new TdarDir(getBasicUser(), act, DIR);
        TdarDir subdir = new TdarDir(getBasicUser(), act, SUBDIR);
        subdir.setParent(dir);
        img.setParent(subdir);
        img2.setParent(subdir);
        files.add(dir);
        files.add(subdir);
        TdarFile gis1 = new TdarFile(TestConstants.getFile(TestConstants.TEST_GEOTIFF), getBasicUser(), act);
        TdarFile gis2 = new TdarFile(TestConstants.getFile(TestConstants.TEST_GEOTIFF_TFW), getBasicUser(), act);
        gis1.setParent(subdir);
        gis2.setParent(subdir);
        files.add(gis1);
        files.add(gis2);
        // setup a second account with one file
        TdarFile gis3 = new TdarFile(TestConstants.getFile(TestConstants.TEST_GEOTIFF), getBasicUser(), act);
        files.add(gis3);
        genericService.saveOrUpdate(files);
        
        genericService.synchronize();
        DirSummary summary = pfs.summarizeAccountBy(act, null, getAdminUser());
        logger.debug(String.format("total: %s, %s, %s, %s, %s",summary.getCurated(), summary.getResource(), summary.getInitialReviewed(), summary.getReviewed(),  summary.getExternalReviewed()));
        summary.getParts().forEach(sum -> {
            logger.debug(String.format("%s: %s, %s, %s, %s, %s", sum.getDir() ,sum.getCurated(), sum.getResource(), sum.getInitialReviewed(), sum.getReviewed(), sum.getExternalReviewed()));
        });
    }

}
