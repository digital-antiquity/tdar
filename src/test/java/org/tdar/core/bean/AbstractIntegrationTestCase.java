package org.tdar.core.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.jaxp13.Validator;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Account;
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
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.MockMailSender;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.Filestore;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.FileProxy;
import org.tdar.web.SessionData;
import org.xml.sax.SAXException;

import com.opensymphony.xwork2.ActionSupport;

@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
@SuppressWarnings("rawtypes")
public abstract class AbstractIntegrationTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    protected HttpServletRequest defaultHttpServletRequest = new MockHttpServletRequest();

    protected HttpServletRequest httpServletRequest = defaultHttpServletRequest;
    protected HttpServletRequest httpServletPostRequest = new MockHttpServletRequest("POST", "/");
    protected HttpServletResponse httpServletResponse = new MockHttpServletResponse();

    protected PlatformTransactionManager transactionManager;
    private TransactionCallback verifyTransactionCallback;
    private TransactionTemplate transactionTemplate;

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected DatasetService datasetService;
    @Autowired
    protected DataTableService dataTableService;
    @Autowired
    protected DataIntegrationService dataIntegrationService;
    @Autowired
    protected GenericService genericService;
    @Autowired
    protected UrlService urlService;
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
    protected PersonalFilestoreService filestoreService;
    @Autowired
    protected AuthenticationAndAuthorizationService authenticationAndAuthorizationService;
    @Autowired
    private XmlService xmlService;
    @Autowired
    protected ResourceCollectionService resourceCollectionService;

    @Autowired
    protected EmailService emailService;

    private List<ActionSupport> controllers = new ArrayList<ActionSupport>();
    private boolean ignoreActionErrors = false;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected MockMailSender mockMailSender = new MockMailSender();
    private SessionData sessionData;

    @Rule
    public TestName testName = new TestName();

    @Before
    public final void announceTestStarting() {
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());

        emailService.setMailSender(mockMailSender);

        getControllers().clear();
        setIgnoreActionErrors(false);
    }

    @After
    public final void announceTestOver() {

        int errorCount = 0;
        if (!isIgnoreActionErrors()) {
            for (ActionSupport controller : getControllers()) {
                if (controller != null && !controller.getActionErrors().isEmpty()) {
                    logger.error("action errors {}", controller.getActionErrors());
                    errorCount += controller.getActionErrors().size();
                }
            }
        }
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());

        if (errorCount > 0) {
            Assert.fail(String.format("There were %d action errors", errorCount));
        }
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

    public Document generateInformationResourceWithFileAndUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, false);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        assertTrue("testing " + TestConstants.TEST_DOCUMENT_NAME + " exists", file.exists());
        ir = (Document) addFileToResource(ir, file);
        return ir;
    }

    public Document generateDocumentWithUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, false);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        return ir;
    }

    public InformationResource addFileToResource(InformationResource ir, File file) {
        try {
            FileProxy proxy = new FileProxy(file.getName(), file, VersionType.UPLOADED, FileAction.ADD);
            informationResourceService.processFileProxy(ir, proxy);
            // informationResourceService.addOrReplaceInformationResourceFile(ir, new FileInputStream(file), file.getName(), FileAction.ADD,
            // VersionType.UPLOADED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ir = genericService.find(ir.getClass(), ir.getId());
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
        Person submitter = getUser();
        if (createUser) {
            submitter = createAndSaveNewPerson("test@user.com", "");
        }
        return createAndSaveNewInformationResource(cls, submitter);
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, Person persistentPerson)
            throws InstantiationException, IllegalAccessException {
        return createAndSaveNewInformationResource(cls, persistentPerson, "TEST TITLE");
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, Person persistentPerson, String resourceTitle)
            throws InstantiationException, IllegalAccessException {
        Project project = new Project();
        project.markUpdated(persistentPerson);
        project.setTitle("PROJECT " + resourceTitle);
        project.setDescription("test description");
        projectService.save(project);
        return createAndSaveNewInformationResource(cls, project, persistentPerson, resourceTitle);
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, Project project, Person persistentPerson, String resourceTitle)
            throws InstantiationException, IllegalAccessException {

        R iResource = cls.newInstance();
        iResource.setTitle(resourceTitle);
        iResource.setDescription("test description");
        iResource.markUpdated(persistentPerson);
        iResource.setProject(project);
        iResource.setDate(2012);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            iResource.setCopyrightHolder(persistentPerson);
        }
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
        dataset.setDate(1999);
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

    public <R extends Resource> R createAndSaveNewResource(Class<R> cls, Person persistentPerson, String resourceTitle) {
        R resource = null;
        try {
            resource = cls.newInstance();
            resource.markUpdated(persistentPerson);
            resource.setTitle(resourceTitle);
            resource.setDescription("description for " + resourceTitle);
            if (resource instanceof InformationResource) {
                ((InformationResource) resource).setDate(2012);
            }
            genericService.save(resource);
        } catch (Exception e) {
            Assert.fail("failed to create/save test" + cls.getSimpleName() + " record");
        }

        return resource;
    }

    public <R extends Resource> R createAndSaveNewResource(Class<R> cls) {
        Person persistentPerson = entityService.findByEmail("test@user.com");
        if (persistentPerson == null) {
            persistentPerson = createAndSaveNewPerson("test@user.com", "");
        }
        String resourceTitle = "Sample " + cls.getSimpleName() + " record";
        return createAndSaveNewResource(cls, persistentPerson, resourceTitle);
    }

    // create new, public, collection with the getUser() as the owner and no resources
    public ResourceCollection createAndSaveNewResourceCollection(String name) {
        ResourceCollection resourceCollection = new ResourceCollection();
        resourceCollection.setName(name);
        resourceCollection.setDescription(name);
        resourceCollection.setType(CollectionType.SHARED);
        resourceCollection.setViewable(true);
        resourceCollection.setVisible(true);
        resourceCollection.setOwner(getUser());
        genericService.saveOrUpdate(resourceCollection);
        return resourceCollection;
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
            ((TdarActionSupport) controller).setServletRequest(getServletRequest());
            ((TdarActionSupport) controller).setServletResponse(getServletResponse());
        }
        return controller;
    }

    protected void init(TdarActionSupport controller, Person user) {
        if (controller != null) {
            controller.setSessionData(getSessionData());

            if (user != null && Persistable.Base.isTransient(user)) {
                throw new TdarRecoverableRuntimeException("can't test this way right now, must persist first");
            } else if (user != null) {
                Person user_ = genericService.find(Person.class, user.getId());
                AuthenticationToken token = AuthenticationToken.create(user_);
                controller.getSessionData().setAuthenticationToken(token);
                genericService.save(token);
                // genericService.detachFromSession(user_);
            } else {
                controller.getSessionData().setAuthenticationToken(new AuthenticationToken());
            }
        }
    }

    protected <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass) {
        return generateNewInitializedController(controllerClass, null);
    }

    protected <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass, Person user) {
        T controller = generateNewController(controllerClass);
        if (controller instanceof TdarActionSupport) {
            if (user != null) {
                init((TdarActionSupport) controller, user);
            } else {
                init((TdarActionSupport) controller);
            }
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
        internalResourceCollection.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(internalResourceCollection);
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

    public void ignoreActionErrors(boolean ignoreActionErrors) {
        this.ignoreActionErrors = ignoreActionErrors;
    }

    /**
     * @return the ignoreActionErrors
     */
    public boolean isIgnoreActionErrors() {
        return ignoreActionErrors;
    }

    
    private Person sessionUser;
    /**
     * @return
     */
    public Person getSessionUser() {
        if (sessionUser != null) {
            return sessionUser;
        }
        return getUser();
    }
    
    public void setSessionUser(Person user) {
        this.sessionUser = user;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected <V> V runInNewTransaction(TransactionCallback<V> action) {
        logger.debug("starting new transaction");
        return transactionTemplate.execute(action);
    }

    protected void runInNewTransactionWithoutResult(TransactionCallback<Object> action) {
        runInNewTransaction(action);
    }

    @AfterTransaction
    @SuppressWarnings("unchecked")
    public void verifyTransactionCallback() {
        if (verifyTransactionCallback != null) {
            runInNewTransaction(verifyTransactionCallback);
        }
    }

    /**
     * Validate a response against an external schema
     * 
     * @param schemaLocation
     *            the URL of the schema to use to validate the document
     * @throws ConfigurationException
     * @throws SAXException
     */
    public void testValidXMLResponse(InputStream code, String schemaLocation) throws ConfigurationException, SAXException {
        testValidXML(code, schemaLocation, true);
    }

    private void testValidXML(InputStream code, String schema, boolean loadSchemas) {
        Validator v = setupValidator(loadSchemas);

        if (schema != null) {
            v.addSchemaSource(new StreamSource(schema));
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(code));
        StreamSource is = new StreamSource(reader);
        List<?> errorList = v.getInstanceErrors(is);

        if (!errorList.isEmpty()) {
            StringBuffer errors = new StringBuffer();
            for (Object error : errorList) {
                errors.append(error.toString());
                errors.append(System.getProperty("line.separator"));
                logger.error(error.toString());
            }
            Assert.fail("Instance invalid: " + errors.toString());
        }
    }

    private static Map<String, File> schemaMap = new HashMap<String, File>();

    private void addSchemaToValidatorWithLocalFallback(Validator v, String url, File schemaFile) {
        File schema = null;
        if (schemaMap.containsKey(url)) {
            schema = schemaMap.get(url);
        } else {
            logger.debug("attempting to add schema to validation list: " + url);
            try {
                File tmpFile = File.createTempFile(schemaFile.getName(), ".temp.xsd");
                FileUtils.writeStringToFile(tmpFile, IOUtils.toString(new URI(url)));
                schema = tmpFile;
            } catch (Throwable e) {
                logger.debug("could not validate against remote schema, attempting to use cached fallback:" + schemaFile);
            }
            if (schema == null) {
                try {
                    schema = schemaFile;
                } catch (Exception e) {
                    logger.debug("could not validate against local schema");
                }
            } else {
                schemaMap.put(url, schema);
            }
        }

        if (schema != null) {
            // try {
            // // testValidXMLSchemaResponse(schema);
            // } catch (Exception e) {
            // logger.debug("schema setup exception ", e);
            // assertTrue(false);
            // }
            v.addSchemaSource(new StreamSource(schema));
            for (Object err : v.getSchemaErrors()) {
                logger.error(err.toString());
            }
            assertTrue(v.isSchemaValid());
        }
    }

    private Validator setupValidator(boolean extra) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Validator v = new Validator(factory);

        if (extra) {
            // not the "ideal" way to set these up, but it should work... caching the schema locally and injecting
            addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", new File(TestConstants.TEST_XML_DIR,
                    "schemaCache/oaipmh.xsd"));
            addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                    new File(TestConstants.TEST_XML_DIR, "schemaCache/oaidc.xsd"));
            addSchemaToValidatorWithLocalFallback(v, "http://www.loc.gov/standards/mods/v3/mods-3-3.xsd", new File(TestConstants.TEST_XML_DIR,
                    "schemaCache/mods3.3.xsd"));
            addSchemaToValidatorWithLocalFallback(v, "http://www.openarchives.org/OAI/2.0/oai-identifier.xsd", new File(TestConstants.TEST_XML_DIR,
                    "schemaCache/oai-identifier.xsd"));

            try {
                addSchemaToValidatorWithLocalFallback(v, "http://localhost:8180/schema/current", xmlService.generateSchema());
            } catch (Exception e) {
                logger.error("an error occured creating the schema", e);
                assertTrue(false);
            }
        }
        return v;
    }

    /**
     * Validate that a response is a valid XML schema
     * 
     * @throws ConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void testValidXMLSchemaResponse(String code) throws ConfigurationException, SAXException, IOException {
        Validator setupValidator = setupValidator(false);
        // cleanup -- this is lazy
        File tempFile = File.createTempFile("test-schema", "xsd");
        FileUtils.writeStringToFile(tempFile, code);
        addSchemaToValidatorWithLocalFallback(setupValidator, null, tempFile);
    }

    public TransactionCallback getVerifyTransactionCallback() {
        return verifyTransactionCallback;
    }

    public <T> void setVerifyTransactionCallback(TransactionCallback<T> verifyTransactionCallback) {
        this.verifyTransactionCallback = verifyTransactionCallback;
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    /**
     * @return the dataTableService
     */
    public DataTableService getDataTableService() {
        return dataTableService;
    }

    /**
     * @return the dataIntegrationService
     */
    public DataIntegrationService getDataIntegrationService() {
        return dataIntegrationService;
    }

    public static void assertInvalid(Validatable address, String reason) {
        TdarValidationException tv = null;
        try {
            address.isValid();
        } catch (TdarValidationException ex) {
            tv = ex;
        }
        Assert.assertNotNull(tv);
        if (reason != null) {
            Assert.assertEquals(reason, tv.getMessage());
        }
    }

    public Account setupAccountForPerson(Person p) {
        Account account = new Account("my account");
        account.setOwner(p);
        account.setStatus(Status.ACTIVE);
        account.markUpdated(getUser());
        genericService.saveOrUpdate(account);
        return account;
    }

    public static void assertNotEquals(Object obj1, Object obj2) {
        assertNotEquals("", obj1, obj2);
    }

    public static void assertNotEquals(String msg, Object obj1, Object obj2) {
        assertTrue(msg, ObjectUtils.notEqual(obj1, obj2));
    }
    
    public static void assertNotEmpty(List<Resource> results) {
        assertTrue(CollectionUtils.isNotEmpty(results));
    }

}
