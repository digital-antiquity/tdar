package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ResourceService;
import org.tdar.filestore.PersonalFilestoreFile;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;
import org.tdar.web.SessionData;

import com.opensymphony.xwork2.ActionSupport;

public abstract class AbstractControllerITCase extends AbstractIntegrationTestCase {

    private static final String PATH = TestConstants.TEST_ROOT_DIR;

    @Before
    public final void init() {
        init(getController());
    }

    protected abstract TdarActionSupport getController();

    private SessionData sessionData;

    @Autowired
    private ResourceService resourceService;

    public SessionData getSessionData() {
        if (sessionData == null) {
            this.sessionData = new SessionData();
        }
        return sessionData;
    }

    public void bookmarkResource(Resource r) {
        bookmarkResource(r, false);
    }

    public void removeBookmark(Resource r) {
        removeBookmark(r, false);
    }

    public void bookmarkResource(Resource r, boolean ajax) {
        BookmarkResourceAction bookmarkController = generateNewInitializedController(BookmarkResourceAction.class);
        logger.info("bookmarking " + r.getTitle() + " (" + r.getId() + ")");
        bookmarkController.setResourceId(r.getId());
        if (ajax) {
            bookmarkController.bookmarkResourceAjaxAction();
        } else {
            bookmarkController.bookmarkResourceAction();
        }
        r = resourceService.find(r.getId());
        assertNotNull(r);
        assertTrue(r.getBookmarks().size() > 0);
    }

    public void removeBookmark(Resource r, boolean ajax) {
        BookmarkResourceAction bookmarkController = generateNewInitializedController(BookmarkResourceAction.class);
        int size = r.getBookmarks().size();
        logger.info("removing bookmark " + r.getTitle() + " (" + r.getId() + ")");
        bookmarkController.setResourceId(r.getId());
        logger.info(r.getBookmarks());
        if (ajax) {
            bookmarkController.removeBookmarkAjaxAction();
        } else {
            bookmarkController.removeBookmarkAction();
        }
        r = resourceService.find(r.getId());
        assertNotNull(r);
        assertTrue(r.getBookmarks().isEmpty() || r.getBookmarks().size() == (size - 1));
    }

    /*
     * FIXME: figure out if we can load the resource within a new transaction.
     * otherwise since we're running within the same transaction Hibernate's first-level cache
     * will return the same resource that we saved initially as opposed to loading it again
     * within a new transaction for a new web request.
     */
    // @Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.SERIALIZABLE)
    protected void loadResourceFromId(AbstractResourceController<?> controller, Long id) {
        controller.setResourceId(id);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        if (controller instanceof AbstractInformationResourceController) {
            ((AbstractInformationResourceController<?>) controller).loadInformationResourceProperties();
        }
    }

    protected void init(TdarActionSupport controller) {
        init(controller, getUser());
    }

    protected void init(TdarActionSupport controller, Person user) {
        if (controller != null) {
            controller.setSessionData(getSessionData());

            if (getUser() != null) {
                AuthenticationToken token = AuthenticationToken.create(user);
                controller.getSessionData().setAuthenticationToken(token);
            } else {
                controller.getSessionData().setAuthenticationToken(new AuthenticationToken());
            }
        }
    }

    protected <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass) {
        T controller = generateNewController(controllerClass);
        if (controller instanceof TdarActionSupport) {
            init((TdarActionSupport) controller);
        }
        return controller;
    }

    Long uploadFile(String path, String name) {
        UploadController controller = generateNewInitializedController(UploadController.class);
        controller.setSessionData(sessionData);
        controller.grabTicket();
        Long ticketId = controller.getPersonalFilestoreTicket().getId();
        logger.info(ticketId);
        controller = generateNewInitializedController(UploadController.class);
        controller.setUploadFile(Arrays.asList(new File(path + "/" + name)));
        controller.setUploadFileFileName(Arrays.asList(name));
        controller.setTicketId(ticketId);
        controller.upload();
        return ticketId;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <C> C setupAndLoadResource(String filename, Class<C> cls) {
        AbstractInformationResourceController controller = null;
        Long ticketId = -1L;
        if (cls.equals(Ontology.class)) {
            controller = generateNewInitializedController(OntologyController.class);
        } else if (cls.equals(Dataset.class)) {
            controller = generateNewInitializedController(DatasetController.class);
        } else if (cls.equals(Document.class)) {
            controller = generateNewInitializedController(DocumentController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(Image.class)) {
            controller = generateNewInitializedController(ImageController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(CodingSheet.class)) {
            controller = generateNewInitializedController(CodingSheetController.class);
        }
        if (controller == null)
            return null;

        controller.prepare();
        controller.getResource().setTitle(filename);
        controller.getResource().setDescription("This resource was created as a result of a test: " + getClass());
        List<File> files = new ArrayList<File>();
        List<String> filenames = new ArrayList<String>();
        File file = new File(getTestFilePath(), filename);
        assertTrue("file not found:" + getTestFilePath() + "/" + filename, file.exists());
        files.add(file);
        filenames.add(filename);
        if (ticketId != -1) {
            controller.setTicketId(ticketId);
            controller.setFileProxies(Arrays.asList(new FileProxy(filename, VersionType.UPLOADED, false)));
        } else {
            controller.setUploadedFiles(files);
            controller.setUploadedFilesFileName(filenames);
        }
        controller.save();
        return (C) controller.getResource();
    }

    protected String getTestFilePath() {
        return PATH;
    }

    public Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync(List<File> uploadFiles) throws FileNotFoundException {
        UploadController uploadController = generateNewInitializedController(UploadController.class);
        assertEquals(TdarActionSupport.SUCCESS, uploadController.grabTicket());
        PersonalFilestoreTicket ticket = uploadController.getPersonalFilestoreTicket();
        Pair<PersonalFilestoreTicket, List<FileProxy>> toReturn = new Pair<PersonalFilestoreTicket, List<FileProxy>>(ticket, new ArrayList<FileProxy>());
        uploadController = generateNewInitializedController(UploadController.class);
        assertNull(uploadController.getTicketId());

        uploadController.setTicketId(ticket.getId());
        uploadController.setUploadFile(uploadFiles);
        for (File uploadedFile : uploadFiles) {
            uploadController.getUploadFileFileName().add(uploadedFile.getName());
            FileProxy fileProxy = new FileProxy();
            fileProxy.setFilename(uploadedFile.getName());
            fileProxy.setInputStream(new FileInputStream(uploadedFile));
            fileProxy.setAction(FileAction.ADD);
            toReturn.getSecond().add(fileProxy);
        }

        assertEquals(TdarActionSupport.SUCCESS, uploadController.upload());
        List<PersonalFilestoreFile> files = filestoreService.retrieveAllPersonalFilestoreFiles(uploadController.getTicketId());
        assertEquals(files.size(), uploadFiles.size());
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
}
