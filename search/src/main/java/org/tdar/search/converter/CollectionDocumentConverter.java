package org.tdar.search.converter;

import java.util.HashSet;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.search.query.QueryFieldNames;

public class CollectionDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/collections-schema.xml
     */
    public static SolrInputDocument convert(ResourceCollection collection) {
        
        SolrInputDocument doc = convertPersistable(collection);
        doc.setField(QueryFieldNames.NAME, collection.getName());
        if (collection.getResourceIds() != null) {
             doc.setField(QueryFieldNames.RESOURCE_IDS, new HashSet<>(collection.getResourceIds()));
		 }
        doc.setField(QueryFieldNames.RESOURCE_OWNER, collection.getOwner().getId());
        doc.setField(QueryFieldNames.COLLECTION_PARENT, collection.getParentId());
        if (collection.getParentIds() != null) {
             doc.setField(QueryFieldNames.COLLECTION_PARENT_LIST, new HashSet<>(collection.getParentIds()));
        }
        doc.setField(QueryFieldNames.DESCRIPTION, collection.getDescription());
        doc.setField(QueryFieldNames.TOP_LEVEL, collection.isTopLevel());
        doc.setField(QueryFieldNames.TYPE, collection.getType().name());
        doc.setField(QueryFieldNames.COLLECTION_HIDDEN, collection.isHidden());
        CollectionRightsExtractor extractor = new CollectionRightsExtractor(collection);
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, extractor.getUsersWhoCanModify());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER, extractor.getUsersWhoCanAdminister());
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, extractor.getUsersWhoCanView());
        doc.setField(QueryFieldNames.ALL, collection.getAllFieldSearch());
        return doc;
    }
    
    
}
