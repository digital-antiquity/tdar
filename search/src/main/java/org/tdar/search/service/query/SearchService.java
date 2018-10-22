package org.tdar.search.service.query;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.SearchInfoObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;

import com.opensymphony.xwork2.TextProvider;

public interface SearchService<I extends Indexable> {

    /**
     * remove unauthorized statuses from list. it's up to caller to handle implications of empty list
     *
     * @param statusList
     * @param user
     */
    void filterStatusList(List<Status> statusList, TdarUser user);

    /**
     * Replace AND/OR with lowercase so that lucene does not interpret them as operaters.
     * It is not necessary sanitized quoted strings.
     *
     * @param unsafeQuery
     * @return
     */
    String sanitize(String unsafeQuery);

    /**
     * Takes a set of @link ResourceCreator entities from the @link SearchParameters and resolves them in tDAR before doing an ID search in Lucene
     *
     * @param group
     * @param maxCreatorsToResolve
     * @throws ParseException
     * @throws IOException
     * @throws SolrServerException
     * @throws SearchException
     */
    void updateResourceCreators(SearchParameters group, Integer maxCreatorsToResolve) throws IOException, SearchException;

    /**
     * Applies the @link ResourceType facet to a search
     *
     * @param qb
     * @param selectedResourceTypes
     * @param handler
     */
    void addResourceTypeFacetToViewPage(ResourceQueryBuilder qb, List<ResourceType> selectedResourceTypes, SearchResultHandler<?> handler);

    Collection<? extends Resource> findMostRecentResources(long l, TdarUser authenticatedUser, TextProvider provider)
            throws SearchException, IOException;

    Collection<? extends Resource> findRecentResourcesSince(Date d, TdarUser authenticatedUser, TextProvider provider)
            throws SearchException, IOException;

    <C> void facetBy(Class<C> c, Collection<C> vals, SearchResultHandler<Indexable> handler);

    void handleSearch(QueryBuilder q, LuceneSearchResultHandler resultHandler, TextProvider textProvider) throws SearchException, IOException;

    SearchInfoObject getSearchInfoObject(TdarUser authenticatedUser);

}