package org.tdar.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;
import org.tdar.utils.StringPair;

public class ResourceAnnotationSearchITCase extends AbstractResourceSearchITCase {

    private static final String _18ST659_158 = "18ST659/158";
    private static final String _18ST659_143 = "18ST659/143";

    private static final String MAC_LAB_LOT_NUMBER = "MAC Lab Lot Number";

    @Test
    @Rollback
    public void testResourceAnnotationSearch() throws SearchIndexException, IOException, SearchException {
        Document document = createDocumentWithAnnotationKey(_18ST659_143);
        SearchParameters sp = new SearchParameters();
        sp.getAnnotations().add(new StringPair(MAC_LAB_LOT_NUMBER, _18ST659_143));
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.getObjectTypes().clear(); // select all resource types
        SearchResult<Resource> result = doSearch(null, null, sp, null);
        int resourceCount = 0;
        for (Indexable resource : result.getResults()) {
            if (resource instanceof InformationResource) {
                resourceCount++;
            }
        }
        assertTrue("search should have at least 1 result", resourceCount > 0);

    }

    @Test
    @Rollback
    public void testResourceAnnotationKeywordSearch() throws SearchException, SearchIndexException, IOException, ParseException {
        String code = _18ST659_158;
        Document doc = createDocumentWithAnnotationKey(code);
        SearchResult<Resource> result = doSearch(code, null, null, null);
        assertFalse("we should get back at least one hit", result.getResults().isEmpty());
        assertTrue(result.getResults().contains(doc));
    }

    private Document createDocumentWithAnnotationKey(String code) throws SearchIndexException, IOException {
        Document doc = createAndSaveNewResource(Document.class);
        ResourceAnnotationKey key = new ResourceAnnotationKey(MAC_LAB_LOT_NUMBER);
        genericService.saveOrUpdate(key);
        ResourceAnnotation ann = new ResourceAnnotation(key, code);
        ResourceAnnotation ann2 = new ResourceAnnotation(key, _18ST659_143);
        doc.getResourceAnnotations().add(ann);
        doc.getResourceAnnotations().add(ann2);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        return doc;
    }

}
