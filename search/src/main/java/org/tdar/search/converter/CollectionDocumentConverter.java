package org.tdar.search.converter;

import java.util.HashSet;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;

public class CollectionDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(VisibleCollection collection) {

    /*
     * See solr/configsets/default/conf/collections-schema.xml
     */
        SolrInputDocument doc = convertPersistable(collection);
        VisibleCollection props = collection;
        doc.setField(QueryFieldNames.NAME, props.getName());
        doc.setField(QueryFieldNames.NAME_SORT, Sortable.getTitleSort(props.getTitle()));
        doc.setField(QueryFieldNames.DESCRIPTION, props.getDescription());
        StringBuilder sb = new StringBuilder();
        sb.append(props.getTitle()).append(" ").append(props.getDescription()).append(" ");

        doc.setField(QueryFieldNames.ALL, sb.toString());
        doc.setField(QueryFieldNames.SUBMITTER_ID, collection.getOwner().getId());
        doc.setField(QueryFieldNames.RESOURCE_IDS, new HashSet<>(collection.getResourceIds()));
        if (collection instanceof HierarchicalCollection) {
            HierarchicalCollection hier = (HierarchicalCollection)collection;
            doc.setField(QueryFieldNames.COLLECTION_PARENT, hier.getParentId());
            HashSet<Long> parentIds = new HashSet<>();
            if (hier.getAlternateParent() != null) {
                parentIds.add(hier.getAlternateParent().getId());
            }
            if (hier.getParentIds() != null) {
                 parentIds.addAll(hier.getParentIds());
                doc.setField(QueryFieldNames.COLLECTION_PARENT_LIST, parentIds);
            }
            doc.setField(QueryFieldNames.COLLECTION_PARENT_LIST, new HashSet<>(hier.getParentIds()));
            doc.setField(QueryFieldNames.TOP_LEVEL, hier.isTopLevel());
        } else {
            doc.setField(QueryFieldNames.TOP_LEVEL, false);
        }
        CollectionRightsExtractor extractor = new CollectionRightsExtractor(collection);
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, extractor.getUsersWhoCanModify());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER, extractor.getUsersWhoCanAdminister());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, extractor.getUsersWhoCanView());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADD, extractor.getUsersWhoCanAdd());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_REMOVE, extractor.getUsersWhoCanRemove());
        doc.setField(QueryFieldNames.COLLECTION_TYPE, collection.getType().name());

        doc.setField(QueryFieldNames.GENERAL_TYPE, LookupSource.COLLECTION.name());
        doc.setField(QueryFieldNames.OBJECT_TYPE, ObjectType.from(collection.getType()).name());
        doc.setField(QueryFieldNames.OBJECT_TYPE_SORT, ObjectType.from(collection.getType()).getSortName());
        return doc;
    }
    
    
}
