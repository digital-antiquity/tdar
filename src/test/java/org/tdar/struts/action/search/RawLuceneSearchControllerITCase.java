package org.tdar.struts.action.search;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
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
        controller = generateNewInitializedController(AdvancedSearchController.class);
        controller.setRecordsPerPage(50);
    }

    @Test
    @Ignore("Not currently implemented")
    public void testSearchPhraseWithQuote() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("\"test");
        assertHasErrors();
    }


    @Test
    @Ignore("Not currently implemented")
    public void testSearchPhraseWithColon() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("test : abc ");
    }

    @Test
    @Ignore("Not currently implemented")
    public void testSearchPhraseWithLuceneSyntax() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("title:abc");
    }


    @Test
    @Ignore("Not currently implemented")
    public void testSearchPhraseWithUnbalancedParenthesis() {
        setIgnoreActionErrors(true);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("\"test ( abc ");
    }

    @Test
    @Ignore("Not currently implemented")
    public void testAndInSearchPhrase() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        doSearch("Moon AND River");
    }

    @Test
    @Ignore("Not currently implemented")
    public void testOrInSearchPhrase() {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
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
