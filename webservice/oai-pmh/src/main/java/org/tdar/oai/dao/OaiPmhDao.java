package org.tdar.oai.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.hibernate.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.oai.bean.OAIRecordType;

@Component
public class OaiPmhDao {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @SuppressWarnings("unchecked")
    public List<OaiDcProvider> handleSearch(OAIRecordType recordType, OaiSearchResult search, Date effectiveFrom,
            Date effectiveUntil, Long collectionId) {
        String qn = "query.oai.collections";
        if (recordType != null) {
            switch (recordType) {
            case INSTITUTION:
                qn = "query.oai.institutions";
                break;
            case PERSON:
                qn = "query.oai.people";
                break;
            case RESOURCE:
                qn = "query.oai.resources";
                break;
            default:
                break;
            }
        }

        Query query = genericDao.getNamedQuery(qn + "_count");
        setupQuery(query, effectiveFrom, effectiveUntil, recordType, collectionId);
        search.setTotalRecords(((Long) query.uniqueResult()).intValue());

        query = genericDao.getNamedQuery(qn);
        setupQuery(query, effectiveFrom, effectiveUntil, recordType, collectionId);

        query.setMaxResults(search.getRecordsPerPage());
        query.setFirstResult(search.getStartRecord());
        List<OaiDcProvider> results = new ArrayList<>();
        results.addAll(query.list());
        search.setResults(results);
        return results;
    }

    void setupQuery(Query query, Date effectiveFrom, Date effectiveUntil, OAIRecordType recordType, Long collectionId) {
        Date to = DateTime.now().toDate();
        Date from = new DateTime(1900).toDate();
        if (effectiveFrom != null) {
            from = effectiveFrom;
        }
        if (effectiveUntil != null) {
            effectiveUntil = to;
        }
        query.setParameter("start", from);
        query.setParameter("end", to);
        Long id = -1L;
        if (collectionId != null && collectionId > -1L) {
            id = collectionId;
        }
        if (recordType == OAIRecordType.RESOURCE) {
            query.setParameter("collectionId", id);
        }

    }
}
