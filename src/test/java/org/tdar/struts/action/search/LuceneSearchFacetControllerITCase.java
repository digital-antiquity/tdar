package org.tdar.struts.action.search;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;

@Transactional
public class LuceneSearchFacetControllerITCase extends AbstractSearchControllerITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;
    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    public TdarActionSupport getController() {
        return controller;
    }

    @Autowired
    SearchIndexService searchIndexService;
    @Autowired
    GenericKeywordService genericKeywordService;

    @Before
    public void reset() {
        reindex();
        controller = generateNewInitializedController(LuceneSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Rollback
    public void testLookupResourceWithDateRegisteredRange() throws InstantiationException, IllegalAccessException {
        // first create two documents with two separate create dates
        Document document1 = createAndSaveNewInformationResource(Document.class);
        document1.setDate(2001);
        genericService.saveOrUpdate(document1);
        searchIndexService.indexAll(Resource.class);

        // okay, lets start with a search that should contain both of our newly created documents
        controller.setDateCreatedMax(2010);
        controller.setDateCreatedMin(2000);

        doSearch();
        assertFalse(controller.getResults().contains(document1));
    }
}
