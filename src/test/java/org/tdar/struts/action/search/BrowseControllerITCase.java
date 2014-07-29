package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.Action;

public class BrowseControllerITCase extends AbstractSearchControllerITCase {

    @Autowired
    private BrowseController controller;
    private Logger log = Logger.getLogger(getClass());

    @Before
    public void initController() {
        controller = generateNewInitializedController(BrowseController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    @Rollback
    public void testBrowsePersonWithResults() throws InstantiationException, IllegalAccessException, ParseException, TdarActionException {
        testBrowseController(getAdminUser());
    }

    @Test
    @Rollback
    public void testBrowsePersonHiddenWithResults() throws InstantiationException, IllegalAccessException, ParseException, TdarActionException {
        getBasicUser().setHiddenIfNotCited(true);
        getBasicUser().setOccurrence(0L);
        genericService.saveOrUpdate(getBasicUser());
        testBrowseController(getBasicUser());
    }

    @Test
    @Rollback
    public void testBrowsePersonHiddenWithout() throws InstantiationException, IllegalAccessException, ParseException, TdarActionException {
        TdarUser person = new TdarUser();
        person.setFirstName("test");
        person.setLastName("test");
        person.markUpdated(getAdminUser());
        person.setHiddenIfNotCited(true);
        person.setOccurrence(0L);
        genericService.saveOrUpdate(person);
        genericService.synchronize();
        boolean expectedException = false;
        controller.setId(person.getId());
        try {
            assertEquals(Action.SUCCESS, controller.browseCreators());
        } catch (TdarActionException ex) {
            expectedException = true;
        }
        assertTrue("Exception expected but not found", expectedException);
    }

    @Test
    @Rollback
    public void testBrowseInstitutionWithResults() throws InstantiationException, IllegalAccessException, ParseException, TdarActionException {
        Institution institution = new Institution("testBrowseControllerInstitution");
        genericService.save(institution);
        testBrowseController(institution);
    }

    @Test
    @Rollback
    public void testBrowseInstitutionWithResultsViaResourceProvider() throws Exception {
        Institution institution = new Institution("testBrowseControllerInstitution");
        genericService.save(institution);
        Document doc = genericService.find(Document.class, setupDatedDocument());
        doc.setResourceProviderInstitution(institution);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        controller.setId(institution.getId());
        controller.browseCreators();
        List<Resource> results = controller.getResults();
        assertTrue(results.contains(doc));
    }

    @Test
    @Rollback
    public void testNewCreatorHasNoResourceAssociations() throws ParseException, TdarActionException {
        Creator creator = createAndSaveNewPerson("testNewPersonHasNoResourceAssociations@tdar.org", "");
        controller.setId(creator.getId());
        controller.browseCreators();
        assertEquals(0, controller.getResults().size());

        initController();
        creator = new Institution("testNewCreatorHasNoResourceAssociations");
        genericService.save(creator);
        controller.setId(creator.getId());
        controller.browseCreators();
        assertEquals(0, controller.getResults().size());

    }

    @Override
    public TdarUser getSessionUser() {
        return getBasicUser();
    }

    private void testBrowseController(Creator creator) throws InstantiationException, IllegalAccessException, ParseException, TdarActionException {
        Document doc = genericService.find(Document.class, setupDatedDocument());
        ResourceCreator rc = new ResourceCreator(creator, ResourceCreatorRole.CONTRIBUTOR);
        assertTrue(rc.isValidForResource(doc));
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(rc);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        controller.setId(creator.getId());
        assertEquals(Action.SUCCESS, controller.browseCreators());
        assertEquals(creator, controller.getCreator());
        log.info(controller.getResults());
        assertTrue(controller.getResults().size() > 0);
    }
}
