package org.tdar.search.converter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Hideable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.SearchUtils;

public class AbstractSolrDocumentConverter {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractSolrDocumentConverter.class);

    public static SolrInputDocument convertPersistable(Indexable persist) {
        SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(QueryFieldNames.ID, persist.getId());
        Class<? extends Indexable> class1 = persist.getClass();
        doc.setField(QueryFieldNames.CLASS, class1.getName());
        doc.setField(QueryFieldNames._ID, SearchUtils.createKey(persist));
        boolean hidden = false;
        Status status = Status.ACTIVE;
        if (persist instanceof HasStatus) {
            Status status2 = ((HasStatus) persist).getStatus();
            doc.setField(QueryFieldNames.STATUS, status2.name());
            status= status2;
        }
        
        if (persist instanceof Hideable) {
            boolean hidden2 = ((Hideable) persist).isHidden();
            doc.setField(QueryFieldNames.HIDDEN, hidden2);
            hidden = hidden2;
        }
        
        if (hidden == true && status == Status.ACTIVE) {
            doc.setField(QueryFieldNames.EFFECTIVELY_PUBLIC, true);
        } else {
            doc.setField(QueryFieldNames.EFFECTIVELY_PUBLIC, false);
        }

        if (persist instanceof Updatable) {
            Updatable up = (Updatable) persist;
            doc.setField(QueryFieldNames.DATE_CREATED, dateFormatUTC.format(up.getDateCreated()));
            doc.setField(QueryFieldNames.DATE_UPDATED, dateFormatUTC.format(up.getDateUpdated()));
        }
        return doc;

    }

    protected static void addDateField(SolrInputDocument doc, Date date, String field) {
        if (date != null) {
            if (date instanceof java.sql.Date) {
                doc.setField(field, new Date(date.getTime()));
            } else {
                doc.setField(field, date);
            }
        }
    }
}
