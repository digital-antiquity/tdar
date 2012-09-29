package org.tdar.core.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.tdar.TestConstants;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.FilestoreService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.Filestore;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FileProxy;
import org.tdar.web.SessionData;

import com.opensymphony.xwork2.ActionSupport;

@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
public abstract class AbstractIntegrationTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    protected HttpServletRequest defaultHttpServletRequest = new MockHttpServletRequest();

    protected HttpServletRequest httpServletRequest = defaultHttpServletRequest;
    protected HttpServletRequest httpServletPostRequest = new MockHttpServletRequest("POST", "/");
    protected HttpServletResponse httpServletResponse = new MockHttpServletResponse();

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected DatasetService datasetService;
    @Autowired
    protected GenericService genericService;
    @Autowired
    protected ResourceService resourceService;
    @Autowired
    protected EntityService entityService;
    @Autowired
    protected InformationResourceService informationResourceService;
    @Autowired
    protected SearchIndexService searchIndexService;
    @Autowired
    protected SearchService searchService;
    @Autowired
    protected BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    protected FilestoreService filestoreService;

    private List<ActionSupport> controllers = new ArrayList<ActionSupport>();
    private boolean ignoreActionErrors = false;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private SessionData sessionData;

    @Before
    public final void initControllerErrorChecking() {
        getControllers().clear();
        setIgnoreActionErrors(false);
    }

    @After
    public void checkForActionErrors() {
        int errorCount = 0;
        if (!isIgnoreActionErrors()) {
            for (ActionSupport controller : getControllers()) {
                if (controller != null && !controller.getActionErrors().isEmpty()) {
                    logger.error("{}", controller.getActionErrors());
                    errorCount += controller.getActionErrors().size();
                }
            }
        }

        if (errorCount > 0) {
            Assert.fail("You've got errors!");
        }

        logger.trace("is this thing on!??");
    }

    public Person createAndSaveNewPerson() {
        return createAndSaveNewPerson(null, "");
    }

    public Person createAndSaveNewPerson(String email, String suffix) {
        if (StringUtils.isBlank(email)) {
            email = TestConstants.DEFAULT_EMAIL;
        }
        Person testPerson = new Person();
        testPerson.setEmail(email);
        testPerson.setFirstName(TestConstants.DEFAULT_FIRST_NAME + suffix);
        testPerson.setLastName(TestConstants.DEFAULT_LAST_NAME + suffix);
        Institution institution = entityService.findInstitutionByName(TestConstants.INSTITUTION_NAME);
        if (institution == null) {
            institution = new Institution();
            institution.setName(TestConstants.INSTITUTION_NAME);
            genericService.saveOrUpdate(institution);
        }
        testPerson.setInstitution(institution);
        testPerson.setRegistered(true);
        testPerson.setContributor(false);
        genericService.save(testPerson);
        return testPerson;
    }

    @Deprecated
    /*
     * deprecated, use generateInformationResourceWithFileAndUser() or generateInformationResourceWithUser() instead
     */
    public InformationResource generateInformationResourceWithFile() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, true);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        assertTrue("testing " + TestConstants.TEST_DOCUMENT_NAME + " exists", file.exists());
        ir = (Document) addFileToResource(ir, file);
        return ir;
    }

    public <R extends InformationResource> InformationResourceFileVersion generateAndStoreVersion(Class<R> type, String name, File f, Filestore filestore)
            throws InstantiationException,
            IllegalAccessException, IOException {
        InformationResource ir = createAndSaveNewInformationResource(type, false);
        InformationResourceFile irFile = new InformationResourceFile();
        irFile.setInformationResource(ir);
        irFile.setLatestVersion(1);
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setVersion(1);
        version.setFilename(name);
        version.setExtension(FilenameUtils.getExtension(name));
        version.setInformationResourceFile(irFile);
        version.setDateCreated(new Date());
        version.setFileVersionType(VersionType.UPLOADED);
        irFile.getInformationResourceFileVersions().add(version);
        genericService.save(irFile);
        genericService.save(version);
        filestore.store(f, version);
        return version;
    }

    public InformationResource generateInformationResourceWithFileAndUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, false);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        assertTrue("testing " + TestConstants.TEST_DOCUMENT_NAME + " exists", file.exists());
        ir = (Document) addFileToResource(ir, file);
        return ir;
    }

    public InformationResource generateInformationResourceWithUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, false);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        return ir;
    }

    public InformationResource addFileToResource(InformationResource ir, File file) {
        try {
            FileProxy proxy = new FileProxy(file.getName(), new FileInputStream(file), VersionType.UPLOADED, FileAction.ADD);
            informationResourceService.processFileProxy(ir, proxy);
            // informationResourceService.addOrReplaceInformationResourceFile(ir, new FileInputStream(file), file.getName(), FileAction.ADD,
            // VersionType.UPLOADED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ir = genericService.find(Document.class, ir.getId());
        for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
            assertTrue(irf.getId() != null);
            for (InformationResourceFileVersion irfv : irf.getInformationResourceFileVersions()) {
                assertTrue(irfv.getId() != null);
            }
        }
        return ir;
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls) throws InstantiationException, IllegalAccessException {
        return createAndSaveNewInformationResource(cls, false);
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, boolean createUser) throws InstantiationException,
            IllegalAccessException {
        Project project = new Project();
        Person submitter = getUser();
        if (createUser) {
            submitter = createAndSaveNewPerson("test@user.com", "");
        }
        project.markUpdated(submitter);
        project.setTitle("PROJECT TEST TITLE");
        projectService.save(project);

        R iResource = cls.newInstance();
        iResource.setTitle("TEST TITLE");
        iResource.markUpdated(submitter);
        iResource.setProject(project);
        informationResourceService.save(iResource);
        return iResource;
    }

    protected Dataset createAndSaveNewDataset() {
        Dataset dataset = new Dataset();
        Person testPerson = getUser();
        dataset.setTitle("Test dataset");
        dataset.setDescription("Test dataset description");
        dataset.markUpdated(testPerson);
        // dataset.setConfidential(false);
        dataset.setDateMadePublic(new Date());
        dataset.setDateCreated(1999);
        datasetService.save(dataset);
        return dataset;
    }
    
    
    protected Project createAndSaveNewProject() {
        return createAndSaveNewProject("PROJECT TEST TITLE");
    }
        
    protected Project createAndSaveNewProject(String title) {
        Project project = new Project();
        Person submitter = getUser();
        project.markUpdated(submitter);
        project.setTitle(title);
        project.setDescription(title);
        project.setStatus(Status.ACTIVE);
        projectService.save(project);
        return project;
    }

    @Override
    @Autowired
    @Qualifier("tdarMetadataDataSource")
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    @Autowired
    @Qualifier("genericService")
    public void setGenericService(GenericService genericService) {
        this.genericService = genericService;
    }

    public Logger getLogger() {
        return logger;
    }

    protected <T> T generateNewController(Class<T> controllerClass) {
        T controller = (T) applicationContext.getBean(controllerClass);
        if (controller instanceof AuthenticationAware.Base) {
            ((AuthenticationAware.Base) controller).setServletRequest(getServletRequest());
            ((AuthenticationAware.Base) controller).setServletResponse(getServletResponse());
        }
        return controller;
    }

    protected void init(TdarActionSupport controller, Person user) {
        if (controller != null) {
            controller.setSessionData(getSessionData());

            if (user != null) {
                AuthenticationToken token = AuthenticationToken.create(user);
                controller.getSessionData().setAuthenticationToken(token);
                genericService.save(token);
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
        getControllers().add((ActionSupport) controller);
        return controller;
    }

    protected void init(TdarActionSupport controller) {
        init(controller, getSessionUser());
    }

    protected void initAnonymousUserinit(TdarActionSupport controller) {
        init(controller, null);
    }

    public SessionData getSessionData() {
        if (sessionData == null) {
            this.sessionData = new SessionData();
        }
        return sessionData;
    }

    protected <T> List<T> createListWithSingleNull() {
        ArrayList<T> list = new ArrayList<T>();
        list.add(null);
        return list;
    }

    protected Person getUser() {
        return getUser(getUserId());
    }

    protected Long getUserId() {
        return TestConstants.USER_ID;
    }

    protected Person getBasicUser() {
        return getUser(TestConstants.USER_ID);
    }

    protected Person getAdminUser() {
        return getUser(getAdminUserId());
    }

    protected Person getUser(Long id) {
        Person p = entityService.find(id);
        if (p == null) {
            fail("failed to load user:" + id);
        }
        return p;
    }

    protected void flush() {
        sessionFactory.getCurrentSession().flush();
    }

    protected Long getAdminUserId() {
        return TestConstants.ADMIN_USER_ID;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public HttpServletRequest getServletRequest() {
        return httpServletRequest;
    }

    public HttpServletRequest getServletPostRequest() {
        return httpServletPostRequest;
    }

    public HttpServletRequest getDefaultHttpServletRequest() {
        return defaultHttpServletRequest;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public HttpServletResponse getServletResponse() {
        return httpServletResponse;
    }

    public void addAuthorizedUser(Resource resource, Person person, GeneralPermissions permission) {
        AuthorizedUser authorizedUser = new AuthorizedUser(person, permission);
        ResourceCollection internalResourceCollection = resource.getInternalResourceCollection();
        if (internalResourceCollection == null) {
            internalResourceCollection = new ResourceCollection(CollectionType.INTERNAL);
            internalResourceCollection.setOwner(person);
            resource.getResourceCollections().add(internalResourceCollection);
        }
        genericService.saveOrUpdate(internalResourceCollection);
        authorizedUser.setResourceCollection(internalResourceCollection);
        entityService.save(authorizedUser);
        internalResourceCollection.getAuthorizedUsers().add(authorizedUser);
        entityService.saveOrUpdate(resource);
    }

    /**
     * @param controllers
     *            the controllers to set
     */
    public void setControllers(List<ActionSupport> controllers) {
        this.controllers = controllers;
    }

    /**
     * @return the controllers
     */
    public List<ActionSupport> getControllers() {
        return controllers;
    }

    /**
     * @param ignoreActionErrors
     *            the ignoreActionErrors to set
     */
    public void setIgnoreActionErrors(boolean ignoreActionErrors) {
        this.ignoreActionErrors = ignoreActionErrors;
    }

    /**
     * @return the ignoreActionErrors
     */
    public boolean isIgnoreActionErrors() {
        return ignoreActionErrors;
    }

    /**
     * @return
     */
    public Person getSessionUser() {
        return getUser();
    }

}
