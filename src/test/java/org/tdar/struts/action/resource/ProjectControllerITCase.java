package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.ResourceCreatorProxy;

public class ProjectControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    ResourceCollectionService resourceCollectionService;

    @Test
    @Rollback
    public void testProjectJSON() {
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        controller.setId(3805L);
        controller.prepare();
        RelatedComparativeCollection rcc = new RelatedComparativeCollection();
        rcc.setText("old philadelphia museum");
        controller.getProject().getRelatedComparativeCollections().add(rcc);
        controller.getProject().getResourceNotes().add(new ResourceNote(ResourceNoteType.REDACTION, "redacted"));
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("key23123");
        controller.getProject().getResourceAnnotations().add(new ResourceAnnotation(key, "21234"));
        String projectAsJson = controller.getProjectAsJson();
        logger.info(projectAsJson);
        assertTrue(projectAsJson.contains("approved"));
        assertTrue(projectAsJson.contains("Domestic Structure or Architectural Complex"));
        assertTrue(projectAsJson.contains("New Philadelphia"));
        assertTrue(projectAsJson.contains("redacted"));
        assertTrue(projectAsJson.contains("old philadelphia"));
        assertTrue(projectAsJson.contains(ResourceNoteType.REDACTION.name()));
        assertTrue(projectAsJson.contains("21234"));
        assertTrue(projectAsJson.contains("key23123"));
    }

    @Test
    @Rollback
    public void testProjectResourceCreator() throws Exception {
        Person test1 = new Person("test", "person", "");
        Person test5 = new Person("test", "person", null);
        Person test2 = new Person("test", "person", "test@test.com");
        Person test3 = new Person("test", "person", "");
        Person test6 = new Person("Test", "Person", "");
        Institution inst = new Institution("da");
        test3.setInstitution(inst);
        Person test4 = new Person("test", "person", "test@test.com");
        test4.setInstitution(inst);
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        controller.prepare();
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
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Project project = controller.getProject();
        project = genericService.merge(project);
        List<Creator> people = new ArrayList<Creator>();
        for (ResourceCreator creator : project.getResourceCreators()) {
            logger.info(creator + " " + creator.getCreator().getId());
            people.add(creator.getCreator());
        }
        assertEquals("First and Second person should be the same (difference institution)", people.get(0), people.get(1));
        assertEquals("First and Sixth person should be the same (difference case)", people.get(0), people.get(5));
        assertEquals("First and Fourth person should be the same (email null)", people.get(0), people.get(1));
        assertFalse("First and Third should not be the same as there's an email", people.get(0).equals(people.get(2)));
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
        project.markUpdated(getAdminUser());
        genericService.save(project);
        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();
        users.add(user);
        resourceCollectionService.saveAuthorizedUsersForResource(project, users, true);

        // ensure that basicUser can edit the project
        Long id = project.getId();
        project = null;
        Project project_ = genericService.find(Project.class, id);
        assertTrue(project_.getUsersWhoCanModify().contains(getBasicUser().getId()));
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
        assertTrue(authenticationAndAuthorizationService.canEditResource(getBasicUser(), testResource));

        // create a new collection with an owner and three users that have each permission type (view, modify, admin)
        ResourceCollection testCollection = new ResourceCollection(CollectionType.SHARED);
        Person testModify = createAndSaveNewPerson("a@b", "1234");
        Person testOwner = createAndSaveNewPerson("a@b1", "12341");
        Person testView = createAndSaveNewPerson("a@b2", "12341");
        Person testAdmin = createAndSaveNewPerson("a@b3", "12341");
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
        resourceCollectionService.saveAuthorizedUsersForResourceCollection(testCollection, users, true);
        genericService.saveOrUpdate(testCollection);

        logger.info("u:{}, r:{}", testModify.getId(), testResource.getId());
        logger.info("rc:{}", project_.getResourceCollections());
        assertFalse(authenticationAndAuthorizationService.canEditResource(testOwner, testResource));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testView, testResource));

        // THESE NEXT TESTS SHOULD BE TRUE IF INHERITANCE IS TURNED BACK ON FROM PROJECT -> RESOURCE
        assertFalse(authenticationAndAuthorizationService.canEditResource(testModify, testResource));
        assertFalse(authenticationAndAuthorizationService.canEditResource(testAdmin, testResource));
    }

    @Test
    @Rollback
    public void testCollectionRightsToProjectAndTheirChildren() {

    }

    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
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
        assertEquals(Project.NULL, potentialParents.get(0));

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
        assertEquals(Project.NULL, potentialParents.get(0));
    }

    @Test
    // create a new project and add it to a collection
    @Rollback
    public void testAddingToExistingCollection() throws Exception {
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        init(controller, getUser());
        controller.prepare();
        // controller.edit();
        Project project = controller.getProject();
        project.setTitle("testing adding collection");
        project.setDescription("test");

        ResourceCollection rc = createNewEmptyCollection("testing adding a to collection from resource edit page");
        genericService.synchronize();
        assertNotNull(rc);

        // controller expects incoming list of resource collections to be detached, so lets create one
        ResourceCollection detachedCollection = new ResourceCollection(rc.getType());
        // the controller only cares about the ID and the NAME.
        // logger.info("{}",rc.getOwner());
        detachedCollection.setName(rc.getName());
        detachedCollection.setId(rc.getId());
        detachedCollection.setType(CollectionType.SHARED);
        detachedCollection.setVisible(true);
        detachedCollection.setSortBy(SortOption.RELEVANCE);
        detachedCollection.markUpdated(rc.getOwner());
        // logger.info("{}",detachedCollection.getOwner());
        controller.getResourceCollections().add(detachedCollection);
        assertNotNull(detachedCollection.getOwner());
        assertTrue(detachedCollection.isValid());
        controller.validate();
        controller.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.SUCCESS, controller.save());
        Long id = project.getId();
        assertTrue("project should have been saved", id != null && id != -1L);
        logger.info("HI!!! {}", id);

        assertNotNull(project);
        Project loadedProject = genericService.find(Project.class, id);
        logger.info("{}", loadedProject);
        // confirm that the controller added the list of resource collections to the project
        assertTrue("collection list shouldn't be empty", loadedProject.getResourceCollections().size() > 0);

        logger.debug("resource collection id:{}\t  {}", rc.getId(), rc);
        genericService.synchronize();
    }

    @Test
    @Rollback
    // create a new project w/ ad hoc resourceCollecion
    public void testAddProjectToAdHocCollection() throws Exception {
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        init(controller, getUser());
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

        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = project.getId();
        assertFalse(project.isTransient());
        Project loadedProject = genericService.find(Project.class, id);
        assertNotNull(loadedProject);

        // the collections should appear in the list, though we aren't sure of the order.
        ArrayList<String> names = new ArrayList<String>();
        for (ResourceCollection rc : loadedProject.getResourceCollections()) {
            names.add(rc.getName());
        }

        assertTrue(names.contains(name1));
        assertTrue(names.contains(name2));
    }

    private ResourceCollection createNewEmptyCollection(String name) {
        ResourceCollection rc = new ResourceCollection(CollectionType.SHARED);
        Date date = new Date();
        Person owner = new Person("bob", "loblaw", "createNewEmptyCollection" + date.getTime() + "@mailinator.com");
        genericService.save(owner);
        rc.markUpdated(owner);
        rc.setName(name);
        rc.setSortBy(SortOption.RELEVANCE);
        assertTrue(rc.isValid());
        genericService.saveOrUpdate(rc);
        return rc;
    }

}
