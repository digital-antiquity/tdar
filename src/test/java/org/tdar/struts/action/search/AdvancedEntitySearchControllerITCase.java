package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;

@Transactional
public class AdvancedEntitySearchControllerITCase extends AbstractSearchControllerITCase {

    
    @Test
    @Rollback
    public void testInstitutionSearch() {
        AdvancedSearchController controller = generateNewController(AdvancedSearchController.class);
        init(controller);
        String term = "arizona";
        controller.setQuery(term);
        String searchInstitutions = controller.searchInstitutions();
        assertResultsOkay(term, controller);
        assertEquals(AbstractLookupController.SUCCESS, searchInstitutions);
    }

    
    @Test
    @Rollback
    public void testPersonSearch() {
        AdvancedSearchController controller = generateNewController(AdvancedSearchController.class);
        init(controller);
        String term = "Manney";
        controller.setQuery(term);
        String searchInstitutions = controller.searchPeople();
        assertResultsOkay(term, controller);
        assertEquals(AbstractLookupController.SUCCESS, searchInstitutions);
    }

    @Test
    @Rollback
    public void testPersonFullNameSearch() {
        AdvancedSearchController controller = generateNewController(AdvancedSearchController.class);
        init(controller);
        String term = "Joshua Watts";
        controller.setQuery(term);
        String searchInstitutions = controller.searchPeople();
        assertResultsOkay(term, controller);
        assertEquals(AbstractLookupController.SUCCESS, searchInstitutions);
    }

    @Test
    @Rollback
    public void testInstitutionMultiWordSearch() {
        AdvancedSearchController controller = generateNewController(AdvancedSearchController.class);
        init(controller);
        String term = "Arizona State";
        controller.setQuery(term);
        String searchInstitutions = controller.searchInstitutions();
        assertResultsOkay(term, controller);
        assertEquals(AbstractLookupController.SUCCESS, searchInstitutions);
    }

    private void assertResultsOkay(String term, AdvancedSearchController controller) {
        assertNotEmpty(controller.getCreatorResults());
        for (Indexable obj : controller.getCreatorResults()) {
            Creator inst = (Creator) obj;
            assertTrue(String.format("Creator %s should match %s",inst, term), inst.getProperName().toLowerCase().contains(term.toLowerCase()));
        }
        logger.info("{}", controller.getResults());
    }

    @Before
    public void before() {
        reindex();
    }
    
    @Override
    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Institution.class, Person.class);
    }
}
