package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.browse.BrowseCreatorController;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Action;

public class BrowseControllerITCase extends AbstractSearchControllerITCase {

    private BrowseCreatorController controller = null;
    
    private final transient Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void initController() {
        controller = generateNewInitializedController(BrowseCreatorController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    @Rollback
    public void testBrowsePersonWithResults() throws Exception {
        testBrowseController(getAdminUser());
    }

    @Test
    @Rollback
    public void testBrowsePersonHiddenWithResults() throws Exception {
        genericService.saveOrUpdate(getBasicUser());
        testBrowseController(getBasicUser());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testBrowsePersonHiddenWithout() throws InstantiationException, IllegalAccessException, ParseException, TdarActionException {
        TdarUser person = new TdarUser();
        person.setFirstName("test");
        person.setLastName("test");
        person.markUpdated(getAdminUser());
        person.setBrowseOccurrence(0L);
        genericService.saveOrUpdate(person);
        genericService.synchronize();
        testFailed(person);

        person.setBrowseOccurrence(100L);
        person.setHidden(true);
        genericService.saveOrUpdate(person);
        genericService.synchronize();
        testFailed(person);
    }

    private void testFailed(TdarUser person) {
        boolean expectedException = false;
        init(controller, null);
        controller = generateNewController(BrowseCreatorController.class);
        controller.setId(person.getId());
        try {
            controller.setSlug(person.getSlug());
            controller.prepare();
            assertEquals(Action.SUCCESS, controller.browseCreators());
        } catch (Exception ex) {
            expectedException = true;
        }
        assertTrue("Exception expected but not found", expectedException);
    }

    @Test
    @Rollback
    public void testBrowseInstitutionWithResults() throws Exception {
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
        controller.setSlug(institution.getSlug());
        controller.prepare();
        controller.browseCreators();
        List<Resource> results = controller.getResults();
        assertTrue(results.contains(doc));
    }

    @Test
    @Rollback
    public void testNewCreatorHasNoResourceAssociations() throws ParseException, TdarActionException {
        Creator<?> creator = createAndSaveNewPerson("testNewPersonHasNoResourceAssociations@tdar.org", "");
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

    private void testBrowseController(Creator<?> creator) throws Exception {
        Document doc = genericService.find(Document.class, setupDatedDocument());
        ResourceCreator rc = new ResourceCreator(creator, ResourceCreatorRole.AUTHOR);
        assertTrue(rc.isValidForResource(doc));
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(rc);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        controller.setId(creator.getId());
        controller.setSlug(creator.getSlug());
        controller.prepare();
        assertEquals(Action.SUCCESS, controller.browseCreators());
        assertEquals(creator, controller.getCreator());
        log.info("{}", controller.getResults());
        assertTrue(controller.getResults().size() > 0);
    }
}
