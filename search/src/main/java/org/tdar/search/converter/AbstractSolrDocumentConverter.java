package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;

public class AbstractSolrDocumentConverter {

    public static SolrInputDocument convertPersistable(Persistable persist) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", persist.getId());
        if (persist instanceof HasStatus) {
            doc.setField("status", ((HasStatus) persist).getStatus());
        }
        if (persist instanceof Updatable) {
            Updatable up = (Updatable) persist;
            doc.setField("dateCreated", up.getDateCreated());
            doc.setField("dateUpdated", up.getDateUpdated());
        }
        return doc;

    }
}
