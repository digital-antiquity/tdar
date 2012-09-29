package org.tdar.core.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.service.CultureKeywordService;

public class KeywordITCase extends AbstractIntegrationTestCase {

    @Autowired
    CultureKeywordService culturedKeywordService;

    @Test
    public void testFindAllDescendants() {
        CultureKeyword historicKeyword = culturedKeywordService.findByLabel("Historic");
        assertNotNull(historicKeyword);
        List<CultureKeyword> children = culturedKeywordService.findAllDescendants(historicKeyword);
        assertTrue(!children.isEmpty());
        Map<String, CultureKeyword> map = new HashMap<String, CultureKeyword>();
        for (CultureKeyword child : children) {
            map.put(child.getLabel(), child);
        }
        assertTrue(map.containsKey("Spanish"));

    }
}
