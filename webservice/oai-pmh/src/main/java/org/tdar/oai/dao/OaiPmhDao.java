package org.tdar.oai.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.oai.bean.OAIRecordType;
import org.tdar.utils.PersistableUtils;

@Component
public class OaiPmhDao {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @SuppressWarnings("unchecked")
    public List<OaiDcProvider> handleSearch(OAIRecordType recordType, OaiSearchResult search, Date effectiveFrom_,
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
                    if (PersistableUtils.isNotNullOrTransient(collectionId)) {
                        qn = "query.oai.resources_collections";
                    }
                    break;
                default:
                    break;
            }
        }
        Date effectiveFrom = effectiveFrom_;
        Query query = genericDao.getNamedQuery(qn);
        if (search.getCursor().getAfter().after(effectiveFrom)) {
            effectiveFrom = search.getCursor().getAfter();
        }

        setupQuery(query, effectiveFrom, effectiveUntil, recordType, collectionId);
        query.setParameter("id", search.getCursor().getIdFrom());

        query.setMaxResults(search.getRecordsPerPage());
        List<OaiDcProvider> results = new ArrayList<>();
        results.addAll(query.getResultList());
        search.setResults(results);
        search.setResultSize(results.size());
        return results;
    }

    void setupQuery(Query query, Date effectiveFrom, Date effectiveUntil, OAIRecordType recordType, Long collectionId) {
        Date to = DateTime.now().toDate();
        Date from = new DateTime(1900).toDate();
        if (effectiveFrom != null) {
            from = effectiveFrom;
        }
        if (effectiveUntil != null) {
            to = effectiveUntil;
        }
        query.setParameter("start", from);
        query.setParameter("end", to);
        if (collectionId != null && collectionId > -1L) {
            if (recordType == OAIRecordType.RESOURCE) {
                query.setParameter("collectionId", collectionId);
            }
        }

    }
}
