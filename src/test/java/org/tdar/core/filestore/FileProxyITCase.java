/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.filestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.DocumentController;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;

import static org.junit.Assert.*;

/**
 * @author Adam Brin
 * 
 */
public class FileProxyITCase extends AbstractControllerITCase {
    
    @Test
    @Rollback
    public void testDegenerateFileProxy() throws Exception {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test title");
        document.setDescription("descr");
        document.setDateCreated("1234");
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-15.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-17.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "schoenwetter1964b.pdf"));
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        uploadFilesAsync.getSecond().remove(0);
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(0).setConfidential(true);
        controller.save();
        assertEquals(fileList.size(), document.getInformationResourceFiles().size());
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info(irFile);
//            assertEquals("only a2-17.pdf should be confidential", irFile.isConfidential(), irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf"));
            if (irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf")) {
                assertTrue(irFile.isConfidential());
            } else {
                assertFalse(irFile.isConfidential());
            }
        }
    }

    @Test
    @Rollback
    public void testReplace() throws FileNotFoundException {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        Document document = controller.getDocument();
        document.setTitle("test title");
        document.setDescription("descr");
        document.setDateCreated("1234");
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-15.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "a2-17.pdf"));
        fileList.add(new File(TestConstants.TEST_DOCUMENT_DIR + "schoenwetter1964b.pdf"));
        Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync = uploadFilesAsync(fileList);
        controller.setTicketId(uploadFilesAsync.getFirst().getId());
        controller.setFileProxies(uploadFilesAsync.getSecond());
        controller.getFileProxies().get(1).setConfidential(true);
        controller.save();

        assertEquals(fileList.size(), document.getInformationResourceFiles().size());
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            logger.info(irFile);
//            assertEquals("only a2-17.pdf should be confidential", irFile.isConfidential(), irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf"));
            if (irFile.getLatestUploadedVersion().getFilename().equals("a2-17.pdf")) {
                assertTrue(irFile.isConfidential());
            } else {
                assertFalse(irFile.isConfidential());
            }
        }
        controller = generateNewInitializedController(DocumentController.class);
        controller.setResourceId(document.getId());
        loadResourceFromId(controller, document.getId());
        controller.getFileProxies().get(0).setAction(FileAction.DELETE);
        String deletedFilename = controller.getFileProxies().get(0).getFilename();
        // replace the confidential file 
        FileProxy replaceConfidentialFileProxy = null;
        for (FileProxy proxy: controller.getFileProxies()) {
            if (proxy.isConfidential()) {
                replaceConfidentialFileProxy = proxy;
            }
        }
        replaceConfidentialFileProxy.setAction(FileAction.REPLACE);
        replaceConfidentialFileProxy.setConfidential(false);
        replaceConfidentialFileProxy.setFilename("pia-09-lame-1980.pdf");
        Pair<PersonalFilestoreTicket, List<FileProxy>> newProxyList = uploadFilesAsync(Arrays.asList(new File(TestConstants.TEST_DOCUMENT_DIR
                + "pia-09-lame-1980.pdf")));
        controller.setTicketId(newProxyList.getFirst().getId());
        controller.getFileProxies().addAll(newProxyList.getSecond());
        controller.save();

        boolean seenDeleted = false;
        for (InformationResourceFile irFile : document.getInformationResourceFiles()) {
            if (irFile.getLatestUploadedVersion().getFilename().equals(deletedFilename) && irFile.getStatus() != FileStatus.DELETED) {
                seenDeleted = true;
            }
            assertFalse(irFile.isConfidential());
            logger.info(irFile);
        }
        assertFalse(seenDeleted);
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
