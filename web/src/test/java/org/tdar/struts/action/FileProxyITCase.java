/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static com.opensymphony.xwork2.Action.SUCCESS;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.tdar.TestConstants.TEST_DOCUMENT_DIR;
import static org.tdar.TestConstants.TEST_IMAGE_DIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.utils.Pair;

/**
 * @author Adam Brin
 * 
 */
public class FileProxyITCase extends AbstractResourceControllerITCase {

    /**
     * Simulate situation where system encounters more files than file proxies.
     * @throws Exception
     */
    @Test
    @Rollback
    public void testDegenerateFileProxy() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class, getBasicUser());
        controller.prepare();
        controller.add();
        Document document = getNewDocument(controller);
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(TEST_DOCUMENT_DIR + "a2-15.pdf"));
        fileList.add(new File(TEST_DOCUMENT_DIR + "a2-17.pdf"));
        fileList.add(new File(TEST_DOCUMENT_DIR + "schoenwetter1964b.pdf"));
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        uploadFilesAsync.getSecond().remove(0);
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(0).setRestriction(FileAccessRestriction.CONFIDENTIAL);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(SUCCESS, save);

        document = genericService.find(Document.class, document.getId());
        assertThat(document.getInformationResourceFiles(), hasSize(2));

        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info("{}", irFile);
            // assertEquals("only a2-17.pdf should be confidential", irFile.isConfidential(),
            // irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf"));
            if (irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf")) {
                assertTrue(irFile.isConfidential());
            } else {
                assertFalse(irFile.isConfidential());
            }
        }
    }

    @Test
    @Rollback
    public void testSameFileName() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class, getBasicUser());
        controller.prepare();
        controller.add();
        Document document = getNewDocument(controller);
        List<File> fileList = new ArrayList<File>();
        for (String subdir : Arrays.asList("t1", "t2", "t3")) {
            File file = new File(TEST_DOCUMENT_DIR + subdir, "test.pdf");
            assertTrue(file.exists());
            fileList.add(file);
        }
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        uploadFilesAsync.getSecond().remove(0);
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(0).setRestriction(FileAccessRestriction.CONFIDENTIAL);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(SUCCESS, save);
        document = genericService.find(Document.class, document.getId());
        assertThat(document.getInformationResourceFiles(), hasSize(2));
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info("{}", irFile);
        }
    }

    @Test
    @Rollback
    public void testReplace() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = getNewDocument(controller);
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(TEST_DOCUMENT_DIR + "a2-15.pdf"));
        fileList.add(new File(TEST_DOCUMENT_DIR + "a2-17.pdf"));
        fileList.add(new File(TEST_DOCUMENT_DIR + "schoenwetter1964b.pdf"));
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(1).setRestriction(FileAccessRestriction.CONFIDENTIAL);
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        assertEquals(fileList.size(), document.getInformationResourceFiles().size());
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info("{}", irFile);
            assertEquals("only a2-17.pdf should be confidential",
                    irFile.isConfidential(),
                    irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf"));
            // if (irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf")) {
            // assertTrue(irFile.isConfidential());
            // } else {
            // assertFalse(irFile.isConfidential());
            // }
        }
        controller = generateNewInitializedController(DocumentController.class);
        controller.setId(document.getId());
        controller.prepare();
        controller.edit();
        controller.getFileProxies().get(0).setAction(FileAction.DELETE);
        String deletedFilename = controller.getFileProxies().get(0).getFilename();
        // replace the confidential file
        FileProxy replaceConfidentialFileProxy = null;
        for (FileProxy proxy : controller.getFileProxies()) {
            if (proxy.getRestriction() == FileAccessRestriction.CONFIDENTIAL) {
                replaceConfidentialFileProxy = proxy;
            }
        }
        replaceConfidentialFileProxy.setAction(FileAction.REPLACE);
        replaceConfidentialFileProxy.setRestriction(FileAccessRestriction.PUBLIC);
        replaceConfidentialFileProxy.setFilename("pia-09-lame-1980.pdf");
        Pair<PersonalFilestoreTicket, List<FileProxy>> newProxyList = uploadFilesAsync(Arrays.asList(new File(TEST_DOCUMENT_DIR
                + "pia-09-lame-1980.pdf")));
        controller.setTicketId(newProxyList.getFirst().getId());
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            if (irFile.getLatestUploadedVersion().getFilename().equals(deletedFilename)) {
                assertTrue(irFile + " should have been flagged as deleted", irFile.isDeleted());
            }
            logger.info("{}", irFile);
            assertFalse("there should be no confidential files", irFile.isConfidential());
        }
    }

    @Test
    @Rollback
    public void testReplaceFileWithSameName() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = getNewDocument(controller);
        List<File> fileList = new ArrayList<File>();
        String a2pdf = "a2-15.pdf";
        fileList.add(new File(TEST_DOCUMENT_DIR + a2pdf));
        fileList.add(new File(TEST_DOCUMENT_DIR + "a2-17.pdf"));
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(1).setRestriction(FileAccessRestriction.CONFIDENTIAL);
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        assertEquals(fileList.size(), document.getInformationResourceFiles().size());
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info("{}", irFile);
            assertEquals("only a2-17.pdf should be confidential",
                    irFile.isConfidential(),
                    irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf"));
            // if (irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf")) {
            // assertTrue(irFile.isConfidential());
            // } else {
            // assertFalse(irFile.isConfidential());
            // }
        }
//        SessionProxy.getInstance().flushAll();
        controller = generateNewInitializedController(DocumentController.class);
        controller.setId(document.getId());
        controller.prepare();
        controller.edit();

        FileProxy replaceConfidentialFileProxy = null;
        for (FileProxy proxy : controller.getFileProxies()) {
            if (proxy.getFilename().equals(a2pdf)) {
                replaceConfidentialFileProxy = proxy;
            }
        }
        replaceConfidentialFileProxy.setAction(FileAction.REPLACE);
        replaceConfidentialFileProxy.setRestriction(FileAccessRestriction.PUBLIC);
        Pair<PersonalFilestoreTicket, List<FileProxy>> newProxyList = uploadFilesAsync(Arrays.asList(new File(TEST_DOCUMENT_DIR, a2pdf)));
        controller.setTicketId(newProxyList.getFirst().getId());
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        assertEquals(2, document.getInformationResourceFiles().size());
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info("{}", irFile);
            if (irFile.getLatestUploadedVersion().getFilename().equals(a2pdf)) {
                assertEquals(2, irFile.getLatestVersion().intValue());
            } else {
                assertTrue(irFile.isConfidential());
            }
        }
    }

    @Test
    public void testImageWithUppercaseFilename() throws FileNotFoundException, TdarActionException {
        PersonalFilestoreTicket ticket = grabTicket();
        FileProxy fileProxy = uploadFileAsync(new File(TEST_IMAGE_DIR, "GREYBOX.PNG"), ticket);
        ImageController controller = generateNewInitializedController(ImageController.class);
        controller.setServletRequest(getServletPostRequest());
        controller.prepare();

        Image image = controller.getResource();
        image.setTitle("test title");
        image.setDescription("descr");
        image.setDate(1234);
        image.setProject(Project.NULL);
        image.getFileProxies().clear();
        image.getFileProxies().add(fileProxy);
        controller.setTicketId(ticket.getId());
        controller.validate();
        String resultName = controller.save();
        assertThat(resultName, is( SUCCESS));
        assertThat(controller.getActionErrors(), is( empty()));
    }

    private Document getNewDocument(DocumentController controller) {
        Document document = controller.getDocument();
        document.setTitle("test title");
        document.setDescription("descr");
        document.setDate(1234);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Person copyrightHolder = genericService.find(Person.class, 1L);
            document.setCopyrightHolder(copyrightHolder);
        }
        return document;
    }
}
