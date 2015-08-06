package org.tdar.oai.dao;

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
import org.tdar.core.dao.GenericDao;
import org.tdar.oai.bean.OAIRecordType;
import org.tdar.search.query.SearchResult;

@Component
public class OaiPmhDao {

	@Transient
	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private GenericDao genericDao;

	public List<? extends OaiDcProvider> handleSearch(OAIRecordType recordType, SearchResult search, Date effectiveFrom,
			Date effectiveUntil, Long collectionId) {
		String qn = "query.oai.collections";
		if (recordType != null) {
			switch (recordType) {
			case INSTITUTION:
				qn = "query.oai.institutions";
				break;
			case PERSON:
				qn = "query.oai.people";
			case RESOURCE:
				qn = "query.oai.resources";
			default:
				break;
			}
		}

		Query query = genericDao.getNamedQuery(qn);
		// TODO Auto-generated method stub
		if (recordType== OAIRecordType.RESOURCE) {
			query.setLong("collectionId",collectionId);
		}
		setupQuery(query, effectiveFrom, effectiveUntil);
		search.setTotalRecords(query.list().size());

		query = genericDao.getNamedQuery(qn);
		setupQuery(query, effectiveFrom, effectiveUntil);
		if (recordType== OAIRecordType.RESOURCE) {
			query.setLong("collectionId",collectionId);
		}

		query.setMaxResults(search.getRecordsPerPage());
		query.setFirstResult(search.getStartRecord());
		return query.list();
	}

	void setupQuery(Query query, Date effectiveFrom, Date effectiveUntil) {
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

	}
}
