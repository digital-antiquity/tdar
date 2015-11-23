package org.tdar.search.service;

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
public class ResourceSearchService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    
    @Autowired
    private SearchService searchService;
    
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


    public ResourceQueryBuilder lookupResource(String name, Long projectId, boolean includeParent, Long collectionId, Long categoryId, TdarUser user, ReservedSearchParameters reservedSearchParameters, GeneralPermissions permission, TextProvider support) {
        ResourceQueryBuilder q = new ResourceQueryBuilder();
        q.append(new CategoryTermQueryPart(name, categoryId));

        if (PersistableUtils.isNotNullOrTransient(projectId)) {
            q.append(new ProjectIdLookupQueryPart(projectId));
        }

        String colQueryField = QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS;
        if (!includeParent) {
            colQueryField = QueryFieldNames.RESOURCE_COLLECTION_DIRECT_SHARED_IDS;
        }

        if (permission != null) {
            logger.error("PERMISSIONS ARE SET, but PARAMS not DEFINED");
        }
        if (PersistableUtils.isNotNullOrTransient(collectionId)) {
            q.append(new FieldQueryPart<Long>(colQueryField, collectionId));
        }
        searchService.initializeReservedSearchParameters(reservedSearchParameters, user);
        q.append(reservedSearchParameters.toQueryPartGroup(support));

        return q;
        
    }
}