package org.tdar.search.service.query;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.search.query.part.AutocompleteTitleQueryPart;
import org.tdar.search.query.part.CollectionAccessQueryPart;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.GeneralSearchQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
@Transactional
public class CollectionSearchService extends AbstractSearchService {

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient SearchService<ResourceCollection> searchService;

    public LuceneSearchResultHandler<ResourceCollection> buildResourceCollectionQuery(TdarUser authenticatedUser, List<String> allFields, boolean limitToTopLevel,
            LuceneSearchResultHandler<ResourceCollection> result, TextProvider provider) throws ParseException, SolrServerException, IOException {
        ResourceCollectionQueryBuilder queryBuilder = new ResourceCollectionQueryBuilder();
        queryBuilder.setOperator(Operator.AND);

        if (CollectionUtils.isNotEmpty(allFields)) {
            queryBuilder.append(new GeneralSearchQueryPart(allFields));
        }
        queryBuilder.append(new FieldQueryPart<String>(QueryFieldNames.COLLECTION_TYPE, CollectionType.SHARED.name()));
        if (limitToTopLevel) {
            queryBuilder.append(new FieldQueryPart<Boolean>(QueryFieldNames.TOP_LEVEL, true));
        }
        // either it's not hidden and you can see it, or it is hidden but you have rights to it.

        QueryPartGroup rightsPart = new QueryPartGroup(Operator.OR);
        rightsPart.append(new FieldQueryPart<String>(QueryFieldNames.COLLECTION_HIDDEN, "false"));
        if (PersistableUtils.isNotNullOrTransient(authenticatedUser)) {
            QueryPartGroup qpg = new QueryPartGroup(Operator.AND);
            qpg.append(new FieldQueryPart<String>(QueryFieldNames.COLLECTION_HIDDEN, "true"));
            if (!authorizationService.can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser)) {
                // if we're a "real user" and not an administrator -- make sure the user has view rights to things in the collection
                qpg.append(new FieldQueryPart<Long>(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, authenticatedUser.getId()));
                rightsPart.append(qpg);
            } else {
                // if we're admin, drop the hidden check
                rightsPart.clear();
            }
        }
        queryBuilder.append(rightsPart);
        searchService.handleSearch(queryBuilder, result, provider);
        return result;

    }

    public LuceneSearchResultHandler<ResourceCollection> findCollection(TdarUser authenticatedUser, GeneralPermissions permission, String title,
            LuceneSearchResultHandler<ResourceCollection> result, TextProvider provider) throws ParseException, SolrServerException, IOException {
        ResourceCollectionQueryBuilder q = new ResourceCollectionQueryBuilder();
        q.append(new AutocompleteTitleQueryPart(title));
        boolean admin = false;
        if (authorizationService.can(InternalTdarRights.VIEW_ANYTHING, authenticatedUser)) {
            admin = true;
        }
        CollectionAccessQueryPart queryPart = new CollectionAccessQueryPart(authenticatedUser, admin, permission);
        q.append(queryPart);
        searchService.handleSearch(q, result, provider);
        return result;

    }

}