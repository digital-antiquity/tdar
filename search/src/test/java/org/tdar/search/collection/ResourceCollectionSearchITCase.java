package org.tdar.search.collection;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.HasDisplayProperties;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.utils.MessageHelper;

public class ResourceCollectionSearchITCase extends AbstractCollectionSearchTestCase {

    @Autowired
    CollectionSearchService collectionSearchService;

    public void init() {
        boolean first = true;
        for (String name : collectionNames) {
            SharedCollection collection = new SharedCollection(name, name, SortOption.COLLECTION_TITLE, false, getAdminUser());
            collection.setDescription(name);
            collection.markUpdated(collection.getOwner());
            genericService.saveOrUpdate(collection);
            if (first) {
                collection.setOwner(getAdminUser());
                collection.setHidden(true);
                collection.markUpdated(collection.getOwner());
                collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP));
                genericService.saveOrUpdate(collection.getAuthorizedUsers());
                genericService.saveOrUpdate(collection);
            }
            first = false;
            logger.debug("{} {} {}", collection.getId(), collection.getTitle(), collection.isHidden());
        }
        SharedCollection find = genericService.find(SharedCollection.class, 1003L);
        find.setHidden(false);
        genericService.saveOrUpdate(find);
        reindex();
    }

    @Test
    @Rollback
    public void testCustomStemming() throws ParseException, SolrServerException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.getAllFields().add("Australia");
        SearchResult<ResourceCollection> result = new SearchResult<>();
        collectionSearchService.buildResourceCollectionQuery(getBasicUser(), csqo,  result, MessageHelper.getInstance());
        logger.debug("{}", result.getResults());
        assertNotEmpty(result.getResults());
        assertEquals("should have one result",  1, result.getResults().size());
    }

    @Test
    @Rollback
    public void testBasicCollectionSearch() throws ParseException, SolrServerException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        SearchResult<ResourceCollection> result = runQuery(null, csqo);
        assertNotEmpty(result.getResults());
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
        }
    }

    @Test
    @Rollback
    public void testParents() throws ParseException, SolrServerException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.setLimitToTopLevel(false);
        SearchResult<ResourceCollection> result = runQuery(getAdminUser(), csqo);
        assertNotEmpty(result.getResults());
        boolean seen = false;
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
            if (c.getId().equals(1002L)) {
                seen = true;
            }
        }
        assertTrue("should see 1002 ", seen);

        csqo.setLimitToTopLevel(true);
        result = runQuery(getAdminUser(), csqo);
        assertNotEmpty(result.getResults());

        seen = false;
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
            if (c.getId().equals(1002L) && c instanceof SharedCollection) {
                SharedCollection shared = (SharedCollection)c;
                logger.debug("parent: {}", shared.getParent());
                logger.debug("parent: {}", shared.isTopLevel());
                seen = true;
            }
        }
        assertFalse("should not see 1002 ", seen);
    }

    @Test
    @Rollback
    public void testHiddenBoolean() throws ParseException, SolrServerException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.setIncludeHidden(false);
        SearchResult<ResourceCollection> result = runQuery(getAdminUser(), csqo);
        assertNotEmpty(result.getResults());
        for (ResourceCollection c : result.getResults()) {
            if (c instanceof HasDisplayProperties) {
                logger.debug("{} {} {}", c.getId(), c, ((HasDisplayProperties) c).isHidden());
                assertFalse("should not be hidden", ((HasDisplayProperties) c).isHidden());
            } else {
                fail("should not be indexing InternalCollections");
            }
        }
    }

    @Test
    @Rollback
    public void testBasicCollectionSearchTerms() throws ParseException, SolrServerException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.getAllFields().add("Kintigh");
        csqo.getAllFields().add("KBP");
        SearchResult<ResourceCollection> result = runQuery(null, csqo);
        assertTrue("AND Search should find 0 results", CollectionUtils.isEmpty(result.getResults()));
        csqo.setOperator(Operator.OR);

        result = runQuery(null, csqo);
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), ((HasDisplayProperties) c).getTitle());
            assertTrue("title contains kbp or kintigh", ((HasDisplayProperties) c).getTitle().contains("KBP") || ((HasDisplayProperties) c).getTitle().contains("Kintigh"));
        }
    }

    private SearchResult<ResourceCollection> runQuery(TdarUser user, CollectionSearchQueryObject csqo) throws ParseException, SolrServerException, IOException {
        SearchResult<ResourceCollection> result = new SearchResult<>();
        collectionSearchService.buildResourceCollectionQuery(user, csqo, result, MessageHelper.getInstance());
        return result;
    }
}
