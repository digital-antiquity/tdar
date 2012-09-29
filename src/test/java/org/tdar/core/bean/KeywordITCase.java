package org.tdar.core.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.keyword.CultureKeywordService;
import org.tdar.core.service.keyword.SiteTypeKeywordService;

public class KeywordITCase extends AbstractIntegrationTestCase {

    @Autowired
    private CultureKeywordService cultureKeywordService;
    
    @Autowired
    private SiteTypeKeywordService siteTypeKeywordService;
    
    @Autowired 
    private GenericService genericService;

    @Test
    public void testFindAllDescendants() {
        CultureKeyword historicKeyword = cultureKeywordService.findByLabel("Historic");
        assertNotNull(historicKeyword);
        List<CultureKeyword> children = cultureKeywordService.findAllDescendants(historicKeyword);
        assertTrue(!children.isEmpty());
        Map<String, CultureKeyword> map = new HashMap<String, CultureKeyword>();
        for (CultureKeyword child : children) {
            map.put(child.getLabel(), child);
        }
        assertTrue(map.containsKey("Spanish"));

    }
    
    //make sure that deleting a hierarchical keyword does not implicitly delete it's parent.  
    public <K extends HierarchicalKeyword<?>> void assertKeywordChildDeleteNotCascaded(List<K> keywords) {
        //get the first item that has a parent
        HierarchicalKeyword<?> parent = null;
        HierarchicalKeyword<?> child = null;
        for(HierarchicalKeyword<?> keyword : keywords) {
            if(keyword.getParent() != null) {
                parent = keyword.getParent();
                child = keyword;
               break;
            }
        }
        
        Long parentId = parent.getId();
        logger.debug("deleting child keyword: {}", child);
        genericService.delete(child);
        
        //try to get the parent back. The deletion of the child should not cascade.
        if(parent instanceof SiteTypeKeyword) {
            HierarchicalKeyword<SiteTypeKeyword> parent2 = siteTypeKeywordService.find(parentId);
            Assert.assertEquals(parent, parent2);
        } else {
            HierarchicalKeyword<CultureKeyword> parent2 = cultureKeywordService.find(parentId);
            Assert.assertEquals(parent, parent2);
            
        }
    }
    
    @Test
    @Rollback
    public void testHierarchicalKeywordChildDeleteNotCascaded() {
        List<CultureKeyword> cultureKeywords = cultureKeywordService.findAll();
        List<SiteTypeKeyword> siteTypeKeywords = siteTypeKeywordService.findAll();
        assertKeywordChildDeleteNotCascaded(cultureKeywords);
        assertKeywordChildDeleteNotCascaded(siteTypeKeywords);
    }
}
