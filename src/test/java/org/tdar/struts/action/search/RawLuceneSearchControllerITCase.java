package org.tdar.struts.action.search;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchIndexService;
import org.tdar.struts.action.TdarActionSupport;

@Transactional
public class RawLuceneSearchControllerITCase extends AbstractSearchControllerITCase {

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
    public void testSearchPhraseWithQuote() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(Resource.class);
        doSearch("\"test");
        assertHasErrors();
    }


    @Test
    public void testSearchPhraseWithColon() {
        searchIndexService.indexAll(Resource.class);
        doSearch("test : abc ");
    }

    @Test
    public void testSearchPhraseWithLuceneSyntax() {
        searchIndexService.indexAll(Resource.class);
        doSearch("title:abc");
    }


    @Test
    public void testSearchPhraseWithUnbalancedParenthesis() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(Resource.class);
        doSearch("\"test ( abc ");
    }

    @Test
    public void testAndInSearchPhrase() {
        searchIndexService.indexAll(Resource.class);
        doSearch("Moon AND River");
    }

    @Test
    public void testOrInSearchPhrase() {
        searchIndexService.indexAll(Resource.class);
        doSearch("Moon OR River");
    }


    @Override
    protected void doSearch(String query) {
        controller.setRawQuery(query);
        controller.search();
        logger.info("search (" + controller.getQuery() + ") found: " + controller.getTotalRecords());
    }
    
    protected void assertHasErrors() {
        assertFalse("expecting action errors", controller.getActionErrors().isEmpty());
    }
}
