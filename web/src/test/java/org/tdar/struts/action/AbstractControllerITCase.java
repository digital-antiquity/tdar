package org.tdar.struts.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import org.tdar.TestConstants;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.account.UserAccountController;
import org.tdar.struts.action.api.resource.BookmarkApiController;
import org.tdar.struts.action.codingSheet.CodingSheetController;
import org.tdar.struts.action.collection.CollectionController;
import org.tdar.struts.action.dataset.DatasetController;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.image.ImageController;
import org.tdar.struts.action.ontology.OntologyController;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.resource.AbstractSupportingInformationResourceController;
import org.tdar.struts.action.resource.BookmarkResourceController;
import org.tdar.struts.action.upload.UploadController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Action;

public abstract class AbstractControllerITCase extends AbstractIntegrationControllerTestCase {

    private static final String PATH = TestConstants.TEST_ROOT_DIR;
    public static final String TESTING_AUTH_INSTIUTION = "testing auth instiution";

    public static final String REASON = "because";

    public void bookmarkResource(Resource r, TdarUser user) throws Exception {
        bookmarkResource(r, false, user);
    }

    public void removeBookmark(Resource r, TdarUser user) throws Exception {
        removeBookmark(r, false, user);
    }

    public BillingAccount createAccount(TdarUser owner) {
        BillingAccount account = new BillingAccount("my account");
        account.setDescription("this is an account for : " + owner.getProperName());
        account.setOwner(owner);
        account.markUpdated(owner);
        genericService.saveOrUpdate(account);
        return account;
    }

    // public Account createAccountWithOneItem(Person person) {
    // return createA
    // }

    public Invoice createInvoice(TdarUser person, TransactionStatus status, BillingItem... items) {
        Invoice invoice = new Invoice();
        invoice.setItems(new ArrayList<BillingItem>());
        for (BillingItem item : items) {
            invoice.getItems().add(item);
        }
        invoice.setOwner(person);
        invoice.setTransactionStatus(status);
        genericService.saveOrUpdate(invoice);
        return invoice;
    }

    public void bookmarkResource(Resource r_, boolean ajax, TdarUser user) throws Exception {
        Resource r = r_;
        if (ajax) {
            BookmarkApiController bookmarkController = generateNewInitializedController(BookmarkApiController .class);
            logger.info("bookmarking " + r.getTitle() + " (" + r.getId() + ")");
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.bookmarkResourceAjaxAction();
        } else {
            BookmarkResourceController bookmarkController = generateNewInitializedController(BookmarkResourceController .class);
            logger.info("bookmarking " + r.getTitle() + " (" + r.getId() + ")");
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.bookmarkResourceAction();
        }
        r = resourceService.find(r.getId());
        assertNotNull(r);
        genericService.refresh(user);
        boolean seen = false;
        for (BookmarkedResource b : entityService.getBookmarkedResourcesForUser(user)) {
            if (ObjectUtils.equals(b.getResource(), r)) {
                seen = true;
            }
        }
        Assert.assertTrue("should have seen resource in bookmark list", seen);
    }

    @SuppressWarnings("deprecation")
    public void removeBookmark(Resource r, boolean ajax, TdarUser user_) throws Exception {
        TdarUser user = user_;
        boolean seen = false;
        for (BookmarkedResource b : entityService.getBookmarkedResourcesForUser(user)) {
            if (ObjectUtils.equals(b.getResource(), r)) {
                seen = true;
            }
        }

        Assert.assertTrue("should have seen resource in bookmark list", seen);
        logger.info("removing bookmark " + r.getTitle() + " (" + r.getId() + ")");
        if (ajax) {
            BookmarkApiController bookmarkController = generateNewInitializedController(BookmarkApiController .class);
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.removeBookmarkAjaxAction();
        } else {
            BookmarkResourceController bookmarkController = generateNewInitializedController(BookmarkResourceController.class);
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.removeBookmarkAction();
        }
        seen = false;
        genericService.synchronize();
        user = genericService.find(TdarUser.class, user.getId());
        for (BookmarkedResource b : entityService.getBookmarkedResourcesForUser(user)) {
            if (ObjectUtils.equals(b.getResource(), r)) {
                seen = true;
            }
        }
        Assert.assertFalse("should not see resource", seen);
    }

    public ResourceCollection generateResourceCollection(String name, String description, CollectionType type, boolean visible, List<AuthorizedUser> users,
            List<? extends Resource> resources, Long parentId)
            throws Exception {
        return generateResourceCollection(name, description, type, visible, users, getUser(), resources, parentId);
    }

    @SuppressWarnings("deprecation")
    public ResourceCollection generateResourceCollection(String name, String description, CollectionType type, boolean visible, List<AuthorizedUser> users,
            TdarUser owner, List<? extends Resource> resources, Long parentId) throws Exception {
        CollectionController controller = generateNewInitializedController(CollectionController.class, owner);
        controller.setServletRequest(getServletPostRequest());
        
        // controller.setSessionData(getSessionData());
        logger.info("{}", getUser());
        assertEquals(owner, controller.getAuthenticatedUser());
        ResourceCollection resourceCollection = controller.getResourceCollection();
        resourceCollection.setName(name);
        	
        resourceCollection.setType(type);
        controller.setAsync(false);
        resourceCollection.setHidden(!visible);
        resourceCollection.setDescription(description);
        if (resources != null) {
            controller.getToAdd().addAll(PersistableUtils.extractIds(resources));
        }
        
        if (parentId != null) {
        	controller.setParentId(parentId);
        }

        if (users != null) {
            controller.getAuthorizedUsers().clear();
            controller.getAuthorizedUsers().addAll(users);
        }
        resourceCollection.setSortBy(SortOption.RESOURCE_TYPE);
        controller.setServletRequest(getServletPostRequest());

        //A better replication of the struts lifecycle would include calls to prepare() and validate(), however, this
        // method currently generates resources that would ultimately generate ActionErrors, as well as Constraint
        // Violation errors. To fix this, we should make the following changes:

        //FIXME: remove actionError checks from controller.execute() methods (they are implicitly performed by struts and/or our test runner),
        //FIXME: improve generateResourceCollection() so that it constructs valid resources (vis a vis  validator.validate() and dao.enforceValidation())
        controller.prepare();
        controller.validate();

        String save = controller.save();
        assertTrue(save.equals(Action.SUCCESS));
        genericService.synchronize();
        Long id = resourceCollection.getId();
        genericService.evictFromCache(resourceCollection);
        resourceCollection = null;
        resourceCollection = genericService.find(ResourceCollection.class, id);
        logger.debug("parentId: {}", parentId);
        logger.debug("Resources: {}", resources);
        if (PersistableUtils.isNotNullOrTransient(parentId)) {
            assertEquals(parentId, resourceCollection.getParent().getId());
        }
        if (CollectionUtils.isNotEmpty(resources)) {
            assertThat(resourceCollection.getResources(), containsInAnyOrder(resources.toArray()));
        }
        return resourceCollection;
    }

    Long uploadFile(String path, String name) {
        String path_ = path;
        String name_ = name;
        if (name_.contains("src/test/") || name_.contains("target/test-resources")) {
            path_ = FilenameUtils.getPath(name_);
            name_ = FilenameUtils.getName(name_);
        }
        logger.info("name: {} path: {}", name_, path_);
        UploadController controller = generateNewInitializedController(UploadController.class);
        controller.setSessionData(getSessionData());
        controller.grabTicket();
        Long ticketId = controller.getPersonalFilestoreTicket().getId();
        logger.info("ticketId {}", ticketId);
        controller = generateNewInitializedController(UploadController.class);
        controller.setUploadFile(Arrays.asList(new File(path_ + name_)));
        controller.setUploadFileFileName(Arrays.asList(name_));
        controller.setTicketId(ticketId);
        String upload = controller.upload();
        assertEquals(Action.SUCCESS, upload);
        return ticketId;
    }

    public <C> C setupAndLoadResource(String filename, Class<C> cls) throws TdarActionException {
        return setupAndLoadResource(filename, cls, FileAccessRestriction.PUBLIC, -1L);
    }

    public <C> C setupAndLoadResource(String filename, Class<C> cls, FileAccessRestriction permis) throws TdarActionException {
        return setupAndLoadResource(filename, cls, permis, -1L);
    }

    public <C> C setupAndLoadResource(String filename, Class<C> cls, Long id) throws TdarActionException {
        return setupAndLoadResource(filename, cls, FileAccessRestriction.PUBLIC, id);
    }

    @SuppressWarnings("unchecked")
    public <C> C replaceFile(String uploadFile, String replaceFile, Class<C> cls, Long id) throws TdarActionException {
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
        // FileProxy newProxy = new FileProxy(uploadFile, VersionType.UPLOADED, FileAccessRestriction.PUBLIC);
        // newProxy.setAction(FileAction.REPLACE);
        for (FileProxy proxy : controller.getFileProxies()) {
            if (proxy.getFilename().equals(replaceFile)) {
                proxy.setFilename(uploadFile);
                proxy.setAction(FileAction.REPLACE);
                logger.debug("replaceFile: Replacing {} with {}", replaceFile, uploadFile);
            }
        }
        // controller.getFileProxies().add(newProxy);
        controller.setTicketId(ticketId);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        return (C) controller.getResource();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <C> C setupAndLoadResource(String filename, Class<C> cls, FileAccessRestriction permis, Long id) throws TdarActionException {

        AbstractInformationResourceController controller = null;
        Long ticketId = -1L;
        if (cls.equals(Ontology.class)) {
            controller = generateNewInitializedController(OntologyController.class);
        } else if (cls.equals(Dataset.class)) {
            controller = generateNewInitializedController(DatasetController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(Document.class)) {
            controller = generateNewInitializedController(DocumentController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(Image.class)) {
            controller = generateNewInitializedController(ImageController.class);
            ticketId = uploadFile(getTestFilePath(), filename);
        } else if (cls.equals(CodingSheet.class)) {
            controller = generateNewInitializedController(CodingSheetController.class);
        }
        if (controller == null) {
            return null;
        }

        if (PersistableUtils.isNotNullOrTransient(id)) {
            controller.setId(id);
        }
        controller.prepare();
        final Resource resource = controller.getResource();
        resource.setTitle(filename);
        resource.setDescription("This resource was created as a result of a test: " + getClass());
        if ((resource instanceof InformationResource) && TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Creator copyrightHolder = genericService.find(Person.class, 1L);
            ((InformationResource) resource).setCopyrightHolder(copyrightHolder);
        }

        List<File> files = new ArrayList<File>();
        List<String> filenames = new ArrayList<String>();
        if (ticketId != -1) {
            controller.setTicketId(ticketId);
            controller.setFileProxies(Arrays.asList(new FileProxy(FilenameUtils.getName(filename), VersionType.UPLOADED, permis)));
        } else {
            File file = new File(getTestFilePath(), filename);
            assertTrue("file not found:" + getTestFilePath() + "/" + filename, file.exists());
            if (FilenameUtils.getExtension(filename).equals("txt") && (controller instanceof AbstractSupportingInformationResourceController<?>)) {
                AbstractSupportingInformationResourceController<?> asc = (AbstractSupportingInformationResourceController<?>) controller;
                asc.setFileInputMethod(AbstractInformationResourceController.FILE_INPUT_METHOD);
                try {
                    asc.setFileTextInput(FileUtils.readFileToString(file));
                } catch (Exception e) {
                    Assert.fail(e.getMessage());
                }
            } else {
                files.add(file);
                filenames.add(filename);
                controller.setUploadedFiles(files);
                controller.setUploadedFilesFileName(filenames);
            }
        }
        try {
            controller.setServletRequest(getServletPostRequest());
            controller.save();
        } catch (TdarActionException exception) {
            // what now?
            exception.printStackTrace();
        }
        return (C) controller.getResource();
    }

    protected String getTestFilePath() {
        return PATH;
    }


    public FileProxy uploadFileAsync(File file, PersonalFilestoreTicket ticket) throws FileNotFoundException {
        return uploadFilesAsync(Arrays.asList(file), ticket).getSecond().get(0);
    }

    public Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync(List<File> uploadFiles) throws FileNotFoundException {
        return uploadFilesAsync(uploadFiles, grabTicket());
    }

    public Pair<PersonalFilestoreTicket, List<FileProxy>> uploadFilesAsync(List<File> uploadFiles, PersonalFilestoreTicket ticket) throws FileNotFoundException {
        UploadController uploadController;
        Pair<PersonalFilestoreTicket, List<FileProxy>> toReturn = new Pair<PersonalFilestoreTicket, List<FileProxy>>(ticket, new ArrayList<FileProxy>());
        uploadController = generateNewInitializedController(UploadController.class);
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
        List<PersonalFilestoreFile> files = filestoreService.retrieveAllPersonalFilestoreFiles(uploadController.getTicketId());
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

    protected PersonalFilestoreTicket grabTicket() {
        UploadController uploadController = generateNewInitializedController(UploadController.class);
        assertEquals(Action.SUCCESS, uploadController.grabTicket());
        return uploadController.getPersonalFilestoreTicket();
    }

    @Override
    protected TdarUser getUser() {
        return getUser(getUserId());
    }

    public String setupValidUserInController(UserAccountController controller) {
        return setupValidUserInController(controller, "testuser@example.com");
    }

    public String setupValidUserInController(UserAccountController controller, String email) {
        TdarUser p = new TdarUser();
        p.setEmail(email);
        p.setUsername(email);
        p.setFirstName("Testing auth");
        p.setLastName("User");
        p.setPhone("212 000 0000");
        controller.getRegistration().setPerson(p);
        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.getRegistration().setContributorReason(REASON);
        p.setRpaNumber("214");

        return setupValidUserInController(controller, p);
    }

    public String setupValidUserInController(UserAccountController controller, TdarUser p) {
        return setupValidUserInController(controller, p, "password");
    }

    public String setupValidUserInController(UserAccountController controller, TdarUser p, String password) {
        // cleanup crowd if we need to...
        authenticationService.getAuthenticationProvider().deleteUser(p);
        controller.getRegistration().setRequestingContributorAccess(true);
        controller.getRegistration().setInstitutionName(TESTING_AUTH_INSTIUTION);
        controller.getRegistration().setPassword(password);
        controller.getRegistration().setConfirmPassword(password);
        controller.getRegistration().setConfirmEmail(p.getEmail());
        controller.getRegistration().setPerson(p);
        controller.getRegistration().setAcceptTermsOfUse(true);
        controller.setServletRequest(getServletPostRequest());
        controller.setServletResponse(getServletResponse());
        controller.validate();
        String execute = null;
        // technically this is more appropriate -- only call create if validate passes
        if (CollectionUtils.isEmpty(controller.getActionErrors())) {
            execute = controller.create();
        } else {
            logger.error("errors: {} ", controller.getActionErrors());
        }

        return execute;
    }

    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE, LookupSource.COLLECTION);
    }
}
