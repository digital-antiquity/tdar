package org.tdar.core.bean;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.jaxp13.Validator;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.collection.CollectionDisplayProperties;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.external.MockMailSender;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.db.conversion.DatasetConversionFactory;
import org.tdar.db.conversion.converters.DatasetConverter;
import org.tdar.db.model.PostgresDatabase;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.TestConfiguration;
import org.xml.sax.SAXException;

@ContextConfiguration(classes = TdarAppConfiguration.class)
@SuppressWarnings("rawtypes")
public abstract class AbstractIntegrationTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    protected HttpServletRequest defaultHttpServletRequest = new MockHttpServletRequest();

    protected HttpServletRequest httpServletRequest = defaultHttpServletRequest;
    protected HttpServletRequest httpServletPostRequest = new MockHttpServletRequest("POST", "/");
    protected HttpServletResponse httpServletResponse = new MockHttpServletResponse();

    protected PostgresDatabase tdarDataImportDatabase = new PostgresDatabase();
    protected Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    protected PlatformTransactionManager transactionManager;
    private TransactionCallback verifyTransactionCallback;
    private TransactionTemplate transactionTemplate;

    public static final String SPITAL_DB_NAME = TestConstants.SPITAL_DB_NAME;
    protected static final String PATH = TestConstants.TEST_DATA_INTEGRATION_DIR;

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
    protected BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    protected PersonalFilestoreService filestoreService;
    @Autowired
    protected AuthorizationService authenticationAndAuthorizationService;
    @Autowired
    protected AuthenticationService authenticationService;
    @Autowired
    private SerializationService serializationService;
    @Autowired
    protected ResourceCollectionService resourceCollectionService;
    @Autowired
    private AuthorizedUserDao authorizedUserDao;

    @Autowired
    public SendEmailProcess sendEmailProcess;

    @Autowired
    protected EmailService emailService;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private SessionData sessionData;
    
    public AbstractIntegrationTestCase() {
        // making sure all test-index data ends up in target
        System.setProperty("solr.data.dir", "target/junit-solr/");
    }

    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher failWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            AbstractIntegrationTestCase.this.onFail(e, description);
        }

    };

    @Before
    public void announceTestStarting() {
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
        genericService.delete(genericService.findAll(Email.class));
        sendEmailProcess.setAllIds(null);
        if (emailService.getMailSender() instanceof MockMailSender) {
            ((MockMailSender) emailService.getMailSender()).getMessages().clear();
        }
        String base = TestConstants.TEST_ROOT_DIR + "schemaCache";
        if (TdarConfiguration.getInstance().shouldLogToFilestore()) {
            serializationService.setUseTransactionalEvents(false);
        }
        schemaMap.put("http://www.loc.gov/standards/mods/v3/mods-3-3.xsd", new File(base, "mods3.3.xsd"));
        schemaMap.put("http://www.openarchives.org/OAI/2.0/oai-identifier.xsd", new File(base, "oai-identifier.xsd"));
        schemaMap.put("http://www.openarchives.org/OAI/2.0/oai_dc.xsd", new File(base, "oaidc.xsd"));
        schemaMap.put("http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", new File(base, "oaipmh.xsd"));
        schemaMap.put("http://www.loc.gov/standards/xlink/xlink.xsd", new File(base, "xlink.xsd"));
        schemaMap.put("http://www.w3.org/XML/2008/06/xlink.xsd", new File(base, "xlink.xsd"));
        schemaMap.put("http://www.w3.org/2001/03/xml.xsd", new File(base, "xml.xsd"));
        schemaMap.put("http://dublincore.org/schemas/xmls/simpledc20021212.xsd", new File(base, "simpledc20021212.xsd"));

    }
    
    protected String getTestFilePath() {
        return PATH;
    }

    // Called when your test fails. Did I say "when"? I meant "if".
    public void onFail(Throwable e, Description description) {
    }

    @After
    public void announceTestOver() {
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());
    }

    public TdarUser createAndSaveNewPerson() {
        return createAndSaveNewPerson(null, "");
    }

    public TdarUser createAndSaveNewPerson(String email_, String suffix) {
        String email = email_;
        if (StringUtils.isBlank(email)) {
            email = TestConstants.DEFAULT_EMAIL;
        }
        TdarUser testPerson = new TdarUser();
        testPerson.setEmail(email);
        testPerson.setFirstName(TestConstants.DEFAULT_FIRST_NAME + suffix);
        testPerson.setLastName(TestConstants.DEFAULT_LAST_NAME + suffix);
        testPerson.setUsername(email);
        Institution institution = entityService.findInstitutionByName(TestConstants.INSTITUTION_NAME);
        if (institution == null) {
            institution = new Institution();
            institution.setName(TestConstants.INSTITUTION_NAME);
            genericService.saveOrUpdate(institution);
        }
        testPerson.setInstitution(institution);
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

    public <R extends InformationResource> R generateAndStoreVersion(Class<R> type, String name, File f, Filestore filestore)
            throws InstantiationException,
            IllegalAccessException, IOException {
        R ir = createAndSaveNewInformationResource(type, false);
        InformationResourceFile irFile = new InformationResourceFile();
        irFile.setInformationResource(ir);
        irFile.setLatestVersion(1);
        irFile.setFilename(name);
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setVersion(1);
        version.setFilename(name);
        version.setExtension(FilenameUtils.getExtension(name));
        version.setInformationResourceFile(irFile);
        version.setDateCreated(new Date());
        version.setInformationResourceFile(irFile);
        version.setFileVersionType(VersionType.UPLOADED);
        irFile.getInformationResourceFileVersions().add(version);
        ir.getInformationResourceFiles().add(irFile);
        genericService.save(irFile);
        genericService.save(version);
        filestore.store(FilestoreObjectType.RESOURCE, f, version);
        return ir;
    }

    public Document generateDocumentWithFileAndUseDefaultUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, false);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        assertTrue("testing " + TestConstants.TEST_DOCUMENT_NAME + " exists", file.exists());
        ir = (Document) addFileToResource(ir, file);
        return ir;
    }

    public Document generateDocumentAndUseDefaultUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, false);
        return ir;
    }

    public Document generateDocumentWithFileAndUser() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class, true);
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

    @Transactional
    public <R extends InformationResource> R addFileToResource(R ir, File file) {
        return addFileToResource(ir, file, FileAccessRestriction.PUBLIC);
    }

    public <R extends InformationResource> R addFileToResource(R ir, File file, FileAccessRestriction restriction) {
        try {
            FileProxy proxy = new FileProxy(file.getName(), file, VersionType.UPLOADED, FileAction.ADD);
            proxy.setRestriction(restriction);
            // PersonalFilestore filestore, T resource, List<FileProxy> fileProxiesToProcess, Long ticketId
            ErrorTransferObject listener = informationResourceService.importFileProxiesAndProcessThroughWorkflow(ir, null, null, Arrays.asList(proxy));
            if (CollectionUtils.isNotEmpty(listener.getActionErrors())) {
                throw new TdarRecoverableRuntimeException(String.format("errors ocurred while processing file: %s", listener));
            }
            // informationResourceService.addOrReplaceInformationResourceFile(ir, new FileInputStream(file), file.getName(), FileAction.ADD,
            // VersionType.UPLOADED);
            evictCache();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        genericService.refresh(ir);// = genericService.find(ir.getClass(), ir.getId());
        for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
            assertTrue(irf.getId() != null);
            for (InformationResourceFileVersion irfv : irf.getInformationResourceFileVersions()) {
                assertTrue(irfv.getId() != null);
            }
        }
        return ir;
    }

    
    public <R extends InformationResource> R replaceFileOnResource(R ir, File file, InformationResourceFile oldFile) {
        try {
            FileProxy proxy = new FileProxy(file.getName(), file, VersionType.UPLOADED, FileAction.REPLACE);
            proxy.setFileId(oldFile.getId());
            // PersonalFilestore filestore, T resource, List<FileProxy> fileProxiesToProcess, Long ticketId
            ErrorTransferObject listener = informationResourceService.importFileProxiesAndProcessThroughWorkflow(ir, null, null, Arrays.asList(proxy));
            if (CollectionUtils.isNotEmpty(listener.getActionErrors())) {
                throw new TdarRecoverableRuntimeException(String.format("errors ocurred while processing file: %s", listener));
            }
            // informationResourceService.addOrReplaceInformationResourceFile(ir, new FileInputStream(file), file.getName(), FileAction.ADD,
            // VersionType.UPLOADED);
            evictCache();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        genericService.refresh(ir);// = genericService.find(ir.getClass(), ir.getId());

        return ir;
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls) {
        return createAndSaveNewInformationResource(cls, false);
    }

    protected <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, boolean createUser) {
        TdarUser submitter = getUser();
        if (createUser) {
            submitter = createAndSaveNewPerson("test@user.com", "");
        }
        return createAndSaveNewInformationResource(cls, submitter);
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, TdarUser persistentPerson) {
        return createAndSaveNewInformationResource(cls, persistentPerson, "TEST TITLE");
    }

    @Transactional
    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, TdarUser persistentPerson, String resourceTitle) {
        return createAndSaveNewInformationResource(cls, null, persistentPerson, resourceTitle);
    }

    public <R extends InformationResource> R createAndSaveNewInformationResource(Class<R> cls, Project project, TdarUser persistentPerson,
            String resourceTitle) {
        R iResource = createAndSaveNewResource(cls, persistentPerson, resourceTitle);
        iResource.setDescription("test description");
        iResource.setProject(project);
        iResource.setDate(2012);
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            iResource.setCopyrightHolder(persistentPerson);
        }
        informationResourceService.saveOrUpdate(iResource);
        return iResource;
    }

    protected Dataset createAndSaveNewDataset() {
        String title = "Test dataset";
        Dataset dataset = createAndSaveNewInformationResource(Dataset.class, null, getUser(), title);
        dataset.setDescription("Test dataset description");
        dataset.setDate(1999);
        datasetService.saveOrUpdate(dataset);
        return dataset;
    }

    protected Project createAndSaveNewProject(String title) {
        return createAndSaveNewResource(Project.class, getUser(), title);
    }

    public <R extends Resource> R createAndSaveNewResource(Class<R> cls, TdarUser persistentPerson, String resourceTitle) {
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
            logger.error("failed: ", e);
            Assert.fail("failed to create/save test" + cls.getSimpleName() + " record: " + e.getMessage() + " \n " + ExceptionUtils.getFullStackTrace(e));
        }

        return resource;
    }

    public <R extends Resource> R createAndSaveNewResource(Class<R> cls) {
        TdarUser persistentPerson = (TdarUser) entityService.findByEmail("test@user.com");
        if (persistentPerson == null) {
            persistentPerson = createAndSaveNewPerson("test@user.com", "");
        }
        String resourceTitle = "Sample " + cls.getSimpleName() + " record";
        return createAndSaveNewResource(cls, persistentPerson, resourceTitle);
    }

    // create new, public, collection with the getUser() as the owner and no resources
    public SharedCollection createAndSaveNewResourceCollection(String name) {
        return init(new SharedCollection(), name);
    }

    public SharedCollection createAndSaveNewWhiteLabelCollection(String name) {
        SharedCollection wlc = new SharedCollection();
        wlc.setProperties(new CollectionDisplayProperties());
        wlc.getProperties().setWhitelabel(true);
        wlc.getProperties().setSubtitle("This is a fancy whitelabel collection");
        init(wlc, name);
        return wlc;
    }

    protected <C extends VisibleCollection> C init(C resourceCollection, String name) {
        resourceCollection.setName(name);
        resourceCollection.setDescription(name);
        resourceCollection.setViewable(true);
        resourceCollection.setHidden(false);
        resourceCollection.markUpdated(getUser());
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

    protected TdarUser getUser() {
        return getUser(getUserId());
    }

    protected Long getUserId() {
        return TestConfiguration.getInstance().getUserId();
    }

    protected final Long getBasicUserId() {
        return TestConfiguration.getInstance().getUserId();
    }

    protected final Long getBillingAdminUserId() {
        return TestConfiguration.getInstance().getBillingAdminUserId();
    }

    protected TdarUser getBasicUser() {
        return getUser(getBasicUserId());
    }

    protected TdarUser getEditorUser() {
        return getUser(getEditorUserId());
    }

    protected TdarUser getBillingUser() {
        return getUser(getBillingAdminUserId());
    }

    protected TdarUser getAdminUser() {
        return getUser(getAdminUserId());
    }

    protected TdarUser getUser(Long id) {
        TdarUser p = genericService.find(TdarUser.class, id);
        if (PersistableUtils.isNullOrTransient(p)) {
            fail("failed to load user:" + id);
        }
        genericService.refresh(p);
        Assert.assertNotNull(p.getEmail());
        // genericService.markWritableOnExistingSession(p);
        // logger.info("({}) {}",p.getEmail(),p);
        return p;
    }

    protected void flush() {
        Session session = sessionFactory.getCurrentSession();
        if (session != null) {
            session.flush();
            session.clear();
        }

        evictCache();

        // searchIndexService.flushToIndexes();
        Cache cache = sessionFactory.getCache();
        if (cache != null) {
            cache.evictAllRegions();
        }

    }

    @SuppressWarnings("deprecation")
    public void evictCache() {
        genericService.synchronize();
    }

    protected Long getAdminUserId() {
        return TestConfiguration.getInstance().getAdminUserId();
    }

    protected Long getEditorUserId() {
        return TestConfiguration.getInstance().getEditorUserId();
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

    public void addAuthorizedUser(Resource resource, TdarUser person, GeneralPermissions permission) {
        AuthorizedUser authorizedUser = new AuthorizedUser(person, permission);
        InternalCollection internalResourceCollection = resource.getInternalResourceCollection();
        if (internalResourceCollection == null) {
            internalResourceCollection = new InternalCollection();
            internalResourceCollection.setOwner(person);
            internalResourceCollection.markUpdated(person);
            resource.getInternalCollections().add(internalResourceCollection);
            genericService.save(internalResourceCollection);
        }
        internalResourceCollection.getAuthorizedUsers().add(authorizedUser);
        logger.debug("{}", internalResourceCollection);
        genericService.saveOrUpdate(internalResourceCollection);
        genericService.saveOrUpdate(authorizedUser);
        genericService.saveOrUpdate(resource);
    }

    private TdarUser sessionUser;

    private static Validator v;

    /**
     * @return
     */
    public TdarUser getSessionUser() {
        if (sessionUser != null) {
            return sessionUser;
        }
        return getUser();
    }

    public void setSessionUser(TdarUser user) {
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
        InputStream rereadableStream = null;
        try {
            rereadableStream = new ByteArrayInputStream(IOUtils.toByteArray(code));
        } catch (Exception e) {
            logger.error("", e);
        }
        if (rereadableStream == null) {
            rereadableStream = code;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(rereadableStream));
        StreamSource is = new StreamSource(reader);
        List<?> errorList = v.getInstanceErrors(is);

        if (!errorList.isEmpty()) {
            StringBuffer errors = new StringBuffer();
            for (Object error : errorList) {
                errors.append(error.toString());
                errors.append(System.getProperty("line.separator"));
                logger.error(error.toString());
            }
            String content = "";
            try {
                rereadableStream.reset();
                content = IOUtils.toString(rereadableStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Assert.fail("Instance invalid: " + errors.toString() + " in:\n" + content);
        }
    }

    private static Map<String, File> schemaMap = new HashMap<String, File>();

    private void addSchemaToValidatorWithLocalFallback(Validator v, String url, File schemaFile) {
        File schema = null;
        if (schemaMap.containsKey(url)) {
            schema = schemaMap.get(url);
            logger.debug("using cache of: {}", url);
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
            v.addSchemaSource(new StreamSource(schema));
            for (Object err : v.getSchemaErrors()) {
                logger.error("*=> schema error: {} ", err.toString());
            }
            assertTrue("Schema is invalid! Error count: " + v.getSchemaErrors().size(), v.isSchemaValid());
        }
    }

    private Validator setupValidator(boolean extra) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (v != null) {
            return v;
        }
        v = new Validator(factory);
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.loc.gov/standards/xlink/xlink.xsd")));
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.w3.org/XML/2008/06/xlink.xsd")));
        // v.addSchemaSource(new StreamSource(schemaMap.get("http://www.w3.org/2001/03/xml.xsd")));
        addSchemaToValidatorWithLocalFallback(v, "http://www.loc.gov/standards/xlink/xlink.xsd", new File(TestConstants.TEST_XML_DIR,
                "schemaCache/xlink.xsd"));

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
            addSchemaToValidatorWithLocalFallback(v, "http://localhost:8180/schema/current", serializationService.generateSchema());
        } catch (Exception e) {
            logger.error("an error occured creating the schema", e);
            assertTrue(false);
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
            Assert.assertEquals(reason, tv.getLocalizedMessage());
        }
    }

    public BillingAccount setupAccountWithInvoiceFor6Mb(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        BillingActivity activity = new BillingActivity("6 mb", 10f, 0, 0L, 0L, 6L, model);
        initAccount(account, activity, getUser());
        genericService.saveOrUpdate(account);
        return account;
    }

    public BillingAccount setupAccountWithInvoiceForOneFile(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("1 file", 10f, 0, 0L, 1L, 0L, model), user);
        genericService.saveOrUpdate(account);
        return account;
    }

    public BillingAccount setupAccountWithInvoiceForOneResource(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("1 resource", 10f, 0, 1L, 0L, 0L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        genericService.saveOrUpdate(account);
        return account;
    }

    public BillingAccount setupAccountWithInvoiceSomeResourcesAndSpace(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("10 resource", 100f, 0, 10L, 10L, 100L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        genericService.saveOrUpdate(account);
        return account;
    }

    public BillingAccount setupAccountWithInvoiceFiveResourcesAndSpace(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("10 resource", 5f, 0, 5L, 5L, 50L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        genericService.saveOrUpdate(account);
        return account;
    }

    public BillingAccount setupAccountWithInvoiceTenOfEach(BillingActivityModel model, TdarUser user) {
        BillingAccount account = new BillingAccount();
        initAccount(account, new BillingActivity("10 resource", 10f, 10, 10L, 10L, 10L, model), user);
        /* add one resource */
        // account.resetTransientTotals();
        genericService.saveOrUpdate(account);
        return account;
    }

    private Invoice initAccount(BillingAccount account, BillingActivity activity, TdarUser user) {
        account.markUpdated(user);
        Invoice invoice = setupInvoice(activity, user);
        account.getInvoices().add(invoice);
        return invoice;
    }

    public Invoice setupInvoice(BillingActivity activity, TdarUser user) {
        Invoice invoice = new Invoice();
        invoice.markUpdated(user);
        genericService.saveOrUpdate(activity.getModel());
        genericService.saveOrUpdate(activity);
        invoice.setNumberOfFiles(activity.getNumberOfFiles());
        invoice.setNumberOfMb(activity.getNumberOfMb());
        invoice.getItems().add(new BillingItem(activity, 1));
        invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
        invoice.markFinal();
        genericService.saveOrUpdate(invoice);
        genericService.saveOrUpdate(invoice.getItems());
        return invoice;
    }

    public BillingAccount setupAccountForPerson(TdarUser p) {
        BillingAccount account = new BillingAccount("my account");
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
        if (StringUtils.isNotBlank(msg)) {
            assertTrue(msg, ObjectUtils.notEqual(obj1, obj2));
        } else {
            assertTrue(String.format("'%s' == '%s'", obj1, obj2), ObjectUtils.notEqual(obj1, obj2));
        }
    }

    public static void assertNotEmpty(Collection<?> results) {
        assertTrue(CollectionUtils.isNotEmpty(results));
    }

    public SimpleMailMessage checkMailAndGetLatest(String text) {
        sendEmailProcess.execute();
        sendEmailProcess.cleanup();
        ArrayList<SimpleMailMessage> messages = ((MockMailSender) emailService.getMailSender()).getMessages();
        logger.debug("{} messages ", messages.size());
        SimpleMailMessage toReturn = null;
        for (SimpleMailMessage msg : messages) {
            logger.debug("{} from:{} to:{}", msg.getSubject(), msg.getFrom(), msg.getTo());
            if (msg.getText().contains(text)) {
                toReturn = msg;
            }
        }
        assertTrue("should have a mail in our 'inbox'", messages.size() > 0);
        if (toReturn != null) {
            messages.remove(toReturn);
        }
        return toReturn;
    }

    public String getText(String msgKey) {
        String msg = MessageHelper.getMessage(msgKey);
        assertThat("key should not be same as getText(key) (did you forget to add it to tdar-messages?)", msgKey, is(not(msg)));
        return msg;
    }

    public AuthorizedUserDao getAuthorizedUserDao() {
        return authorizedUserDao;
    }

    public void setAuthorizedUserDao(AuthorizedUserDao authorizedUserDao) {
        this.authorizedUserDao = authorizedUserDao;
    }

    protected InformationResourceFileVersion makeFileVersion(File name, long id) throws IOException {
        long infoId = (long) (Math.random() * 10000);
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.UPLOADED, name.getName(), 1, infoId, 123L);
        version.setId(id);
        filestore.store(FilestoreObjectType.RESOURCE, name, version);
        version.setTransientFile(name);
        return version;
    }

    public DatasetConverter convertDatabase(File file, Long irFileId) throws IOException, FileNotFoundException {
        InformationResourceFileVersion accessDatasetFileVersion = makeFileVersion(file, irFileId);
        File storedFile = filestore.retrieveFile(FilestoreObjectType.RESOURCE, accessDatasetFileVersion);
        assertTrue("text file exists", storedFile.exists());
        DatasetConverter converter = DatasetConversionFactory.getConverter(accessDatasetFileVersion, tdarDataImportDatabase);
        converter.execute();
        setDataImportTables((String[]) ArrayUtils.addAll(getDataImportTables(), converter.getTableNames().toArray(new String[0])));
        return converter;
    }

    static Long spitalIrId = (long) (Math.random() * 10000);

    public DatasetConverter setupSpitalfieldAccessDatabase() throws IOException {
        spitalIrId++;
        DatasetConverter converter = convertDatabase(new File(getTestFilePath(), SPITAL_DB_NAME), spitalIrId);
        return converter;
    }

    @Autowired
    @Qualifier("tdarDataImportDataSource")
    public void setIntegrationDataSource(DataSource dataSource) {
        tdarDataImportDatabase.setDataSource(dataSource);
    }

    String[] dataImportTables = new String[0];

    public String[] getDataImportTables() {
        return dataImportTables;
    }

    public void setDataImportTables(String[] dataImportTables) {
        this.dataImportTables = dataImportTables;
    }

    @Before
    public void dropDataImportDatabaseTables() throws Exception {
        for (String table : getDataImportTables()) {
            try {
                tdarDataImportDatabase.dropTable(table);
            } catch (Exception ignored) {
            }
        }

    }


    public void assertArchiveContents(Collection<File> expectedFiles, File archive) throws IOException {
        assertArchiveContents(expectedFiles, archive, true);
    }

    public void assertArchiveContents(Collection<File> expectedFiles, File archive, boolean strict) throws IOException {

        Map<String, Long> nameSize = unzipArchive(archive);
        List<String> errs = new ArrayList<>();
        for (File expected : expectedFiles) {
            Long size = nameSize.get(expected.getName());
            if (size == null) {
                errs.add("expected file not in archive:" + expected.getName());
                continue;
            }
            // if doing a strict test, assert that file is exactly the same
            if (strict) {
                if (size.longValue() != expected.length()) {
                    errs.add(String.format("%s: item in archive %s does not have same content", size.longValue(), expected));
                }
                // otherwise, just make sure that the actual file is not empty
            } else {
                if (expected.length() > 0) {
                    assertThat(size, greaterThan(0L));
                }
            }
        }
        if (errs.size() > 0) {
            for (String err : errs) {
                logger.error(err);
            }
            fail("problems found in archive:" + archive);
        }
    }

    public Map<String, Long> unzipArchive(File archive) {
        Map<String, Long> files = new HashMap<>();
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(archive);
            for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                files.put(entry.getName(), entry.getSize());
                logger.info("{} {}", entry.getName(), entry.getSize());
            }
        } catch (Exception e) {
            logger.error("Error while extracting file " + archive, e);
        } finally {
            if (zipfile != null) {
                IOUtils.closeQuietly(zipfile);
            }
        }
        return files;
    }

}
