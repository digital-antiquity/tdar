package org.tdar.search.service.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.MultiCoreQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralSearchResourceQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.resource.CategoryTermQueryPart;
import org.tdar.search.query.part.resource.ProjectIdLookupQueryPart;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.range.DateRange;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class ResourceSearchServiceImpl extends AbstractSearchService implements ResourceSearchService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final GenericService genericService;
    private final SearchService<Resource> searchService;

    @Autowired
    public ResourceSearchServiceImpl(SearchService<Resource> searchService, GenericService genericService) {
        this.searchService = searchService;
        this.genericService = genericService;
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#buildCollectionResourceSearch(org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> buildCollectionResourceSearch(LuceneSearchResultHandler<Resource> result, TextProvider provider)
            throws SearchException, IOException {
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart<CollectionResourceSection>(QueryFieldNames.COLLECTION_TYPE, CollectionResourceSection.MANAGED));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.HIDDEN, Boolean.FALSE));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.TOP_LEVEL, Boolean.TRUE));
        searchService.handleSearch(qb, result, provider);
        return result;
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#buildResourceContainedInSearch(org.tdar.core.bean.resource.Project, java.lang.String, org.tdar.core.bean.entity.TdarUser, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> buildResourceContainedInSearch(Project indexable, String term, TdarUser user,
            LuceneSearchResultHandler<Resource> result, TextProvider provider) throws SearchException, IOException {
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        qb.append(new FieldQueryPart<>(QueryFieldNames.PROJECT_ID, indexable.getId()));
        runContainedInQuery(term, user, result, provider, qb);
        return result;
    }

    private void runContainedInQuery(String term, TdarUser user, LuceneSearchResultHandler<Resource> result, TextProvider provider, ResourceQueryBuilder qb)
            throws SearchException, IOException {
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        initializeReservedSearchParameters(reservedSearchParameters, user);
        qb.setOperator(Operator.AND);
        if (StringUtils.isNotBlank(term)) {
            reservedSearchParameters.getAllFields().add(term);
        }
        qb.append(reservedSearchParameters, provider);
        searchService.handleSearch(qb, result, provider);
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#buildResourceContainedInSearch(org.tdar.core.bean.collection.VisibleCollection, java.lang.String, org.tdar.core.bean.entity.TdarUser, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> buildResourceContainedInSearch(ResourceCollection indexable, String term, TdarUser user,
            LuceneSearchResultHandler<Resource> result, TextProvider provider) throws SearchException, IOException {
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        List<Long> ids = new ArrayList<>();
        ids.add(indexable.getId());
        QueryPartGroup idGroup = new QueryPartGroup(Operator.OR);
        idGroup.append(new FieldQueryPart<>(QueryFieldNames.RESOURCE_COLLECTION_UNMANAGED_IDS, indexable.getId()));
        idGroup.append(new FieldQueryPart<>(QueryFieldNames.RESOURCE_COLLECTION_MANAGED_IDS, indexable.getId()));
        qb.append(idGroup);
        runContainedInQuery(term, user, result, provider, qb);
        return result;
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#lookupResource(org.tdar.core.bean.entity.TdarUser, org.tdar.search.bean.ResourceLookupObject, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> lookupResource(TdarUser user, ResourceLookupObject searchParams, LuceneSearchResultHandler<Resource> result,
            TextProvider support) throws SearchException, IOException {
 
    	//Construct a Query Builder object, add the data that we want to search for, then execute the search. 
    	ResourceQueryBuilder queryBuilder = new ResourceQueryBuilder();

    	//If a search term  or category ID is provided, then add it as a field to be searched. 
    	if (StringUtils.isNotBlank(searchParams.getTerm()) || searchParams.getCategoryId() != null) {
            queryBuilder.append(new CategoryTermQueryPart(searchParams.getTerm(), searchParams.getCategoryId()));
        }

    	//If a project Id is provided, add it as a field to be searched. 
        if (PersistableUtils.isNotNullOrTransient(searchParams.getProjectId())) {
            queryBuilder.append(new ProjectIdLookupQueryPart(searchParams.getProjectId()));
        }

        if (StringUtils.isNotBlank(searchParams.getGeneralQuery())) {
            queryBuilder.append(new GeneralSearchResourceQueryPart(searchParams.getGeneralQuery()));
        }

        
        //If we're not looking for the parent collections, then only search for the resources directly in the collection. 
        if (searchParams.getIncludeParent() == Boolean.FALSE || searchParams.getIncludeParent() == null) {
        	QueryPartGroup qgp = new QueryPartGroup(Operator.OR);
        	qgp.append(createCollectionLookupQueryPart(searchParams, queryBuilder, QueryFieldNames.RESOURCE_COLLECTION_DIRECT_MANAGED_IDS, searchParams.getCollectionIds()));
        	qgp.append(createCollectionLookupQueryPart(searchParams, queryBuilder, QueryFieldNames.RESOURCE_COLLECTION_DIRECT_UNMANAGED_IDS, searchParams.getCollectionIds()));
        	queryBuilder.append(qgp);
        	
        }
        else{
        	setupCollectionLookup(searchParams, queryBuilder, QueryFieldNames.RESOURCE_COLLECTION_MANAGED_IDS, searchParams.getCollectionIds());
        }

        ReservedSearchParameters reservedSearchParameters = searchParams.getReservedSearchParameters();
        reservedSearchParameters.setUseSubmitterContext(searchParams.isUseSubmitterContext());
        initializeReservedSearchParameters(reservedSearchParameters, user);
        
        queryBuilder.append(reservedSearchParameters.toQueryPartGroup(support));
        queryBuilder.appendFilter(reservedSearchParameters.getFilters());
        
        searchService.handleSearch(queryBuilder, result, MessageHelper.getInstance());
        return result;
    }

    /**
     * 
     * 
     * @param searchParms
     * @param queryBuilder
     * @param queryFieldName
     * @param collectionIds
     */
    private void setupCollectionLookup(ResourceLookupObject searchParms, ResourceQueryBuilder queryBuilder, String queryFieldName, List<Long> collectionIds) {
        FieldQueryPart<Long> queryPart = createCollectionLookupQueryPart(searchParms, queryBuilder, queryFieldName, collectionIds);
        queryBuilder.append(queryPart);
    }
    
    private FieldQueryPart<Long> createCollectionLookupQueryPart(ResourceLookupObject searchParms, ResourceQueryBuilder queryBuilder, String queryFieldName, List<Long> collectionIds){
    	Set<Long> filtered = new HashSet<>();
        for (Long id : collectionIds) {
            if (PersistableUtils.isNotNullOrTransient(id)) {
                filtered.add(id);
            }
        }
    	return new FieldQueryPart<Long>(queryFieldName, Operator.OR, filtered);
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#buildKeywordQuery(org.tdar.core.bean.keyword.Keyword, org.tdar.core.bean.keyword.KeywordType, org.tdar.search.bean.ReservedSearchParameters, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> buildKeywordQuery(Keyword keyword, KeywordType keywordType, ReservedSearchParameters rsp, LuceneSearchResultHandler<Resource> result,
            TextProvider provider, TdarUser user) throws SearchException, IOException {

        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        queryBuilder.setOperator(Operator.AND);
        queryBuilder.append(new HydrateableKeywordQueryPart<Keyword>(keywordType, Arrays.asList(keyword)));
        initializeReservedSearchParameters(rsp, user);
        queryBuilder.append(rsp, provider);
        searchService.handleSearch(queryBuilder, result, provider);
        return result;
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#buildAdvancedSearch(org.tdar.search.bean.AdvancedSearchQueryObject, org.tdar.core.bean.entity.TdarUser, org.tdar.search.query.LuceneSearchResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> buildAdvancedSearch(AdvancedSearchQueryObject asqo, TdarUser authenticatedUser,
            LuceneSearchResultHandler<Resource> result, TextProvider provider) throws SearchException, IOException {
        ResourceQueryBuilder queryBuilder = new ResourceQueryBuilder();
        if (asqo.isMultiCore()) {
            queryBuilder = new MultiCoreQueryBuilder();
        }
        logger.trace("{}", queryBuilder.getClass());
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
            } catch (SearchException e) {
                logger.error("issue hydrating creators",e);
            }
            topLevelQueryPart.append(group.toQueryPartGroup(provider));
        }
        queryBuilder.append(topLevelQueryPart);


        if (topLevelQueryPart.isEmpty() || CollectionUtils.isNotEmpty(asqo.getAllGeneralQueryFields())) {
            asqo.setCollectionSearchBoxVisible(true);
        }
        reservedQueryPart = processReservedTerms(asqo.getReservedParams(), authenticatedUser, provider);
        asqo.setRefinedBy(reservedQueryPart.getDescription(provider));
        queryBuilder.append(reservedQueryPart);

        queryBuilder.setDeemphasizeSupporting(true);
        searchService.handleSearch(queryBuilder, result, provider);
        // dependent on handle search to hydrate 
        asqo.setSearchPhrase(topLevelQueryPart.getDescription(provider));
        return result;
    }

    // deal with the terms that correspond w/ the "narrow your search" section
    // and from facets
    @Transactional(readOnly = true)
    protected QueryPartGroup processReservedTerms(ReservedSearchParameters reserved, TdarUser tdarUser, TextProvider provider) {
        initializeReservedSearchParameters(reserved, tdarUser);
        return reserved.toQueryPartGroup(provider);
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#inflateSearchParameters(org.tdar.search.bean.SearchParameters)
     */
    @Override
    @Transactional(readOnly = true)
    public void inflateSearchParameters(SearchParameters searchParameters) {
        // FIXME: refactor to ue genericService.populateSparseObjectsById() which optimizes the qeries to the DB
        // Also, consider moving into genericService
        List<List<? extends Persistable>> lists = searchParameters.getSparseLists();
        for (List<? extends Persistable> list : lists) {
            logger.debug("inflating list of sparse objects: {}", list);
            // making unchecked cast so compiler accepts call to set()
            @SuppressWarnings("unchecked")
            ListIterator<Persistable> itor = (ListIterator<Persistable>) list.listIterator();
            while (itor.hasNext()) {
                Persistable sparse = itor.next();
                if (sparse != null) {
                    Persistable persistable = genericService.find(sparse.getClass(), sparse.getId());
                    logger.debug("\t inflating {}({}) -> {}", sparse.getClass().getSimpleName(), sparse.getId(), persistable);
                    itor.set(persistable);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.search.service.query.ResourceSearchService#generateQueryForRelatedResources(org.tdar.core.bean.entity.Creator, org.tdar.core.bean.entity.TdarUser, org.tdar.search.query.facet.FacetedResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> generateQueryForRelatedResources(Creator<?> creator, TdarUser user, FacetedResultHandler<Resource> result,
            TextProvider provider) throws SearchException, IOException {
        QueryBuilder queryBuilder = new ResourceQueryBuilder();
        // result.setRecordsPerPage(MAX_FTQ_RESULTS);
        queryBuilder.setOperator(Operator.AND);
        SearchParameters params = new SearchParameters(Operator.AND);
        params.setCreatorOwner(new ResourceCreatorProxy(creator, null));
        queryBuilder.append(params, provider);
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        initializeReservedSearchParameters(reservedSearchParameters, user);
        queryBuilder.append(reservedSearchParameters, provider);
        searchService.handleSearch(queryBuilder, result, provider);
        return result;
    }

    /*
     * The @link AdvancedSearchController's ReservedSearchParameters is a proxy object for handling advanced boolean searches. We initialize it with the search
     * parameters
     * that are AND-ed with the user's search to ensure appropriate search results are returned (such as a Resource's @link Status).
     */
    @Transactional(readOnly = true)
    protected void initializeReservedSearchParameters(ReservedSearchParameters reservedSearchParameters, TdarUser user) {
        if (reservedSearchParameters == null) {
            return;
        }
        reservedSearchParameters.setAuthenticatedUser(user);
        reservedSearchParameters.setTdarGroup(authenticationService.findGroupWithGreatestPermissions(user));
        Set<Status> allowedSearchStatuses = authorizationService.getAllowedSearchStatuses(user);
        List<Status> statuses = reservedSearchParameters.getStatuses();
        if (statuses == null) {
            statuses = new ArrayList<>();
        }
        statuses.removeAll(Collections.singletonList(null));

        if (CollectionUtils.isEmpty(statuses)) {
            statuses = new ArrayList<>(Arrays.asList(Status.ACTIVE, Status.DRAFT));
        }
        for (Iterator<Status> iterator = statuses.iterator(); iterator.hasNext();) {
            Status status = iterator.next();
            if (!allowedSearchStatuses.contains(status)) {
                iterator.remove();
            }
        }
        reservedSearchParameters.setStatuses(statuses);
        if (statuses.isEmpty()) {
            throw (new TdarRecoverableRuntimeException("auth.search.status.denied"));
        }

    }

    @Transactional(readOnly = true)
    public LuceneSearchResultHandler<Resource> findByTdarYear(int year, LuceneSearchResultHandler<Resource> result, TextProvider support)
            throws SearchException, IOException {
        ResourceQueryBuilder q = new ResourceQueryBuilder();
        q.setOperator(Operator.AND);
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        reservedSearchParameters.setStatuses(new ArrayList<>(Arrays.asList(Status.ACTIVE)));
        initializeReservedSearchParameters(reservedSearchParameters, null);
        SearchParameters params = new SearchParameters();
        DateTime dt = new DateTime().withYear(year).withMonthOfYear(1).withDayOfMonth(1).withTimeAtStartOfDay();
        params.getRegisteredDates().add(new DateRange(dt.toDate(), dt.plusYears(1).toDate()));
        q.append(params.toQueryPartGroup(support));
        q.append(reservedSearchParameters.toQueryPartGroup(support));
        result.setSortField(SortOption.DATE);
        searchService.handleSearch(q, result, support);
        return result;
    }

    @Transactional(readOnly =true)
    public LuceneSearchResultHandler<Resource> findByResourceType(ResourceType resourceType, LuceneSearchResultHandler<Resource> result, TextProvider support) throws SearchException, IOException {
        ResourceQueryBuilder q = new ResourceQueryBuilder();
        q.setOperator(Operator.AND);
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        reservedSearchParameters.setStatuses(new ArrayList<>(Arrays.asList(Status.ACTIVE)));
        initializeReservedSearchParameters(reservedSearchParameters, null);
        SearchParameters params = new SearchParameters();
        params.getResourceTypes().add(resourceType);
        q.append(params.toQueryPartGroup(support));
        q.append(reservedSearchParameters.toQueryPartGroup(support));
        result.setSortField(SortOption.TITLE);
        searchService.handleSearch(q, result, support);
        return result;
    }
}