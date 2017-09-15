package org.tdar.search.converter;

import java.util.HashSet;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;

public class CollectionDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(ResourceCollection collection) {

        /*
         * See solr/configsets/default/conf/collections-schema.xml
         */
        SolrInputDocument doc = convertPersistable(collection);
        ResourceCollection props = collection;
        doc.setField(QueryFieldNames.NAME, props.getName());
        doc.setField(QueryFieldNames.NAME_SORT, Sortable.getTitleSort(props.getTitle()));
        doc.setField(QueryFieldNames.DESCRIPTION, props.getDescription());
        StringBuilder sb = new StringBuilder();
        sb.append(props.getTitle()).append(" ").append(props.getDescription()).append(" ");

        doc.setField(QueryFieldNames.ALL, sb.toString());
        doc.setField(QueryFieldNames.SUBMITTER_ID, collection.getOwner().getId());
        doc.setField(QueryFieldNames.RESOURCE_IDS, new HashSet<>(collection.getResourceIds()));
        doc.setField(QueryFieldNames.COLLECTION_PARENT, collection.getParentId());
        HashSet<Long> parentIds = new HashSet<>();
        if (collection.getAlternateParent() != null) {
            parentIds.add(collection.getAlternateParent().getId());
        }
        if (collection.getParentIds() != null) {
            parentIds.addAll(collection.getParentIds());
            doc.setField(QueryFieldNames.COLLECTION_PARENT_LIST, parentIds);
        }
        doc.setField(QueryFieldNames.COLLECTION_PARENT_LIST, new HashSet<>(collection.getParentIds()));
        doc.setField(QueryFieldNames.TOP_LEVEL, collection.isTopLevel());
        CollectionRightsExtractor extractor = new CollectionRightsExtractor(collection);
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, extractor.getUsersWhoCanModify());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER, extractor.getUsersWhoCanAdminister());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, extractor.getUsersWhoCanView());
        doc.setField(QueryFieldNames.COLLECTION_TYPE, collection.getType().name());

        doc.setField(QueryFieldNames.GENERAL_TYPE, LookupSource.COLLECTION.name());
        doc.setField(QueryFieldNames.OBJECT_TYPE, ObjectType.from(collection.getType()).name());
        doc.setField(QueryFieldNames.OBJECT_TYPE_SORT, ObjectType.from(collection.getType()).getSortName());
        return doc;
    }

}
