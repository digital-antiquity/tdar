package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.struts.action.codingSheet.CodingSheetController;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts.action.ontology.OntologyController;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.upload.CreateFilestoreTicketAction;
import org.tdar.struts.action.upload.UploadAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public interface TestFileUploadHelper {

    final Logger logger_ = LoggerFactory.getLogger(TestFileUploadHelper.class);

    default Long uploadFile(String path, String name) {
        String path_ = path;
        String name_ = name;
        if (name_.contains("src/test/") || name_.contains("target/test-resources")) {
            path_ = FilenameUtils.getPath(name_);
            name_ = FilenameUtils.getName(name_);
        }
        logger_.info("name: {} path: {}", name_, path_);
        UploadAction controller = generateNewInitializedController(UploadAction.class);
        controller.setSessionData(getSessionData());
//        controller.grabTicket();
        controller = generateNewInitializedController(UploadAction.class);
        controller.setUploadFile(Arrays.asList(new File(path_ + name_)));
        controller.setUploadFileFileName(Arrays.asList(name_));
//        controller.setTicketId(ticketId);
        String upload = controller.upload();
        Long ticketId = controller.getPersonalFilestoreTicket().getId();
        logger_.info("ticketId {}", ticketId);
        assertEquals(Action.SUCCESS, upload);
        return ticketId;
    }

    @SuppressWarnings("unchecked")
    default <C> C replaceFile(String uploadFile, String replaceFile, Class<C> cls, Long id) throws TdarActionException {
        AbstractInformationResourceController<?> controller = null;
        Long ticketId = -1L;
        if (cls.equals(Ontology.class)) {
            controller = generateNewInitializedController(OntologyController.class);
        } else if (cls.equals(Dataset.class)) {
            controller = generateNewInitializedController(DatasetController.class);
            ticketId = uploadFile(getTestFilePath(), uploadFile);
        } else if (cls.equals(Document.class)) {
            controller = generateNewInitializedController(DocumentController.class);
            ticketId = uploadFile(getTestFilePath(), uploadFile);
        } else if (cls.equals(Image.class)) {
            controller = generateNewInitializedController(ImageController.class);
            ticketId = uploadFile(getTestFilePath(), uploadFile);
        } else if (cls.equals(CodingSheet.class)) {
            controller = generateNewInitializedController(CodingSheetController.class);
        }
        controller.setId(id);
        controller.prepare();
        controller.edit();
        for (FileProxy proxy : controller.getFileProxies()) {
            if (proxy.getFilename().equals(replaceFile)) {
                proxy.setFilename(uploadFile);
                proxy.setAction(FileAction.REPLACE);
                logger_.debug("replaceFile: Replacing {} with {}", replaceFile, uploadFile);
            }
        }
        // controller.getFileProxies().add(newProxy);
        controller.setTicketId(ticketId);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        return (C) controller.getResource();
    }

    default FileProxy uploadFileAsync(File file, PersonalFilestoreTicket ticket) throws FileNotFoundException {
        return uploadFilesAsync(Arrays.asList(file), ticket).getSecond().get(0);
    }

    default Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync(List<File> uploadFiles) throws FileNotFoundException {
        return uploadFilesAsync(uploadFiles, grabTicket());
    }

    default Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync(List<File> uploadFiles, PersonalFilestoreTicket ticket)
            throws FileNotFoundException {
        UploadAction uploadController;
        Pair<PersonalFilestoreTicket, List<FileProxy>> toReturn = new Pair<PersonalFilestoreTicket, List<FileProxy>>(ticket, new ArrayList<FileProxy>());
        uploadController = generateNewInitializedController(UploadAction.class);
        assertNull(uploadController.getTicketId());

        uploadController.setTicketId(ticket.getId());
        uploadController.setUploadFile(uploadFiles);
        for (File uploadedFile : uploadFiles) {
            uploadController.getUploadFileFileName().add(uploadedFile.getName());
            FileProxy fileProxy = new FileProxy();
            fileProxy.setFilename(uploadedFile.getName());
            fileProxy.setFile(uploadedFile);
            fileProxy.setAction(FileAction.ADD);
            toReturn.getSecond().add(fileProxy);
        }

        assertEquals(Action.SUCCESS, uploadController.upload());
        List<PersonalFilestoreFile> files = getFilestoreService().retrieveAllPersonalFilestoreFiles(uploadController.getTicketId());
        assertEquals("file count retrieved from personal filestore", uploadFiles.size(), files.size());
        // XXX: potentially assert that md5s and/or filenames are same across both file lists
        for (PersonalFilestoreFile personalFilestoreFile : files) {
            String filename = personalFilestoreFile.getFile().getName();
            boolean equal = false;
            for (File uploadFile : uploadFiles) {
                if (filename.equals(uploadFile.getName())) {
                    equal = true;
                }
            }
            assertTrue(filename + " not found in uploadFiles: " + uploadFiles, equal);
        }
        return toReturn;
    }

    PersonalFilestoreService getFilestoreService();

    default PersonalFilestoreTicket grabTicket() {
        CreateFilestoreTicketAction uploadController = generateNewInitializedController(CreateFilestoreTicketAction.class);
        assertEquals(Action.SUCCESS, uploadController.grabTicket());
        return uploadController.getPersonalFilestoreTicket();
    }

    String getTestFilePath();

    SessionData getSessionData();

    HttpServletRequest getServletRequest();

    HttpServletRequest getServletPostRequest();

    HttpServletResponse getServletResponse();

    <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass, TdarUser user);

    <T extends ActionSupport> T generateNewInitializedController(Class<T> class1);

    EntityService getEntityService();

    GenericService getGenericService();

}
