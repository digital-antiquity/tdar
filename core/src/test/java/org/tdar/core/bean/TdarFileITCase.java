package org.tdar.core.bean;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.FileProcessingDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.PersonalFilestoreService;

public class TdarFileITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private FileProcessingDao fileProcessingDao;

    @Test
    @Rollback
    public void testFileSearch() {
        BillingAccount act = new BillingAccount();
        act.setName("test");
        act.markUpdated(getAdminUser());
        genericService.saveOrUpdate(act);
        TdarFile file = new TdarFile("atest.pdf", getAdminUser(), act);
        TdarFile file2 = new TdarFile("test.pdf", getAdminUser(), act);
        TdarFile file3 = new TdarFile("cookies.pdf", getAdminUser(), act);
        genericService.saveOrUpdate(file);
        genericService.saveOrUpdate(file2);
        genericService.saveOrUpdate(file3);
        List<AbstractFile> list = fileProcessingDao.listFilesFor(null, null, "test", getAdminUser());
        for (AbstractFile f : list) {
            logger.debug("{} - {}", f.getName(), f);
        }
        assertFalse(list.contains(file3));
        assertTrue(list.contains(file));
        assertTrue(list.contains(file2));
        list = fileProcessingDao.listFilesFor(null, null, "ook", getAdminUser());
        for (AbstractFile f : list) {
            logger.debug("{} - {}", f.getName(), f);
        }
        assertTrue(list.contains(file3));
        assertFalse(list.contains(file));
        assertFalse(list.contains(file2));
    }

}
