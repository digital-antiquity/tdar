package org.tdar.struts.action.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResultHandler.ProjectionModel;
import org.tdar.search.service.SearchIndexService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionException;

@Transactional
public class PersonSearchControllerITCase extends AbstractControllerITCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    EntityService entityService;

    @Autowired
    private AuthorizationService authenticationAndAuthorizationService;

    private InstitutionSearchAction controller;

    public void AdvancedSearchController() {
        controller = generateNewInitializedController(InstitutionSearchAction.class);
    }

    private void resetController() {
        controller = generateNewInitializedController(InstitutionSearchAction.class);
    }

    @Before
    public void reset() {
        reindex();
        resetController();
        controller.setRecordsPerPage(50);
    }

    public List<Institution> setupInstitutionSearch() throws SolrServerException, IOException {
        ArrayList<Institution> insts = new ArrayList<>();
        String[] names = new String[] { "US Air Force", "Vandenberg Air Force Base", "Air Force Base" };
        for (String name : names) {
            Institution institution = new Institution(name);
            updateAndIndex(institution);
            insts.add(institution);
        }
        return insts;
    }

    @Test
    @Rollback
    public void testInstitutionSearchWordPlacement() throws TdarActionException, SolrServerException, IOException {
        List<Institution> insts = setupInstitutionSearch();
        controller.setQuery("Air Force");
        controller.searchInstitutions();
        assertTrue(CollectionUtils.containsAll(controller.getResults(), insts));

        resetController();
        controller.setQuery("Force");
        controller.searchInstitutions();
        assertTrue(CollectionUtils.containsAll(controller.getResults(), insts));

    }

    @Test
    @Rollback
    public void testInstitutionSearchCaseInsensitive() throws TdarActionException, SolrServerException, IOException {
        List<Institution> insts = setupInstitutionSearch();
        controller.setQuery("air force");
        controller.searchInstitutions();
        assertTrue(CollectionUtils.containsAll(controller.getResults(), insts));
        resetController();
        controller.setQuery("force");
        controller.searchInstitutions();
        assertTrue(CollectionUtils.containsAll(controller.getResults(), insts));
    }

    @Override
    protected void reindex() {
        evictCache();
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Resource.class, Person.class, Institution.class, ResourceCollection.class);
    }

    protected void doSearch() {
        doSearch(false);
    }

    protected void doSearch(Boolean b) {
        controller.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        AbstractSearchControllerITCase.doSearch(controller, LookupSource.RESOURCE, b);
        logger.info("search found: " + controller.getTotalRecords());
    }

    private void updateAndIndex(Indexable doc) throws SolrServerException, IOException {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }

}
