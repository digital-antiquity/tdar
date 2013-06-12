/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.filestore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.struts.action.resource.DocumentController;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

/**
 * @author Adam Brin
 * 
 */
public class FileProxyITCase extends AbstractResourceControllerITCase {

    @Test
    @Rollback
    public void testDegenerateFileProxy() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class, getBasicUser());
        controller.prepare();
        controller.add();
        Document document = getNewDocument(controller);
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-15.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-17.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "schoenwetter1964b.pdf"));
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        uploadFilesAsync.getSecond().remove(0);
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(0).setRestriction(FileAccessRestriction.CONFIDENTIAL);
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(TdarActionSupport.SUCCESS, save);
        assertEquals(fileList.size(), document.getInformationResourceFiles().size());
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
            File file = new File(TestConstants.TEST_DOCUMENT_DIR + subdir, "test.pdf");
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
        assertEquals(TdarActionSupport.SUCCESS, save);
        assertEquals(fileList.size(), document.getInformationResourceFiles().size());
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
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-15.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-17.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "schoenwetter1964b.pdf"));
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
        loadResourceFromId(controller, document.getId());
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
        Pair<PersonalFilestoreTicket, List<FileProxy>> newProxyList = uploadFilesAsync(Arrays.asList(new File(TestConstants.TEST_DOCUMENT_DIR
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
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + a2pdf));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-17.pdf"));
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
        loadResourceFromId(controller, document.getId());

        FileProxy replaceConfidentialFileProxy = null;
        for (FileProxy proxy : controller.getFileProxies()) {
            if (proxy.getFilename().equals(a2pdf)) {
                replaceConfidentialFileProxy = proxy;
            }
        }
        replaceConfidentialFileProxy.setAction(FileAction.REPLACE);
        replaceConfidentialFileProxy.setRestriction(FileAccessRestriction.PUBLIC);
        Pair<PersonalFilestoreTicket, List<FileProxy>> newProxyList = uploadFilesAsync(Arrays.asList(new File(TestConstants.TEST_DOCUMENT_DIR
                + a2pdf)));
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
            // if (irFile.getLatestUploadedVersion().getFilename().equals("a2-15.pdf")) {
            // assertTrue(irFile + " should have been flagged as deleted", irFile.isDeleted());
            // }
            // logger.info("{}", irFile);

        }
    }

    private Document getNewDocument(DocumentController controller) {
        Document document = controller.getDocument();
        document.setTitle("test title");
        document.setDescription("descr");
        document.setDate(1234);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Creator copyrightHolder = genericService.find(Person.class, 1L);
            document.setCopyrightHolder(copyrightHolder);
        }
        return document;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
    }

}
