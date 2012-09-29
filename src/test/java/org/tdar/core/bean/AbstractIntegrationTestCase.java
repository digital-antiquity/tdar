package org.tdar.core.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.DatasetService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.FilestoreService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.InformationResourceService;
import org.tdar.core.service.ProjectService;
import org.tdar.core.service.ResourceService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.core.service.SearchService;
import org.tdar.struts.data.FileProxy;

import com.google.common.annotations.Beta;

import static org.junit.Assert.*;

@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
public abstract class AbstractIntegrationTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    protected HttpServletRequest httpServletRequest = new MockHttpServletRequest();
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

    protected Logger logger = Logger.getLogger(getClass());

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

    public InformationResource generateInformationResourceWithFile() throws InstantiationException, IllegalAccessException {
        Document ir = createAndSaveNewInformationResource(Document.class);
        assertTrue(ir.getResourceType() == ResourceType.DOCUMENT);
        File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
        assertTrue("testing "+TestConstants.TEST_DOCUMENT_NAME+" exists", file.exists());
        try {
            FileProxy proxy = new FileProxy(file.getName(), new FileInputStream(file), VersionType.UPLOADED, FileAction.ADD);
            informationResourceService.processFileProxy(ir, proxy);
//            informationResourceService.addOrReplaceInformationResourceFile(ir, new FileInputStream(file), file.getName(), FileAction.ADD,
//                    VersionType.UPLOADED);
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
        return createAndSaveNewInformationResource(cls, true);
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
        Person testPerson = getTestPerson();
        dataset.setTitle("Test dataset");
        dataset.setDescription("Test dataset description");
        dataset.markUpdated(testPerson);
//        dataset.setConfidential(false);
        dataset.setDateMadePublic(new Date());
        dataset.setDateCreated("1999");
        datasetService.save(dataset);
        return dataset;
    }

    public Person getTestPerson() {
        return entityService.findPerson(TestConstants.USER_ID);
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
        return controller;
    }

    protected <T> List<T> createListWithSingleNull() {
        ArrayList<T> list = new ArrayList<T>();
        list.add(null);
        return list;
    }

    protected Person getUser() {
        return getUser(getUserId());
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

    protected Long getUserId() {
        return TestConstants.USER_ID;
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

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public HttpServletResponse getServletResponse() {
        return httpServletResponse;
    }

}
