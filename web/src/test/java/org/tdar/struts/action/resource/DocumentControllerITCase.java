package org.tdar.struts.action.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.document.DocumentViewAction;
import org.tdar.struts.action.project.ProjectController;
import org.tdar.struts.action.upload.UploadController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;

public class DocumentControllerITCase extends AbstractResourceControllerITCase {

    public DocumentController initControllerFields() throws TdarActionException {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.setProjectId(TestConstants.PARENT_PROJECT_ID);
        return controller;
    }

    @Test
    public void testShowStatuses() {
        DocumentController dc = generateNewController(DocumentController.class);
        init(dc, getUser());
        List<Status> statuses = dc.getStatuses();
        assertFalse(statuses.isEmpty());
    }

    @Test
    @Rollback
    public void testDocumentProjectRights() throws TdarActionException {
        /*
         * Collection |_ Authorized User with Rights |_ Child Collection |_
         * Project
         *
         * Issue -- project doesn't show up in
         * AbstractInformationResourceController.getPotentialParents()
         */
        Project project = new Project();
        project.setTitle("test rights project");
        project.setDescription(project.getTitle());
        project.markUpdated(getAdminUser());
        genericService.saveOrUpdate(project);
        SharedCollection collection = createResourceCollectionWithAdminRights();
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(collection);
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBasicUser(), GeneralPermissions.ADMINISTER_SHARE));
        genericService.saveOrUpdate(collection);

        SharedCollection collectionChild = new SharedCollection();
        collectionChild.setName("child collection with project");
//        collectionChild.setSortBy(SortOption.RELEVANCE);
        collectionChild.setParent(collection);
//        collectionChild.setOrientation(DisplayOrientation.GRID);
        collectionChild.setDescription(collectionChild.getTitle());
        collectionChild.markUpdated(getAdminUser());
        project.getSharedCollections().add(collectionChild);
        collectionChild.getResources().add(project);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), collectionChild, collection, SharedCollection.class);
        genericService.saveOrUpdate(collectionChild);
        genericService.saveOrUpdate(project);
        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.prepare();
        String add = dc.add();
        assertEquals(Action.SUCCESS, add);
        List<Resource> potentialParents = dc.getPotentialParents();

        logger.debug("my parents:  size:{}  contents:{}", potentialParents.size(), potentialParents);
        assertTrue(potentialParents.contains(project));
    }

    @SuppressWarnings({ "deprecation", "unused" })
    @Test
    @Rollback
    public void testDocumentEditRights() throws TdarActionException {
        Document doc = new Document();
        doc.setTitle("test rights project");
        doc.setDescription(doc.getTitle());
        doc.markUpdated(getAdminUser());
        genericService.saveOrUpdate(doc);
        SharedCollection collection = createResourceCollectionWithAdminRights();
        genericService.saveOrUpdate(collection);
//        InternalCollection internal = new InternalCollection();
//        internal.markUpdated(getAdminUser());
//        genericService.saveOrUpdate(internal);
        doc.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
//        genericService.saveOrUpdate(internal);

        doc.getSharedCollections().add(collection);
//        doc.getInternalCollections().add(internal);
//        internal.getResources().add(doc);
        collection.getResources().add(doc);
//        genericService.saveOrUpdate(internal);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(doc);
        Long docId = doc.getId();
        doc = null;
//        internal = null;
        collection = null;
        genericService.synchronize();

        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.setId(docId);
        dc.prepare();
        String add = dc.edit();
        assertEquals(TdarActionSupport.SUCCESS, add);
        List<Resource> potentialParents = dc.getPotentialParents();
        dc.setServletRequest(getServletPostRequest());
        String save = dc.save();
        assertEquals(TdarActionSupport.SUCCESS, save);

    }

    private SharedCollection createResourceCollectionWithAdminRights() {
        SharedCollection collection = new SharedCollection();
        collection.setName("parent collection with rights");
//        collection.setSortBy(SortOption.RELEVANCE);
//        collection.setOrientation(DisplayOrientation.GRID);
        collection.setDescription(collection.getTitle());
        collection.markUpdated(getAdminUser());
        genericService.saveOrUpdate(collection);
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(collection);
        return collection;
    }

    @Test
    @Rollback
    public void testSubmitterChangeRights() throws TdarActionException {
        // setup document
        TdarUser newUser = createAndSaveNewPerson();
        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.prepare();
        Document doc = dc.getDocument();
        doc.setTitle("test");
        doc.setDate(1234);
        doc.setDescription("my description");
        dc.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, dc.save());

        // change the submitter to the admin
        Long id = doc.getId();
        doc = null;
        dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.setId(id);
        dc.prepare();
        dc.edit();
        dc.setSubmitter(newUser);
        dc.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, dc.save());
        setIgnoreActionErrors(true);

        
        // try to edit as basic user -- should work
        doc = null;
        dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.setId(id);
        dc.prepare();
        assertEquals(Action.SUCCESS, dc.edit());

        // try to edit as new user, should not work
        dc = generateNewInitializedController(DocumentController.class, newUser);
        dc.setId(id);
        int statusCode = -1;
        try {
            dc.prepare();
            assertNotEquals(Action.SUCCESS, dc.edit());
        } catch (TdarActionException e) {
            statusCode = e.getStatusCode();
        }
        assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(), statusCode);

    }

    @Ignore("Ignoring because this is an internal performance test, not really a unit-test")
    @Test
    @Rollback
    public void testPerformance() throws InstantiationException, IllegalAccessException, TdarActionException {
        // 42s -- reconcileSet + indexInterceptor @100docs
        // 52s -- reconcileSet + w/o indexInterceptor @100docs
        // 43s -- setter model + w/o indexInterceptor @100docs
        // 41s -- setter model + w indexInterceptor @100docs
        // 54s -- with clearAll + indexInterceptor @100docs
        // 44s -- with clearAll + w/o indexInterceptor @100docs
        Project project = new Project();
        project.setTitle("PerfTest");
        project.setDescription("perfTest");
        project.markUpdated(getUser());
        genericService.saveOrUpdate(project);
        List<Person> fiftyPList = genericService.findRandom(Person.class, 50);
        List<Institution> fiftyIList = genericService.findRandom(Institution.class, 50);
        for (Person person : fiftyPList) {
            ResourceCreator rc = new ResourceCreator(person, ResourceCreatorRole.CONTRIBUTOR);
            project.getResourceCreators().add(rc);
            genericService.saveOrUpdate(rc);
            // project.getResourceCreators().add(rc);
        }
        for (Institution inst : fiftyIList) {
            ResourceCreator rc = new ResourceCreator(inst, ResourceCreatorRole.REPOSITORY);
            project.getResourceCreators().add(rc);
            genericService.saveOrUpdate(rc);
        }
        project.getCultureKeywords().addAll(genericService.findRandom(CultureKeyword.class, 10));
        project.getMaterialKeywords().addAll(genericService.findRandom(MaterialKeyword.class, 10));
        project.getSiteNameKeywords().addAll(genericService.findRandom(SiteNameKeyword.class, 10));
        genericService.saveOrUpdate(project);
        int totalNumOfRecords = 100;

        for (int i = 0; i < totalNumOfRecords; i++) {
            Document doc = createAndSaveNewInformationResource(Document.class, project, getAdminUser(),
                    "perf doc #" + i);
            doc.getCultureKeywords().addAll(genericService.findRandom(CultureKeyword.class, 5));
            doc.setInheritingSiteInformation(true);
            doc.setInheritingMaterialInformation(true);
            for (Person person : genericService.findRandom(Person.class, 10)) {
                ResourceCreator rc = new ResourceCreator(person, ResourceCreatorRole.AUTHOR);
                doc.getResourceCreators().add(rc);
                genericService.saveOrUpdate(rc);
            }
            File file = new File(TestConstants.TEST_DOCUMENT_DIR + TestConstants.TEST_DOCUMENT_NAME);
            addFileToResource(doc, file);
            genericService.saveOrUpdate(doc);
        }
        Long projectId = project.getId();
        evictCache();
        project = null;

        // this the test...
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        controller.setId(projectId);
        controller.prepare();
        Long time = System.currentTimeMillis();
        controller.setServletRequest(getServletPostRequest());
        controller.edit();
        controller.save();
        logger.info("total save time: " + (System.currentTimeMillis() - time));
    }

    @Test
    public void testOpenURLGeneration() throws TdarActionException {
        DocumentViewAction controller = generateNewInitializedController(DocumentViewAction.class);
        controller.setId(4231L);
        controller.prepare();
        String openUrl = controller.getOpenUrl();
        logger.debug(openUrl);
        assertEquals(
                "ctx_ver=Z39.88-2004&amp;rfr_id=info:sid/http://localhost:8180&amp;rft_val_fmt=info:ofi/fmt:kev:mtx:unknown&amp;rft.genre=unknown&amp;rft.title=2008+New+Philadelphia+Archaeology+Report%2C+Chapter+3%2C+Block+3%2C+Lot+4",
                openUrl);
    }

    @Test
    public void testScholarSource() throws Exception {
        DocumentViewAction controller = generateNewInitializedController(DocumentViewAction.class);
        controller.setId(4231L);
        controller.prepare();
        String scholar = controller.getGoogleScholarTags();
        logger.debug(scholar);
        assertEquals(
                "<meta name=\"citation_title\" content=\"2008 New Philadelphia Archaeology Report, Chapter 3, Block 3, Lot 4\"/>\n<meta name=\"citation_date\" content=\"2008\"/>\n<meta name=\"citation_online_date\" content=\"2010/08/14\"/>\n",
                scholar);

    }

    @Test
    @Rollback()
    public void testInstitutionResourceCreatorNew() throws Exception {
        DocumentController controller = initControllerFields();
        // create a document with a single resource creator not currently in the
        // database, then save.
        String EXPECTED_INSTITUTION_NAME = "NewBlankInstitution";
        Long originalId = controller.getResource().getId();
        // FIXME: in reality, struts calls the getter, not the setter, but from
        // there I'm not sure how it's populating the elements.
        controller.getAuthorshipProxies()
                .add(getNewResourceCreator(EXPECTED_INSTITUTION_NAME, -1L, ResourceCreatorRole.REPOSITORY));
        controller.getAuthorshipProxies()
                .add(getNewResourceCreator(EXPECTED_INSTITUTION_NAME, -1L, ResourceCreatorRole.CONTRIBUTOR));
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        DocumentViewAction rva = generateNewInitializedController(DocumentViewAction.class);
        rva.setId(newId);
        rva.prepare();
        rva.view();

        d = controller.getResource();
//        Assert.assertEquals(d.getInternalResourceCollection(), null);
        Assert.assertEquals("expecting document IDs to match (save/reloaded)", newId, d.getId());
        Set<ResourceCreator> resourceCreators = d.getResourceCreators();
        Assert.assertTrue(resourceCreators.size() > 0);
        List<ResourceCreator> actualResourceCreators = new ArrayList<ResourceCreator>(d.getResourceCreators());
        Collections.sort(actualResourceCreators);
        ResourceCreator actualCreator = actualResourceCreators.get(0);
        Assert.assertNotNull(actualCreator);
        Assert.assertEquals(CreatorType.INSTITUTION, actualCreator.getCreator().getCreatorType());
        Assert.assertTrue(actualCreator.getCreator().getName().contains(EXPECTED_INSTITUTION_NAME));
        Assert.assertEquals(ResourceCreatorRole.REPOSITORY, actualCreator.getRole());
        String deletionReason = "this is a test";
        rva = deleteResource(newId, deletionReason);

        boolean seen = false;
        for (ResourceNote note : rva.getResource().getResourceNotes()) {
            if ((note.getType() == ResourceNoteType.ADMIN) && note.getNote().equals(deletionReason)) {
                seen = true;
            }
        }
        assertTrue("a deletion note should have been added", seen);
    }

    @SuppressWarnings("deprecation")
    private DocumentViewAction deleteResource(Long newId, String deletionReason) throws Exception {
        DocumentViewAction rva = generateNewInitializedController(DocumentViewAction.class);
        rva.setId(newId);
        rva.prepare();
        rva.view();

        ResourceDeleteAction deleteAction = generateNewInitializedController(ResourceDeleteAction.class);
        deleteAction.setId(newId);
        deleteAction.prepare();

        Resource res = deleteAction.getPersistable();
        Assert.assertEquals("expecting document IDs to match (save/reloaded)", newId, res.getId());
        deleteAction.setDeletionReason(deletionReason);
        deleteAction.setServletRequest(getServletPostRequest());
        deleteAction.setDelete(TdarActionSupport.DELETE);
        String delete = deleteAction.delete();
        assertEquals(TdarActionSupport.SUCCESS, delete);
        logger.debug("status: {}", delete);
        genericService.synchronize();
        rva = generateNewInitializedController(DocumentViewAction.class, getAdminUser());
        rva.setId(newId);
        rva.prepare();
        rva.view();
        Assert.assertEquals("expecting document status to be deleted", Status.DELETED, rva.getResource().getStatus());
        Assert.assertEquals("expecting controller status to be deleted", Status.DELETED, rva.getStatus());

        return rva;
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback()
    public void testPersonResourceCreatorNew() throws Exception {
        DocumentController controller = initControllerFields();

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies()
                .add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));
        controller.getAuthorshipProxies()
                .add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.EDITOR));
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        controller = generateNewInitializedController(DocumentController.class);
        controller.setId(newId);
        controller.prepare();
        controller.edit();

        d = controller.getResource();
        Assert.assertEquals("expecting document IDs to match (save/reloaded)", newId, d.getId());
        Set<ResourceCreator> resourceCreators = d.getResourceCreators();
        Assert.assertTrue(resourceCreators.size() > 0);
        List<ResourceCreator> actualResourceCreators = new ArrayList<ResourceCreator>(d.getResourceCreators());
        Collections.sort(actualResourceCreators);
        ResourceCreator actualCreator = actualResourceCreators.get(0);
        Assert.assertNotNull(actualCreator);
        Assert.assertEquals(CreatorType.PERSON, actualCreator.getCreator().getCreatorType());
        Assert.assertTrue(actualCreator.getCreator().getName().contains("newLast"));
        Assert.assertEquals(ResourceCreatorRole.AUTHOR, actualCreator.getRole());
        String deletionReason = "because";
        DocumentViewAction rva = deleteResource(d.getId(), deletionReason);
    }

    // return a populated "new" resource creator person (i.e. all person fields
    // set but null id)
    public ResourceCreatorProxy getNewResourceCreator(String name, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        rcp.getInstitution().setName(name);
        // FIXME: THIS NEEDS TO WORK WITHOUT SETTING AN ID as well as when an ID
        // IS SET
        if ((System.currentTimeMillis() % 2) == 0) {
            rcp.getInstitution().setId(-1L);
        }
        rcp.setRole(role);
        return rcp;
    }

    @Test
    @Rollback()
    public void testEditResourceCreators() throws Exception {
        DocumentController controller = initControllerFields();

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies()
                .add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long newId = controller.getResource().getId();

        Assert.assertNotNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        ResourceDeleteAction deleteAction = generateNewInitializedController(ResourceDeleteAction.class);
        deleteAction.setId(newId);
        deleteAction.prepare();

        Resource res = deleteAction.getPersistable();
        Assert.assertEquals("expecting document IDs to match (save/reloaded)", newId, res.getId());
        Set<ResourceCreator> resourceCreators = res.getResourceCreators();
        Assert.assertTrue(resourceCreators.size() > 0);
        ResourceCreator actualCreator = (ResourceCreator) d.getResourceCreators().toArray()[0];
        Assert.assertNotNull(actualCreator);
        Assert.assertEquals(CreatorType.PERSON, actualCreator.getCreator().getCreatorType());
        Assert.assertTrue(actualCreator.getCreator().getName().contains("newLast"));
        deleteAction.delete(controller.getDocument());

        // FIXME: should add and replace items here to really test

        // FIXME: issues with hydrating resources with Institutions

        DocumentViewAction rva = generateNewInitializedController(DocumentViewAction.class, getAdminUser());
        rva.setId(newId);
        rva.prepare();
        rva.view();
        // assert my authorproxies have what i think they should have (rendering
        // edit page)
        controller = generateNewInitializedController(DocumentController.class, getAdminUser());
        controller.setId(newId);
        controller.prepare();
        controller.edit();
        controller.getPersistable().setStatus(Status.DRAFT);
        controller.setAuthorshipProxies(new ArrayList<ResourceCreatorProxy>());
        // deleting all authorship resource creators
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        // loading the view page
        rva = generateNewInitializedController(DocumentViewAction.class);
        rva.setId(newId);
        rva.prepare();
        rva.view();
        logger.info("{}", rva.getAuthorshipProxies());
        Assert.assertEquals("expecting size zero", 0, rva.getAuthorshipProxies().size());
        logger.debug("{}", rva.getAuthorshipProxies().size());
        Assert.assertTrue("expecting invaled proxy", rva.getAuthorshipProxies().isEmpty());
    }

    @Test
    @Rollback
    // create a simple document, using a pre-existing author with no email
    // address. make sure that we didn't create a new person record.
    public void testForDuplicatePersonWithNoEmail() throws Exception {
        DocumentController controller = initControllerFields();
        // get person record count.
        int expectedPersonCount = genericService.findAll(Person.class).size();

        ResourceCreatorProxy rcp = getNewResourceCreator("Cressey", "Pamela", null, null, ResourceCreatorRole.AUTHOR);
        rcp.getPerson().setInstitution(null);

        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies().add(rcp);
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("testing to see if the system created a person record when it shouldn't have");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        int actualPersonCount = genericService.findAll(Person.class).size();
        Assert.assertEquals(
                "Person count should be the same after creating new document with an author that already exists",
                expectedPersonCount, actualPersonCount);
    }

    @Test
    @Rollback
    public void testResourceCreatorSortOrder() throws Exception {
        int numberOfResourceCreators = 20;
        DocumentController controller = initControllerFields();
        for (int i = 0; i < numberOfResourceCreators; i++) {
            controller.getCreditProxies()
                    .add(getNewResourceCreator("Cressey" + i, "Pamela", null, null, ResourceCreatorRole.CONTACT));
        }
        Document d = controller.getDocument();
        d.setTitle("Testing Resource Creator Sort Order");
        d.setDescription("Resource Creator sort order");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long documentId = controller.getResource().getId();
        controller = generateNewInitializedController(DocumentController.class);
        controller.setId(documentId);
        controller.prepare();
        controller.edit();
        for (int i = 0; i < controller.getCreditProxies().size(); i++) {
            ResourceCreatorProxy proxy = controller.getCreditProxies().get(i);
            assertTrue("proxy person " + proxy.getPerson() + "'s last name should end with " + i,
                    proxy.getPerson().getLastName().endsWith("" + i));
            assertEquals("proxy " + proxy + " sequence number should be i", Integer.valueOf(i),
                    proxy.getResourceCreator().getSequenceNumber());
        }
    }

    @Test
    @Rollback
    // create a simple document, using a pre-existing author with no email
    // address. make sure that we didn't create a new person record.
    public void testForDuplicatePersonWithDifferentInstitution() throws Exception {
        DocumentController controller = initControllerFields();
        // get person record count.
        Person person = new Person();
        person.setFirstName("Pamela");
        person.setLastName("Cressey");
        ResourceCreatorProxy rcp = getNewResourceCreator("Cressey", "Pamela", null, null, ResourceCreatorRole.AUTHOR);
        int expectedPersonCount = genericService.findAll(Person.class).size();

        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies().add(rcp);
        rcp.getPerson().getInstitution().setName("testForDuplicatePersonWithDifferentInstitution");

        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("testing to see if the system created a person record when it shouldn't have");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        Set<Person> findByFullName = entityService.findByFullName("Pamela Cressey");
        logger.debug("people: {} ", findByFullName);
        int actualPersonCount = genericService.findAll(Person.class).size();
        Assert.assertEquals(
                "Person count should not be the same after creating new document with an author that already exists",
                expectedPersonCount + 1, actualPersonCount);
    }

    private Long createDocument(String collectionname, String title) throws TdarActionException {
        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.prepare();
        controller.add();
        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        Document d = controller.getDocument();
        d.setTitle(title);
        d.setDescription("desc");
        d.markUpdated(getUser());
        d.setDate(1234);
        SharedCollection collection = new SharedCollection();
        collection.setName(collectionname);
        controller.getShares().add(collection);

        controller.setServletRequest(getServletPostRequest());
        controller.save();
        return controller.getResource().getId();
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback()
    public void testResourceAdhocCollection() throws Exception {
        String collectionname = "my collection";
        Long newId = createDocument(collectionname, "test 1");
        DocumentController controller = generateNewInitializedController(DocumentController.class, getBasicUser());
        controller.setId(newId);
        controller.prepare();
        controller.edit();
        ResourceCollection collection = controller.getResource().getSharedResourceCollections().iterator().next();
        Long collectionId = collection.getId();
        logger.info("{}", collection);
        Long newId2 = createDocument(collectionname, "test 2");
        ResourceCollection collection2 = controller.getResource().getSharedResourceCollections().iterator().next();
        Long collectionId2 = collection2.getId();
        assertEquals(collectionId, collectionId2);

    }


    @Test
    @Rollback
    public void testUserPermIssUpload() throws Exception {
        // setup document
        TdarUser newUser = createAndSaveNewPerson();
        DocumentController dc = generateNewInitializedController(DocumentController.class, getBasicUser());
        dc.prepare();
        Document doc = dc.getDocument();
        doc.setTitle("test");
        doc.setDate(1234);
        doc.setDescription("my description");
        dc.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, dc.save());

        // change the submitter to the admin
        Long id = doc.getId();
        doc = null;
        evictCache();
        ResourceRightsController rrc = generateNewInitializedController(ResourceRightsController.class, getBasicUser());
        rrc.setId(id);
        rrc.prepare();
        rrc.edit();
        rrc.getProxies().add(new UserRightsProxy(new AuthorizedUser(getBasicUser(), newUser, GeneralPermissions.MODIFY_METADATA)));
        rrc.setServletRequest(getServletPostRequest());
        assertEquals(Action.SUCCESS, rrc.save());
        evictCache();
        genericService.synchronize();
        
        UploadController uc = generateNewInitializedController(UploadController.class, newUser);
        uc.grabTicket();
        Long ticketId = uc.getPersonalFilestoreTicket().getId();
        uc.setTicketId(ticketId);
        uc.getUploadFile().add(new File(TestConstants.TEST_DOCUMENT_DIR, TestConstants.TEST_DOCUMENT_NAME));
        uc.getUploadFileFileName().add(TestConstants.TEST_DOCUMENT_NAME);
        uc.upload();
        
        doc = genericService.find(Document.class, id);
        assertFalse(authenticationAndAuthorizationService.canDo(newUser, doc,
                InternalTdarRights.EDIT_ANY_RESOURCE, GeneralPermissions.ADMINISTER_SHARE));
        assertEquals(2, doc.getAuthorizedUsers().size());
        // try to edit as basic user -- should fail
        doc = null;
        dc = generateNewInitializedController(DocumentController.class, newUser);
        dc.setId(id);
        dc.prepare();
        boolean seenException = false;
        try {
            dc.edit();
            FileProxy fileProxy = new FileProxy();
            fileProxy.setFilename(TestConstants.TEST_DOCUMENT_NAME);
            fileProxy.setAction(FileAction.ADD);
            fileProxy.setRestriction(FileAccessRestriction.CONFIDENTIAL);
            dc.getFileProxies().add(fileProxy);
            dc.setTicketId(ticketId);
            dc.save();
        } catch (TdarActionException e) {
            logger.error("{}",e,e);
            assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(), e.getStatusCode());
            seenException = true;
        }
        assertTrue(seenException);
        // assertNotEmpty(dc.getActionErrors());
        // setIgnoreActionErrors(true);

    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testDocumentReplaceWithInvalidfile() throws TdarActionException {
        setIgnoreActionErrors(true);
        Document document = setupAndLoadResource(TestConstants.TEST_DOCUMENT, Document.class);
        Long documentId = document.getId();
        String filename = "dataset_with_floats_to_varchar.xls";
        Document document2 = replaceFile(TestConstants.TEST_IMAGE_NAME, TestConstants.TEST_DOCUMENT_NAME,
                Document.class, documentId);
        assertTrue(getActionErrors().contains(MessageHelper.getMessage("abstractResourceController.bad_extension")));
    }

    @Test
    @Rollback
    public void testObsoleteSubmission() throws TdarActionException {
        setIgnoreActionErrors(true);
        Document doc = new Document();
        Date date1 = new Date(500L);
        Date date2 = new Date(1000L);
        Date date3 = new Date(1500L);

        doc.setTitle("test rights project");
        doc.setDescription(doc.getTitle());
        doc.markUpdated(getUser());
        doc.setDateCreated(date1);
        doc.setDateUpdated(date3);
        genericService.saveOrUpdate(doc);
        doc.getAuthorizedUsers().add(new AuthorizedUser(getUser(), getUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(doc);
        long docId = doc.getId();
        assertThat(docId, is(not(-1L)));

        DocumentController controller = generateNewInitializedController(DocumentController.class);
        controller.setId(docId);
        controller.setServletRequest(getServletPostRequest());
        controller.prepare();
        // set a time that occurs before date3, which should result in an error
        // because this date is obsolete (startDate must occur on/after
        // lastUpdateDate)
        controller.setStartTime(date2.getTime());
        controller.validate();
        controller.save();

        assertThat(controller.getActionErrors(), hasSize(1));
    }

}
