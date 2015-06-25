package org.tdar.dataone.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Log;
import org.dataone.service.types.v1.ObjectList;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.GenericDao;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.ListObjectEntry.Type;
import org.tdar.dataone.bean.LogEntryImpl;

@Component
public class DataOneDao {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @SuppressWarnings("unchecked")
    public List<ListObjectEntry> findUpdatedResourcesWithDOIs(Date start, Date end, Type type, String formatId, String identifier, ObjectList list) {
        Query query = setupListObjectQuery(start, end, type, formatId, identifier);

        // FIXME: find better way to handle pagination
        list.setTotal(query.list().size());

        query = setupListObjectQuery(start, end, type, formatId, identifier);
        query.setMaxResults(list.getCount());
        query.setFirstResult(list.getStart());
        return query.list();
    }

    private Query setupListObjectQuery(Date fromDate, Date toDate, Type type, String formatId, String identifier) {
        Query query = genericDao.getNamedQuery("query.dataone_list_objects_t1");
        // if Tier3, use "query.dataone_list_objects_t3"
        initStartEnd(fromDate, toDate, query);
        if (StringUtils.isNotBlank(formatId)) {
            type = Type.getTypeFromFormatId(formatId);
        }
        
        if (type != null) {
            query.setString("type", type.name());
        } else {
            query.setString("type", null);
        }
        
        query.setString("identifier", identifier);
        return query;
    }

    @SuppressWarnings("unchecked")
    public List<LogEntryImpl> findLogFiles(Date fromDate, Date toDate, Event event, String idFilter, int start, int count, Log log) {
        Query query = setupQuery(fromDate, toDate, event, idFilter);
        // FIXME: find better way to handle pagination
        log.setTotal(query.list().size());

        query = setupQuery(fromDate, toDate, event, idFilter);

        query.setMaxResults(log.getCount());
        query.setFirstResult(log.getStart());
        return query.list();

    }

    private Query setupQuery(Date fromDate, Date toDate, Event event, String idFilter) {
        Query query = genericDao.getNamedQuery("query.dataone_list_logs");
        initStartEnd(fromDate, toDate, query);
        if (event != null) {
            query.setString("event", event.name());
        } else {
            query.setString("event", null);
        }
        query.setString("idFilter", idFilter);
        return query;
    }

    private void initStartEnd(Date fromDate, Date toDate, Query query) {
        Date to = DateTime.now().toDate();
        Date from = new DateTime(1900).toDate();
        if (fromDate != null) {
            from = fromDate;
        }
        if (toDate != null) {
            toDate = to;
        }
        query.setParameter("start", from);
        query.setParameter("end", to);
    }

}
