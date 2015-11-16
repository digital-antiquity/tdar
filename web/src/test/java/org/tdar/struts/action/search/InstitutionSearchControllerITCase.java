package org.tdar.struts.action.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class InstitutionSearchControllerITCase extends AbstractControllerITCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    EntityService entityService;

    @Autowired
    private AuthorizationService authenticationAndAuthorizationService;

    private PersonSearchAction controller;

    public void AdvancedSearchController() {
        controller = generateNewInitializedController(PersonSearchAction.class);
    }

    private void resetController() {
        controller = generateNewInitializedController(PersonSearchAction.class);
    }

    @Before
    public void reset() {
        reindex();
        resetController();
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback
    public void testPersonRelevancy() throws TdarActionException, SolrServerException, IOException {
        List<Person> people = new ArrayList<>();
        Person whelan = new Person("Mary", "Whelan", null);
        people.add(whelan);
        Person mmc = new Person("Mary", "McCready", null);
        Person mmc2 = new Person("McCready", "Mary", null);
        people.add(mmc);
        people.add(new Person("Doug", "Mary", null));
        people.add(mmc2);
        people.add(new Person("Mary", "Robbins-Wade", null));
        people.add(new Person("Robbins-Wade", "Mary", null));
        for (Person p : people) {
            updateAndIndex(p);
        }
        controller.setQuery("Mary Whelan");
        controller.searchPeople();
        List<Person> results = ((List<Person>) (List<?>) controller.getResults());
        logger.debug("Results: {}", results);
        assertTrue(results.get(0).equals(whelan));
        assertTrue(results.size() == 1);

        resetController();
        controller.setQuery("Mary McCready");
        controller.searchPeople();
        results = ((List<Person>) (List<?>) controller.getResults());
        logger.debug("Results: {}", results);
        assertTrue(results.contains(mmc));
        assertTrue(results.contains(mmc2));
        assertTrue(results.size() == 2);
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

    private void updateAndIndex(Indexable doc) {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }

}
