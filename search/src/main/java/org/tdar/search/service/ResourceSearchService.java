package org.tdar.search.service;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.CategoryTermQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.ProjectIdLookupQueryPart;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class ResourceSearchService extends AbstractSearchService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public ResourceSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private final SearchService searchService;

    public QueryBuilder buildCollectionResourceSearch() {
        QueryBuilder qb = new ResourceCollectionQueryBuilder();
        qb.append(new FieldQueryPart<CollectionType>(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.COLLECTION_HIDDEN, Boolean.FALSE));
        qb.append(new FieldQueryPart<Boolean>(QueryFieldNames.TOP_LEVEL, Boolean.TRUE));
        return qb;
    }

    /**
     * Shared logic to find all direct children of container resource (ResourceCollections and Projects)
     *
     * @param fieldName
     * @param indexable
     * @param user
     * @return
     */
    public <P extends Persistable> ResourceQueryBuilder buildResourceContainedInSearch(String fieldName, P indexable, TdarUser user, TextProvider provider) {
        ResourceQueryBuilder qb = new ResourceQueryBuilder();
        ReservedSearchParameters reservedSearchParameters = new ReservedSearchParameters();
        searchService.initializeReservedSearchParameters(reservedSearchParameters, user);
        qb.append(reservedSearchParameters, provider);
        qb.setOperator(Operator.AND);
        qb.append(new FieldQueryPart<>(fieldName, indexable.getId()));

        return qb;
    }

    public ResourceQueryBuilder lookupResource(String name, Long projectId, Boolean includeParent, Long collectionId, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters_, GeneralPermissions permission, TextProvider support) {
        ResourceQueryBuilder q = new ResourceQueryBuilder();
        if (StringUtils.isNotBlank(name) || categoryId != null) {
            q.append(new CategoryTermQueryPart(name, categoryId));
        }

        if (PersistableUtils.isNotNullOrTransient(projectId)) {
            q.append(new ProjectIdLookupQueryPart(projectId));
        }

        String colQueryField = QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS;
        if (includeParent == Boolean.FALSE || includeParent == null) {
            colQueryField = QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS;
        }

        if (permission != null) {
            logger.error("PERMISSIONS ARE SET, but PARAMS not DEFINED");
        }
        if (PersistableUtils.isNotNullOrTransient(collectionId)) {
            q.append(new FieldQueryPart<Long>(colQueryField, collectionId));
        }

        ReservedSearchParameters reservedSearchParameters = reservedSearchParameters_;
        if (reservedSearchParameters == null) {
            reservedSearchParameters = new ReservedSearchParameters();
        }

        if (reservedSearchParameters != null) {
            initializeReservedSearchParameters(reservedSearchParameters, user);
            q.append(reservedSearchParameters.toQueryPartGroup(support));
        }
        
        return q;

    }

    public ResourceQueryBuilder buildAdvancedSearch(SearchParameters params, ReservedSearchParameters reservedParams_, TdarUser user, TextProvider provider) {
        ResourceQueryBuilder builder = new ResourceQueryBuilder();
        ReservedSearchParameters reservedParams = reservedParams_;
        if (reservedParams == null) {
            reservedParams = new ReservedSearchParameters();
        }
        initializeReservedSearchParameters(reservedParams, user);

        builder.setOperator(Operator.AND);
        builder.appendIfNotEmpty(params, provider);
        builder.appendIfNotEmpty(reservedParams, provider);
        return builder;
    }

}