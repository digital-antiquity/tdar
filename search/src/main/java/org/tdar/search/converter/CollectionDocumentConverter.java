package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.collection.ResourceCollection;

public class CollectionDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(ResourceCollection collection) {
        
        SolrInputDocument doc = convertPersistable(collection);
        doc.setField("name", collection.getName());
        doc.setField("name_autocomplete", collection.getName());
        doc.setField("resourceIds", collection.getResourceIds());
        doc.setField("owner.id", collection.getOwner().getId());
        doc.setField("parentId", collection.getParentId());
        doc.setField("parentIds", collection.getParentIds());
        doc.setField("description", collection.getDescription());
        doc.setField("hidden", collection.isHidden());
        doc.setField("usersWhoCanModify", collection.getUsersWhoCanModify());
        doc.setField("usersWhoCanAdminister", collection.getUsersWhoCanAdminister());
        doc.setField("usersWhoCanView", collection.getUsersWhoCanView());
        doc.setField("allSearch", collection.getAllFieldSearch());
        return doc;
    }
}
