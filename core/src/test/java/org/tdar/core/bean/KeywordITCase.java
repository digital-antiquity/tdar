package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.AuthorityManagementService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.utils.Pair;

public class KeywordITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private AuthorityManagementService authorityManagementService;

    @Test
    public void testFindAllDescendants() {
        CultureKeyword historicKeyword = genericKeywordService.findByLabel(CultureKeyword.class, "Historic");
        assertNotNull(historicKeyword);
        List<CultureKeyword> children = genericKeywordService.findAllDescendants(CultureKeyword.class, historicKeyword);
        assertTrue(!children.isEmpty());
        Map<String, CultureKeyword> map = new HashMap<String, CultureKeyword>();
        for (CultureKeyword child : children) {
            map.put(child.getLabel(), child);
        }
        assertTrue(map.containsKey("Spanish"));

    }

    @Test
    @Rollback
    public void testFindAndReconcilePlurals() {
        createAndAddTK("Rock");
        createAndAddTK("Rocks");
        createAndAddTK("1920");
        createAndAddTK("1920s");

        authorityManagementService.findPluralDups(TemporalKeyword.class, getUser(), true);
    }

    
    @Test
    @Rollback
    public void findByDupTest() {
        TemporalKeyword master = createAndAddTK("Rock");
        TemporalKeyword dup = createAndAddTK("Rocks");
        genericService.saveOrUpdate(master,dup);
        dup.setStatus(Status.DUPLICATE);
        master.getSynonyms().add(dup);
        genericService.saveOrUpdate(dup,master);
        genericService.synchronize();
        assertEquals(master,genericKeywordService.findAuthority(dup));


    }
    
    private TemporalKeyword createAndAddTK(String term) {
        TemporalKeyword tk2 = new TemporalKeyword();
        tk2.setLabel(term);
        genericService.saveOrUpdate(tk2);
        return tk2;
    }

    // make sure that deleting a hierarchical keyword does not implicitly delete it's parent.
    public <K extends HierarchicalKeyword<?>> void assertKeywordChildDeleteNotCascaded(List<K> keywords) {
        // get the first item that has a parent
        HierarchicalKeyword<?> parent = null;
        HierarchicalKeyword<?> child = null;
        for (HierarchicalKeyword<?> keyword : keywords) {
            if (keyword.getParent() != null) {
                parent = keyword.getParent();
                child = keyword;
                break;
            }
        }

        Long parentId = parent.getId();
        logger.debug("deleting child keyword: {}", child);
        genericService.delete(child);

        // try to get the parent back. The deletion of the child should not cascade.
        if (parent instanceof SiteTypeKeyword) {
            HierarchicalKeyword<SiteTypeKeyword> parent2 = genericService.find(SiteTypeKeyword.class, parentId);
            Assert.assertEquals(parent, parent2);
        } else {
            HierarchicalKeyword<CultureKeyword> parent2 = genericService.find(CultureKeyword.class, parentId);
            Assert.assertEquals(parent, parent2);

        }
    }

    @Test
    @Rollback
    public void testHierarchicalKeywordChildDeleteNotCascaded() {
        List<CultureKeyword> cultureKeywords = genericService.findAll(CultureKeyword.class);
        List<SiteTypeKeyword> siteTypeKeywords = genericService.findAll(SiteTypeKeyword.class);
        assertKeywordChildDeleteNotCascaded(cultureKeywords);
        assertKeywordChildDeleteNotCascaded(siteTypeKeywords);
    }

    @Test
    @Rollback
    // just some sanity checks on keyword stats methods
    public void testKeywordStats() {
        // I could use any keyword service here, as I'm just calling base methods...
        testKeywordStats(genericKeywordService.getControlledCultureKeywordStats());
        testKeywordStats(genericKeywordService.getUncontrolledCultureKeywordStats());
        testKeywordStats(genericKeywordService.getGeographicKeywordStats());
        testKeywordStats(genericKeywordService.getInvestigationTypeStats());
        testKeywordStats(genericKeywordService.getOtherKeywordStats());
        testKeywordStats(genericKeywordService.getSiteNameKeywordStats());
        testKeywordStats(genericKeywordService.getControlledSiteTypeKeywordStats());
        // TODO: add some uncontrolled site types to test db.
        // testKeywordStats(genericKeywordService.getUncontrolledSiteTypeKeywordStats());
        testKeywordStats(genericKeywordService.getTemporalKeywordStats());
        testKeywordStats(genericKeywordService.getMaterialKeywordStats());

    }

    private <K extends Keyword> void testKeywordStats(List<Pair<K, Integer>> stats) {
        assertFalse(CollectionUtils.isEmpty(stats));
        if (stats.get(0).getFirst() instanceof HierarchicalKeyword)
        {
            return; // for heirarchical keywords we sort by index then by count
        }
        for (int i = 0; i < (stats.size() - 2); i++) {
            int currentCount = stats.get(i).getSecond();
            int nextCount = stats.get(i + 1).getSecond();
            String msg = String.format("keywordcount for '%s(%s)' should be less than '%s(%s)'", stats.get(i).getFirst(), currentCount, stats.get(i + 1)
                    .getFirst(), nextCount);
            assertTrue(msg, currentCount >= nextCount);
        }
    }

}
