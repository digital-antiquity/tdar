package org.tdar.struts.action.search;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
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
        controller.setKeywordType("CultureKeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<Keyword> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testSiteNameKeywordLookup() {
        SiteNameKeyword keyword = new SiteNameKeyword();
        keyword.setLabel("18-ST-389");
        genericService.saveOrUpdate(keyword);
        Long id = keyword.getId();
        searchIndexService.indexAll(getAdminUser(), SiteNameKeyword.class);

        controller.setKeywordType("SiteNameKeyword");
        controller.setTerm("18-ST-389");
        controller.lookupKeyword();
        List<Keyword> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
        
        initController();

        controller.setKeywordType("SiteNameKeyword");
        controller.setTerm("18ST389");
        controller.lookupKeyword();
        resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);

    }

}
