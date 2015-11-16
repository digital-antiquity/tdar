package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;
import org.tdar.search.query.QueryFieldNames;

public class AbstractSolrDocumentConverter {

    public static SolrInputDocument convertPersistable(Persistable persist) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(QueryFieldNames.ID, persist.getId());
        doc.setField(QueryFieldNames.CLASS, persist.getClass());
        doc.setField("_id", persist.getClass().getSimpleName() + "-" + persist.getId());
        if (persist instanceof HasStatus) {
            doc.setField(QueryFieldNames.STATUS, ((HasStatus) persist).getStatus());
        }
        if (persist instanceof Updatable) {
            Updatable up = (Updatable) persist;
            doc.setField(QueryFieldNames.DATE_CREATED, up.getDateCreated());
            doc.setField(QueryFieldNames.DATE_UPDATED, up.getDateUpdated());
        }
        return doc;

    }
}
