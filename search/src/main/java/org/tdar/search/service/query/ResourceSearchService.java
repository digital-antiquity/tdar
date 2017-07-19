package org.tdar.search.service.query;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.facet.FacetedResultHandler;

import com.opensymphony.xwork2.TextProvider;

public interface ResourceSearchService {

    LuceneSearchResultHandler<Resource> buildCollectionResourceSearch(LuceneSearchResultHandler<Resource> result, TextProvider provider)
            throws SearchException, IOException;

    /**
     * Shared logic to find all direct children of container resource (ResourceCollections and Projects)
     *
     * @param fieldName
     * @param indexable
     * @param user
     * @return
     * @throws IOException
     * @throws SolrServerException
     * @throws ParseException
     */
    LuceneSearchResultHandler<Resource> buildResourceContainedInSearch(Project indexable, String term, TdarUser user,
            LuceneSearchResultHandler<Resource> result, TextProvider provider) throws SearchException, IOException;

    /**
     * Shared logic to find all direct children of container resource (ResourceCollections and Projects)
     *
     * @param fieldName
     * @param indexable
     * @param user
     * @return
     * @throws IOException
     * @throws SolrServerException
     * @throws ParseException
     */
    LuceneSearchResultHandler<Resource> buildResourceContainedInSearch(VisibleCollection indexable, String term, TdarUser user,
            LuceneSearchResultHandler<Resource> result, TextProvider provider) throws SearchException, IOException;

    LuceneSearchResultHandler<Resource> lookupResource(TdarUser user, ResourceLookupObject look, LuceneSearchResultHandler<Resource> result,
            TextProvider support) throws SearchException, IOException;

    LuceneSearchResultHandler<Resource> buildKeywordQuery(Keyword keyword, KeywordType keywordType, ReservedSearchParameters rsp,
            LuceneSearchResultHandler<Resource> result,
            TextProvider provider, TdarUser user) throws SearchException, IOException;

    LuceneSearchResultHandler<Resource> buildAdvancedSearch(AdvancedSearchQueryObject asqo, TdarUser authenticatedUser,
            LuceneSearchResultHandler<Resource> result, TextProvider provider) throws SearchException, IOException;

    /**
     * Take any of the @link SearchParameter properties that can support skeleton resources and inflate them so we can display something in the search title /
     * description that isn't just creatorId=4
     *
     * @param searchParameters
     */
    void inflateSearchParameters(SearchParameters searchParameters);

    /**
     * Generates a query for resources created by or releated to in some way to a @link Creator given a creator and a user
     *
     * @param creator
     * @param user
     * @return
     * @throws IOException
     * @throws SolrServerException
     * @throws ParseException
     */
    LuceneSearchResultHandler<Resource> generateQueryForRelatedResources(Creator<?> creator, TdarUser user, FacetedResultHandler<Resource> result,
            TextProvider provider) throws SearchException, IOException;

}