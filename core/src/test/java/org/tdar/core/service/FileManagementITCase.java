package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.w3c.dom.ls.LSException;

public class FileManagementITCase extends AbstractIntegrationTestCase implements TestBillingAccountHelper {

    private static final String SUBDIR2 = "subdir";
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
        TdarDir dir = new TdarDir(getBasicUser(), act, "dir");
        TdarDir subdir = new TdarDir(getBasicUser(), act, SUBDIR2);
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
        List<AbstractFile> listFiles = pfs.listFiles(null, act, null, getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(4, listFiles.size());

        listFiles = pfs.listFiles(null, act, null, getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(4, listFiles.size());
        
        listFiles = pfs.listFiles(null, act2, null, getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(1, listFiles.size());

        listFiles = pfs.listFiles(null, act2, null, getBasicUser());
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
        List<AbstractFile> listFiles = pfs.listFiles(null, act, "Pundo", getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(1, listFiles.size());

        listFiles = pfs.listFiles(null, act, "untitled", getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(2, listFiles.size());
        
    }



    @Test
    @Rollback
    public void testListDirectories() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        setupSomeFilesAndDirs(act, act2);
        genericService.synchronize();
        List<TdarDir> listDirectories = pfs.listDirectories(null, act, getBasicUser());
        logger.debug("{}",listDirectories);
        assertEquals(2, listDirectories.size());

        listDirectories = pfs.listDirectories(null, act, getBasicUser());
        logger.debug("{}",listDirectories);
        assertEquals(2, listDirectories.size());

        listDirectories = pfs.listDirectories(null, act2, getBasicUser());
        logger.debug("{}",listDirectories);
        assertEquals(0, listDirectories.size());

    }


    @Test
    @Rollback
    public void testListSubDirForUsers() throws FileNotFoundException {
        BillingAccount act = setupAccountForPerson(getBasicUser());
        BillingAccount act2 = setupAccountForPerson(getBasicUser());
        List<AbstractFile> files = setupSomeFilesAndDirs(act, act2);
        TdarDir dir = null;
        for (AbstractFile file : files) {
            if (file.getName().equals(SUBDIR2)) {
                dir = (TdarDir) file;
            }
        }

        genericService.synchronize();
        List<AbstractFile> listFiles = pfs.listFiles(dir, act, null, getBasicUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(2, listFiles.size());

        listFiles = pfs.listFiles(dir, act, null, getAdminUser());
        for (AbstractFile af : listFiles) {
            logger.debug("{}", af);
        }
        assertEquals(2, listFiles.size());
    }
}
