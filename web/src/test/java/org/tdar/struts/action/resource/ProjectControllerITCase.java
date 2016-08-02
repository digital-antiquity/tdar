package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.search.converter.ResourceRightsExtractor;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.project.ProjectController;
import org.tdar.struts.action.sensoryData.SensoryDataController;

import com.opensymphony.xwork2.Action;

public class ProjectControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    ResourceCollectionService resourceCollectionService;

    @Test
    @Rollback
    public void testProjectJSON() throws IOException, TdarActionException {
        ProjectController controller = generateNewInitializedController(ProjectController.class, getAdminUser());
        controller.setId(3805L);
        controller.prepare();
        RelatedComparativeCollection rcc = new RelatedComparativeCollection();
        rcc.setText("old philadelphia museum");
        controller.getProject().getRelatedComparativeCollections().add(rcc);
        controller.getProject().getResourceNotes().add(new ResourceNote(ResourceNoteType.REDACTION, "redacted"));
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("key23123");
        controller.getProject().getResourceAnnotations().add(new ResourceAnnotation(key, "21234"));
        controller.json();
        String projectAsJson = IOUtils.toString(controller.getJsonInputStream());
        logger.info(projectAsJson);
        assertTrue(projectAsJson.contains("activeCultureKeywords"));
        assertTrue(projectAsJson.contains("Domestic Structure or Architectural Complex"));
        assertTrue(projectAsJson.contains("New Philadelphia"));
        assertTrue(projectAsJson.contains("redacted"));
        assertTrue(projectAsJson.contains("old philadelphia"));
        assertTrue(projectAsJson.contains(ResourceNoteType.REDACTION.name()));
        assertTrue(projectAsJson.contains("21234"));
        assertTrue(projectAsJson.contains("key23123"));
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    @Ignore
    // ignored because bad test with HashCode
    public void testProjectResourceCreator() throws Exception {
        Institution inst = new Institution("da");

        // FIXME: this test is broken becasuse it doesn't account for the logic used in entityService.findOrSavePerson, which will only create 3
        // person records out of the 6 shown here.
        Person test1 = new Person("test", "person", "");
        Person test2 = new Person("test", "person", "test@test.com");
        Person test3 = new Person("test", "person", "");
        test3.setInstitution(inst);

        Person test4 = new Person("test", "person", "test@test.com");
        test4.setInstitution(inst);

        Person test5 = new Person("test", "person", null);
        Person test6 = new Person("Test", "Person", "");

        Set<Person> personSet = new HashSet<Person>(Arrays.asList(test1, test2, test3, test4, test5, test6));
        int expectedSize = personSet.size();

        ProjectController controller = generateNewInitializedController(ProjectController.class);
        controller.setAsync(false);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());

        List<ResourceCreatorProxy> creditProxies = new ArrayList<ResourceCreatorProxy>();
        creditProxies.add(new ResourceCreatorProxy(test1, ResourceCreatorRole.CONTACT)); // simple person, no institution
        creditProxies.add(new ResourceCreatorProxy(test3, ResourceCreatorRole.CONTACT)); // person1 with institution
        creditProxies.add(new ResourceCreatorProxy(test4, ResourceCreatorRole.CONTACT)); // person1 but with email & institution
        creditProxies.add(new ResourceCreatorProxy(test5, ResourceCreatorRole.CONTACT)); // person1 but with a null email instead of blank
        creditProxies.add(new ResourceCreatorProxy(test2, ResourceCreatorRole.CONTACT)); // person1 with email, no institution
        creditProxies.add(new ResourceCreatorProxy(test6, ResourceCreatorRole.CONTACT)); // person1 with case differences

        controller.setCreditProxies(creditProxies);
        controller.getProject().setTitle("test");
        controller.getProject().setDescription("test");
        controller.setAsync(false);
        controller.save();

        Project project = controller.getProject();
        project = genericService.merge(project);
        List<Creator<?>> people = new ArrayList<>();
        for (ResourceCreator creator : project.getResourceCreators()) {
            logger.info("{}", creator);
            people.add(creator.getCreator());
        }
        assertEquals("if resource and role are constant, resourceCreator set and person set should be same size", personSet.size(), people.size());
        assertEquals("First and Second person should be the same (difference institution)", people.get(0), people.get(1));
        assertEquals("First and Sixth person should be the same (difference case)", people.get(0), people.get(5));
        assertEquals("First and Fourth person should be the same (email null)", people.get(0), people.get(1));
        assertNotEquals("First and Third should not be the same as there's an email", people.get(0), people.get(2));
        assertEquals("Third and Fifth should be the same as there's the same email but one has institution", people.get(2), people.get(4));
    }

    @Test
    @Rollback
    public void testProjectRightsInheritance() throws InstantiationException, IllegalAccessException {
        // create a project w/ one user who has the ability to update it.
        AuthorizedUser user = new AuthorizedUser(getBasicUser(), GeneralPermissions.MODIFY_RECORD);
        Project project = new Project();
        project.setTitle("test");
        project.setDescription("test");
        project.markUpdated(getBasicUser());
        genericService.save(project);
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(user);
        resourceCollectionService.saveAuthorizedUsersForResource(project, users, true, getBasicUser());
        project.setSubmitter(getAdminUser());
        genericService.saveOrUpdate(project);
        // ensure that basicUser can edit the project
        Long id = project.getId();
        project = null;
        Project project_ = genericService.find(Project.class, id);
        ResourceRightsExtractor extractor = new ResourceRightsExtractor(project_);
        assertTrue(extractor.getUsersWhoCanModify().contains(getBasicUser().getId()));
        assertNotNull(project_.getInternalResourceCollection());
        Set<AuthorizedUser> authorizedUsers = project_.getInternalResourceCollection().getAuthorizedUsers();

        // ensure that the project has only one authorizedUser, and that it is basicUser
        assertEquals(1, authorizedUsers.size());
        assertTrue(authorizedUsers.iterator().next().getUser().equals(getBasicUser()));

        // create a document and associate it w/ the project we just made
        InformationResource testResource = generateDocumentWithUser();
        testResource.setProject(project_);
        genericService.saveOrUpdate(testResource);
        assertEquals(getBasicUser(), testResource.getSubmitter());
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), testResource, GeneralPermissions.MODIFY_METADATA));

        // create a new collection with an owner and three users that have each permission type (view, modify, admin)
        ResourceCollection testCollection = new ResourceCollection(CollectionType.SHARED);
        TdarUser testModify = createAndSaveNewPerson("a@b", "1234");
        TdarUser testOwner = createAndSaveNewPerson("a@b1", "12341");
        TdarUser testView = createAndSaveNewPerson("a@b2", "12341");
        TdarUser testAdmin = createAndSaveNewPerson("a@b3", "12341");
        testCollection.markUpdated(testOwner);
        testCollection.setName("test name");
        genericService.save(testCollection);

        // add the resourceCollection to the project (and vice/versa)
        project_.getResourceCollections().add(testCollection);
        genericService.saveOrUpdate(testCollection);
        genericService.saveOrUpdate(project_);
        List<AuthorizedUser> users2 = new ArrayList<AuthorizedUser>();
        users2.addAll(Arrays.asList(new AuthorizedUser(testModify, GeneralPermissions.MODIFY_RECORD), new AuthorizedUser(testView,
                GeneralPermissions.VIEW_ALL),
                new AuthorizedUser(testAdmin, GeneralPermissions.ADMINISTER_GROUP)));
        resourceCollectionService.saveAuthorizedUsersForResourceCollection(project_, testCollection, users, true, getBasicUser());
        genericService.saveOrUpdate(testCollection);

        logger.info("u:{}, r:{}", testModify.getId(), testResource.getId());
        logger.info("rc:{}", project_.getResourceCollections());
        assertFalse(authenticationAndAuthorizationService.canEditResource(testOwner, testResource, GeneralPermissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testView, testResource, GeneralPermissions.MODIFY_METADATA));

        // THESE NEXT TESTS SHOULD BE TRUE IF INHERITANCE IS TURNED BACK ON FROM PROJECT -> RESOURCE
        assertFalse(authenticationAndAuthorizationService.canEditResource(testModify, testResource, GeneralPermissions.MODIFY_METADATA));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testAdmin, testResource, GeneralPermissions.MODIFY_METADATA));
    }

    @Test
    @Rollback
    public void testPotentialParents() {
        SensoryDataController controller = generateNewController(SensoryDataController.class);
        init(controller, getBasicUser());
        List<Resource> potentialParents = controller.getPotentialParents();
        logger.info("potential parents: {}", potentialParents);
        assertTrue("potential parents should always at least have one item (the null project)", potentialParents.size() >= 1);
        // first element should always be the null project
        assertTrue(-1L == potentialParents.get(0).getId());
        assertEquals(getText("project.no_associated_project"), potentialParents.get(0).getTitle());

        int originalParentCount = potentialParents.size();
        createAndSaveNewProject("potential parent project one");
        createAndSaveNewProject("potential paernt project two");
        controller = generateNewController(SensoryDataController.class);
        init(controller, getBasicUser());
        potentialParents = controller.getPotentialParents();
        int newParentCount = potentialParents.size();

        assertEquals(2, newParentCount - originalParentCount);

        controller = generateNewController(SensoryDataController.class);
        init(controller, getAdminUser());
        potentialParents = controller.getPotentialParents();
        logger.info("{}", potentialParents);
        assertTrue(potentialParents.size() > 2);
        assertTrue(-1L == potentialParents.get(0).getId());
        assertEquals(getText("project.no_associated_project"), potentialParents.get(0).getTitle());
    }

    @Test
    // create a new project and add it to a collection
    @Rollback
    public void testAddingToExistingCollection() throws Exception {
        ResourceCollection rc = createNewEmptyCollection("testing adding a to collection from resource edit page");
        setIgnoreActionErrors(true);
        evictCache();
        assertNotNull(rc);

        // try and fail due to rights
        ProjectController controller = tryAndSaveCollectionToController(rc);
        assertNotEquals(Action.SUCCESS, controller.save());

        rc.getAuthorizedUsers().add(new AuthorizedUser(getUser(), GeneralPermissions.ADMINISTER_GROUP));
        genericService.saveOrUpdate(rc);
        evictCache();
        // try ... and should succeed now that we add the user + permissions
        controller = tryAndSaveCollectionToController(rc);
        assertEquals(Action.SUCCESS, controller.save());

        assertNotNull(controller.getProject());
        Long id = controller.getProject().getId();
        assertTrue("project should have been saved", (id != null) && (id != -1L));
        logger.info("HI!!! {}", id);

        Project loadedProject = genericService.find(Project.class, id);
        logger.info("{}", loadedProject);
        // confirm that the controller added the list of resource collections to the project
        assertTrue("collection list shouldn't be empty", loadedProject.getResourceCollections().size() > 0);

        logger.debug("resource collection id:{}\t  {}", rc.getId(), rc);
        evictCache();
    }

    private ProjectController tryAndSaveCollectionToController(ResourceCollection rc) throws TdarActionException {
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        init(controller, getUser());
        controller.setAsync(false);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        // controller.edit();
        Project project = controller.getProject();
        project.setTitle("testing adding collection");
        project.setDescription("test");
        // controller expects incoming list of resource collections to be detached, so lets create one
        ResourceCollection detachedCollection = new ResourceCollection(rc.getType());
        detachedCollection.setName(rc.getName());
        detachedCollection.setId(rc.getId());
        detachedCollection.setType(CollectionType.SHARED);
        detachedCollection.setHidden(false);
        detachedCollection.setSortBy(SortOption.RELEVANCE);
        detachedCollection.markUpdated(rc.getOwner());
        controller.getResourceCollections().add(detachedCollection);
        assertNotEquals(getUser(), rc.getOwner());
        assertNotNull(detachedCollection.getOwner());
        assertTrue(detachedCollection.isValid());
        controller.validate();
        return controller;
    }

    @Test
    @Rollback
    // create a new project w/ ad hoc resourceCollecion
    public void testAddProjectToAdHocCollection() throws Exception {
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        init(controller, getUser());
        controller.setAsync(false);
        controller.prepare();

        Project project = controller.getProject();
        project.setTitle("testing adhoc collection creation");
        project.setDescription("test");

        String name1 = "testing adhoc collection creation";
        String name2 = "yet another collection";

        ResourceCollection collection = new ResourceCollection();
        collection.setName(name1);
        controller.getResourceCollections().add(collection);

        collection = new ResourceCollection();
        collection.setName(name2);
        controller.getResourceCollections().add(collection);

        assertUniqueCollections(controller.getResourceCollections(), name1, name2);
        controller.setServletRequest(getServletPostRequest());
        String result = controller.save();
        assertEquals(Action.SUCCESS, result);
        Long id = project.getId();
        assertFalse(project.isTransient());
        Project loadedProject = genericService.find(Project.class, id);
        assertNotNull(loadedProject);
        assertUniqueCollections(loadedProject.getResourceCollections(), name1, name2);

    }

    private void assertUniqueCollections(Collection<ResourceCollection> resourceCollections, String name1, String name2) {
        // the collections should appear in the list, though we aren't sure of the order.
        ArrayList<String> names = new ArrayList<String>();
        for (ResourceCollection rc : resourceCollections) {
            names.add(rc.getName());
        }

        assertTrue(names.contains(name1));
        assertTrue(names.contains(name2));

    }

    private ResourceCollection createNewEmptyCollection(String name) {
        ResourceCollection rc = new ResourceCollection(CollectionType.SHARED);
        Date date = new Date();
        TdarUser owner = new TdarUser("bob", "loblaw", "createNewEmptyCollection" + date.getTime() + "@tdar.net");
        genericService.save(owner);
        rc.markUpdated(owner);
        rc.setName(name);
        rc.setSortBy(SortOption.RELEVANCE);
        assertTrue(rc.isValid());
        genericService.saveOrUpdate(rc);
        return rc;
    }

}
