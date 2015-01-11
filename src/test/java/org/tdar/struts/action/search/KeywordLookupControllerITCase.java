package org.tdar.struts.action.search;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.struts.action.lookup.KeywordLookupAction;

public class KeywordLookupControllerITCase extends AbstractIntegrationTestCase {

    private KeywordLookupAction controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(KeywordLookupAction.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    public void testKeywordLookup() {
        searchIndexService.indexAll(getAdminUser(), GeographicKeyword.class, CultureKeyword.class);
        controller.setKeywordType("culturekeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<Keyword> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

}
