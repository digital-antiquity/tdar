package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.entity.ResourceCreatorProxy;

public class DocumentControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    DocumentController controller;

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    public void initControllerFields() {
        controller.prepare();
        controller.setProjectId(TestConstants.PARENT_PROJECT_ID);
        controller.setResourceAvailability("public");
    }

    @Test
    public void testShowStatuses() {
        DocumentController dc = generateNewController(DocumentController.class);
        init(dc, getUser());
        List<Status> statuses = controller.getStatuses();
        assertFalse(statuses.isEmpty());
    }

    @Test
    @Rollback()
    public void testInstitutionResourceCreatorNew() {
        initControllerFields();
        // create a document with a single resource creator not currently in the
        // database, then save.
        String EXPECTED_INSTITUTION_NAME = "NewBlankInstitution";

        Long originalId = controller.getResource().getId();
        // FIXME: in reality, struts calls the getter, not the setter, but from
        // there I'm not sure how it's populating the elements.
        controller.getAuthorshipProxies().add(getNewResourceCreator(EXPECTED_INSTITUTION_NAME, -1L, ResourceCreatorRole.REPOSITORY));
        controller.getAuthorshipProxies().add(getNewResourceCreator(EXPECTED_INSTITUTION_NAME, -1L, ResourceCreatorRole.CONTRIBUTOR));
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);
        

        d = controller.getResource();
        Assert.assertEquals(d.getInternalResourceCollection(), null);
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
        setHttpServletRequest(getServletPostRequest());
        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);
        controller.setDelete("delete");
        controller.delete();

        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);

        Assert.assertEquals("expecting document status to be deleted", Status.DELETED, controller.getDocument().getStatus());
        Assert.assertEquals("expecting controller status to be deleted", Status.DELETED, controller.getStatus());
    }

    @Test
    @Rollback()
    public void testPersonResourceCreatorNew() {
        initControllerFields();

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies().add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));
        controller.getAuthorshipProxies().add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.EDITOR));
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);

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
        setHttpServletRequest(getServletPostRequest());
        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);
        controller.setDelete("delete");
        controller.delete();
        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);

        Assert.assertEquals("expecting document status to be deleted", Status.DELETED, controller.getDocument().getStatus());
        Assert.assertEquals("expecting controller status to be deleted", Status.DELETED, controller.getStatus());
    }

    // return a populated "new" resource creator person (i.e. all person fields
    // set but null id)
    private ResourceCreatorProxy getNewResourceCreator(String name, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        rcp.getInstitution().setName(name);
        // FIXME: THIS NEEDS TO WORK WITHOUT SETTING AN ID as well as when an ID
        // IS SET
        if (System.currentTimeMillis() % 2 == 0) {
            rcp.getInstitution().setId(-1L);
        }
        rcp.setInstitutionRole(role);
        return rcp;
    }

    private ResourceCreatorProxy getNewResourceCreator(String last, String first, String email, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        Person p = rcp.getPerson();
        rcp.getPerson().setLastName(last);
        rcp.getPerson().setFirstName(first);
        rcp.getPerson().setEmail(email);
        // id may be null
        rcp.getPerson().setId(id);
        Institution inst = new Institution();
        inst.setName("University of TEST");
        p.setInstitution(inst);
        rcp.setPersonRole(role);
        return rcp;
    }

    public void setController(DocumentController controller) {
        this.controller = controller;
    }

    @Test
    @Rollback()
    public void testEditResourceCreators() {
        initControllerFields();

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies().add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.save();
        Long newId = controller.getResource().getId();

        Assert.assertNotNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);

        d = controller.getResource();
        Assert.assertEquals("expecting document IDs to match (save/reloaded)", newId, d.getId());
        Set<ResourceCreator> resourceCreators = d.getResourceCreators();
        Assert.assertTrue(resourceCreators.size() > 0);
        ResourceCreator actualCreator = (ResourceCreator) d.getResourceCreators().toArray()[0];
        Assert.assertNotNull(actualCreator);
        Assert.assertEquals(CreatorType.PERSON, actualCreator.getCreator().getCreatorType());
        Assert.assertTrue(actualCreator.getCreator().getName().contains("newLast"));
        controller.delete(controller.getDocument());

        // FIXME: should add and replace items here to really test

        // FIXME: issues with hydrating resources with Institutions

        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);
        // assert my authorproxies have what i think they should have (rendering
        // edit page)
        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);
        controller.setAuthorshipProxies(new ArrayList<ResourceCreatorProxy>());
        // deleting all authorship resource creators
        controller.save();

        // loading the view page
        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, newId);
        Assert.assertEquals("expecting size zero", 0, controller.getAuthorshipProxies().size());
        logger.debug("{}", controller.getAuthorshipProxies().size());
        Assert.assertTrue("expecting invaled proxy", controller.getAuthorshipProxies().isEmpty());
    }

    @Test
    @Rollback
    // create a simple document, using a pre-existing author with no email address. make sure that we didn't create a new person record.
    public void testForDuplicatePersonWithNoEmail() {
        initControllerFields();
        // get person record count.
        Person person = new Person();
        person.setFirstName("Pamela");
        person.setLastName("Cressey");
        ResourceCreatorProxy rcp = getNewResourceCreator("Cressey", "Pamela", null, null, ResourceCreatorRole.AUTHOR);
        int expectedPersonCount = genericService.findAll(Person.class).size();

        Long originalId = controller.getResource().getId();
        controller.getAuthorshipProxies().add(rcp);
        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("testing to see if the system created a person record when it shouldn't have");
        d.markUpdated(getUser());
        controller.save();
        Long newId = controller.getResource().getId();

        // now reload the document and see if the institution was saved.
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        int actualPersonCount = genericService.findAll(Person.class).size();
        Assert.assertEquals("Person count should be the same after creating new document with an author that already exists", expectedPersonCount,
                actualPersonCount);
    }

    @Test
    @Rollback
    public void testResourceCreatorSortOrder() {
        int numberOfResourceCreators = 20;
        initControllerFields();
        for (int i = 0; i < numberOfResourceCreators; i++) {
            controller.getCreditProxies().add(getNewResourceCreator("Cressey" + i, "Pamela", null, null, ResourceCreatorRole.CONTACT));
        }
        Document d = controller.getDocument();
        d.setTitle("Testing Resource Creator Sort Order");
        d.setDescription("Resource Creator sort order");
        d.markUpdated(getUser());
        controller.save();
        Long documentId = controller.getResource().getId();
        controller = generateNewInitializedController(DocumentController.class);
        loadResourceFromId(controller, documentId);
        for (int i = 0; i < controller.getCreditProxies().size(); i++) {
            ResourceCreatorProxy proxy = controller.getCreditProxies().get(i);
            assertTrue("proxy person " + proxy.getPerson() + "'s last name should end with " + i, proxy.getPerson().getLastName().endsWith("" + i));
            assertEquals("proxy " + proxy + " sequence number should be i", Integer.valueOf(i), proxy.getResourceCreator().getSequenceNumber());
        }
    }

}
