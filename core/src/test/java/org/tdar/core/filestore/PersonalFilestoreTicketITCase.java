package org.tdar.core.filestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.workflow.FileProcessingService;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;

public class PersonalFilestoreTicketITCase extends AbstractIntegrationTestCase {

    @Autowired
    PersonalFilestoreService filestoreService;

    @Autowired
    FileProcessingService fileProcessingService;
    
    @Autowired
    EntityService entityService;

    private static final String PATH = TestConstants.TEST_ROOT_DIR;

    
    @Test
    @Rollback
    public void testFileProcessing() throws Throwable {
        filestoreService.sweepFiles();
    }
    
    @Test
    @Rollback
    public void testNewFileGroup() {
        // make sure hibernate is serving up a new id
        PersonalFilestoreTicket fileGroup = new PersonalFilestoreTicket();
        fileGroup.setSubmitter(getUser());
        fileGroup.setPersonalFileType(PersonalFileType.UPLOAD);
        Long originalId = fileGroup.getId();
        genericService.save(fileGroup);
        Long newId = fileGroup.getId();
        logger.info(String.format("oldid:%s, newid:%s", originalId, newId));
        Assert.assertNotSame("expecting id to be assigned", newId, originalId);
    }

    @Test
    @Rollback
    public void testNewFileGroupFromService() {
        PersonalFilestoreTicket fileGroup = new PersonalFilestoreTicket();

        TdarUser person = getUser();
        fileGroup.setPersonalFileType(PersonalFileType.UPLOAD);
        fileGroup.setSubmitter(person);
        genericService.save(fileGroup);

        Assert.assertNotNull("group id should not be null", fileGroup.getId());
        fileGroup = filestoreService.createPersonalFilestoreTicket(person);
    }

    @Test
    @Rollback
    public void testStore() throws IOException {

        PersonalFilestoreTicket ticket = filestoreService.createPersonalFilestoreTicket(getUser());
        PersonalFilestore filestore = filestoreService.getPersonalFilestore(getUser());
        File storedFile = filestore.store(ticket, new File(PATH + "images/handbook_of_archaeology.jpg"), "handbook_of_archaeology.jpg").getFile();
        Assert.assertTrue("filestore exists", storedFile.exists());
        storedFile = filestore.store(ticket, new File(PATH + "documents/schoenwetter1963a with space.pdf"), "schoenwetter1963a with space.pdf").getFile();
        logger.info("{}", storedFile);
        Assert.assertTrue("filestore exists", storedFile.exists());

    }

    // save some under a single ticket and then retrieve them to make sure they're still there
    private PersonalFilestoreTicket saveFiles() throws Exception {
        List<File> filesToAdd = new ArrayList<File>();
        filesToAdd.add(new File(PATH + "images/handbook_of_archaeology.jpg"));
        filesToAdd.add(new File(PATH + "data_integration_tests/evmpp-fauna.xls"));
        filesToAdd.add(new File(PATH + "xml/documentImport.xml"));

        List<String> names = new ArrayList<String>();
        names.add("handbook_of_archaeology.jpg");
        names.add("evmpp-fauna.xls");
        names.add("documentImport.xml");

        PersonalFilestoreTicket ticket = filestoreService.createPersonalFilestoreTicket(getUser());
        PersonalFilestore filestore = filestoreService.getPersonalFilestore(getUser());
        filestore.store(ticket, filesToAdd, names);

        // confirm that we have actually stored the items we wanted to store
        List<PersonalFilestoreFile> filestoreFiles = filestore.retrieveAll(ticket);
        Map<String, File> storedFileMap = new HashMap<String, File>();
        for (PersonalFilestoreFile pff : filestoreFiles) {
            logger.debug("adding to map:" + pff.getFile().getAbsolutePath());
            logger.debug("adding to map:" + pff.getFile().getCanonicalPath());
            storedFileMap.put(pff.getFile().getName(), pff.getFile());
        }
        for (File file : filesToAdd) {
            Assert.assertTrue("expecting stored file:" + file.getName(), storedFileMap.containsKey(file.getName()));
            File storedFile = storedFileMap.get(file.getName());
            String msg = String.format("sizes should be equal( Original file:%s,  destination file:%s)", file.getAbsolutePath(), storedFile.getAbsolutePath());
            logger.debug(msg);
            Assert.assertEquals(msg, file.length(), storedFile.length());
        }
        return ticket;
    }

    @Test
    @Rollback
    public void testSaveFiles() throws Exception {
        saveFiles();
    }

    @Test
    @Rollback
    public void testPurge() throws Exception {
        PersonalFilestoreTicket ticket = saveFiles();
        PersonalFilestore filestore = filestoreService.getPersonalFilestore(getUser());
        // get the parent path of the stored files, confirm that it doesn't exist
        File parent = filestore.retrieveAll(ticket).iterator().next().getFile().getParentFile();
        filestore.purge(ticket);
        Assert.assertFalse("bag should be gone after purge", parent.exists());
    }

}
