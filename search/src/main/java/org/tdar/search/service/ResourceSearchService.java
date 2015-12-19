package org.tdar.search.service;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.CategoryTermQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;
import org.tdar.search.query.part.ProjectIdLookupQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class ResourceSearchService extends AbstractSearchService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public ResourceSearchService(SearchService<Resource> searchService) {
        this.searchService = searchService;
    }

    private final SearchService<Resource> searchService;

    public SearchResultHandler<Resource> buildCollectionResourceSearch(SearchResultHandler<Resource> result, TextProvider provider) throws ParseException, SolrServerException, IOException {
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart<CollectionType>(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.COLLECTION_HIDDEN, Boolean.FALSE));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.TOP_LEVEL, Boolean.TRUE));
        searchService.handleSearch(qb, result, provider);
        return result;
    }

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
    public <P extends Persistable> SearchResultHandler<Resource> buildResourceContainedInSearch(String fieldName, P indexable, TdarUser user, SearchResultHandler<Resource> result, TextProvider provider) throws ParseException, SolrServerException, IOException {
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        searchService.initializeReservedSearchParameters(reservedSearchParameters, user);
        qb.append(reservedSearchParameters, provider);
        qb.setOperator(Operator.AND);
        qb.append(new FieldQueryPart<>(fieldName, indexable.getId()));
        searchService.handleSearch(qb, result, provider);
        return result;
    }

    public SearchResultHandler<Resource> lookupResource(TdarUser user, ResourceLookupObject look, SearchResultHandler<Resource> result, TextProvider support) throws ParseException, SolrServerException, IOException {
        ResourceQueryBuilder q = new ResourceQueryBuilder();
        if (StringUtils.isNotBlank(look.getTerm()) || look.getCategoryId() != null) {
            q.append(new CategoryTermQueryPart(look.getTerm(), look.getCategoryId()));
        }

        if (PersistableUtils.isNotNullOrTransient(look.getProjectId())) {
            q.append(new ProjectIdLookupQueryPart(look.getProjectId()));
        }

        String colQueryField = QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS;
        if (look.getIncludeParent()  == Boolean.FALSE || look.getIncludeParent() == null) {
            colQueryField = QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS;
        }

        if (look.getPermission() != null) {
            logger.error("PERMISSIONS ARE SET, but PARAMS not DEFINED");
        }
        if (PersistableUtils.isNotNullOrTransient(look.getCollectionId())) {
            q.append(new FieldQueryPart<Long>(colQueryField, look.getCollectionId()));
        }

        ReservedSearchParameters reservedSearchParameters = look.getReservedSearchParameters();
        initializeReservedSearchParameters(reservedSearchParameters, user);
        q.append(reservedSearchParameters.toQueryPartGroup(support));
        q.appendFilter(reservedSearchParameters.getFilters());

        searchService.handleSearch(q, result, MessageHelper.getInstance());
        return result;

    }

    public SearchResultHandler<Resource> buildKeywordQuery(Keyword keyword, KeywordType keywordType, SearchResultHandler<Resource> result, TextProvider provider) throws ParseException, SolrServerException, IOException {
        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.append(new HydrateableKeywordQueryPart<Keyword>(keywordType, Arrays.asList(keyword)));
        rqb.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        searchService.handleSearch(rqb, result, provider);
        return result;
    }

    public SearchResultHandler<Resource> buildAdvancedSearch(AdvancedSearchQueryObject asqo, TdarUser authenticatedUser,
            SearchResultHandler<Resource> result, TextProvider provider) throws SolrServerException, IOException, ParseException {
        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        queryBuilder.setOperator(Operator.AND);
        QueryPartGroup topLevelQueryPart;
        QueryPartGroup reservedQueryPart;

        topLevelQueryPart = new QueryPartGroup(asqo.getOperator());

        for (SearchParameters group : asqo.getSearchParameters()) {
            if (group == null) {
                continue;
            }
            group.setExplore(asqo.isExplore());
            try {
                searchService.updateResourceCreators(group, 20);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            topLevelQueryPart.append(group.toQueryPartGroup(provider));
        }
        queryBuilder.append(topLevelQueryPart);

        asqo.setSearchPhrase(topLevelQueryPart.getDescription(provider));

        if (topLevelQueryPart.isEmpty() || CollectionUtils.isNotEmpty(asqo.getAllGeneralQueryFields())) {
            asqo.setCollectionSearchBoxVisible(true);
        }
        reservedQueryPart = processReservedTerms(asqo.getReservedParams(), authenticatedUser,provider);
        asqo.setRefinedBy(reservedQueryPart.getDescription(provider));
        queryBuilder.append(reservedQueryPart);
        // TODO Auto-generated method stub
        searchService.handleSearch(queryBuilder, result, provider);
        return result;
    }

    // deal with the terms that correspond w/ the "narrow your search" section
    // and from facets
    protected QueryPartGroup processReservedTerms(ReservedSearchParameters reserved, TdarUser tdarUser, TextProvider provider) {
        searchService.initializeReservedSearchParameters(reserved, tdarUser);
        return reserved.toQueryPartGroup(provider);
    }

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
    public SearchResultHandler<Resource> generateQueryForRelatedResources(Creator<?> creator, TdarUser user, SearchResultHandler<Resource> result, TextProvider provider) throws ParseException, SolrServerException, IOException {
        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        queryBuilder.setOperator(Operator.AND);
        SearchParameters params = new SearchParameters(Operator.AND);
        params.setCreatorOwner(new ResourceCreatorProxy(creator, null));
        queryBuilder.append(params, provider);
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        searchService.initializeReservedSearchParameters(reservedSearchParameters, user);
        queryBuilder.append(reservedSearchParameters, provider);
        searchService.handleSearch(queryBuilder, result, provider);
        return result;
    }

}