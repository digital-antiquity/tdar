package org.tdar.dataone.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Log;
import org.dataone.service.types.v1.ObjectInfo;
import org.dataone.service.types.v1.ObjectList;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.GenericDao;
import org.tdar.dataone.bean.DataOneObject;
import org.tdar.dataone.bean.EntryType;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.LogEntryImpl;
import org.tdar.dataone.service.DataOneConfiguration;
import org.tdar.dataone.service.DataOneUtils;

@Component
public class DataOneDao {
	private static final String SHARED_WHERE = "from resource res where (res.external_id is not null and res.external_id != '') and (res.date_updated between :start and :end or res.date_created between :start and :end) and res.status='ACTIVE' and res.resource_type not in ('PROJECT', 'CODING_SHEET','ONTOLOGY') and (:identifier is null or res.external_id=:identifier) and ";
    private static final String D1_SUFFIX  = SHARED_WHERE  + "(:type is null or   'D1'=:type)";
	private static final String TDAR_SUFFIX = SHARED_WHERE + " (:type is null or 'TDAR'=:type)";
	private static final String D1_PREFIX = " external_id as \"externalId\", 'D1'   as \"type\", id as \"id\", date_updated as \"dateUpdated\" ";
	private static final String TDAR_PREFIX = " external_id as \"externalId\", 'TDAR' as \"type\", id as \"id\", date_updated as \"dateUpdated\" ";
    private static final String LIST_OBJECT_QUERY = "select " + D1_PREFIX + " " + D1_SUFFIX + " union " + "select "+ TDAR_PREFIX +" " + TDAR_SUFFIX;
    private static final String LIMIT = " and res.id < " + DataOneConfiguration.getInstance().getMaxId();
    private static final String LIST_OBJECT_QUERY_LIMITED = "select " + D1_PREFIX + " " + D1_SUFFIX + LIMIT + " union " + "select "+ TDAR_PREFIX +" " + TDAR_SUFFIX + LIMIT;
    private static final String LIST_OBJECT_QUERY_COUNT =  "select ((select count(res.id) " + D1_SUFFIX + " ) + ( " + "select count(res.id) " + TDAR_SUFFIX +" ))";
    private static final String LIST_OBJECT_QUERY_COUNT_LIMITED =  "select ((select count(res.id) " + D1_SUFFIX + LIMIT + " ) + ( " + "select count(res.id) " + TDAR_SUFFIX + LIMIT +" ))";

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    public List<ListObjectEntry> findUpdatedResourcesWithDOIs(Date start, Date end, String formatId, String identifier, ObjectList list, int count, int startNum) {
        SQLQuery query = setupListObjectQuery(LIST_OBJECT_QUERY_COUNT, start, end, formatId, identifier);
        if (DataOneConfiguration.getInstance().isLimited()) {
            query = setupListObjectQuery(LIST_OBJECT_QUERY_COUNT_LIMITED, start, end, formatId, identifier);
        }
//        logger.debug("{}", query);
        list.setTotal(((Number)query.uniqueResult()).intValue());
        if (count == 0) {
            return new ArrayList<>();
        }

        query = setupListObjectQuery(LIST_OBJECT_QUERY, start, end, formatId, identifier);
        if (DataOneConfiguration.getInstance().isLimited()) {
            query = setupListObjectQuery(LIST_OBJECT_QUERY_LIMITED, start, end, formatId, identifier);
        }
        query.setMaxResults(count);
        query.setFirstResult(startNum);
        List list2 = query.list();
        List<ListObjectEntry> toReturn = new ArrayList<>();
        for (Object wrap : list2) {
            try {

            Object[] obj = (Object[])wrap;
            String externalId = (String) obj[0];
            String type = (String)obj[1];
            long tdarId = ((BigInteger)obj[2]).longValue();
            DateTime dateUpdated = DataOneUtils.toUtc((Date)obj[3]);
            toReturn.add(new ListObjectEntry(externalId, type, tdarId, dateUpdated.toDate(),null,null,null,null));
            } catch (Exception e) {
                logger.error("{}",e,e);
            }
        }
        logger.debug("return: {}:", toReturn);
        return toReturn;
    }

    private SQLQuery setupListObjectQuery(String sqlQuery, Date fromDate, Date toDate, String formatId, String identifier) {
        SQLQuery query = genericDao.getNativeQuery(sqlQuery);
        
        // if Tier3, use "query.dataone_list_objects_t3"
        initStartEnd(fromDate, toDate, query);
        EntryType type = null;
        if (StringUtils.isNotBlank(formatId)) {
            type = EntryType.getTypeFromFormatId(formatId);
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
        if (count == 0) {
            return new ArrayList<>();
        }

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
            to = toDate;
        }
        query.setParameter("start", from);
        query.setParameter("end", to);
    }

 
    public DataOneObject updateObjectEntries(ObjectInfo info, EntryType type, String seriesId,Long tdarId, String submitter) {
        DataOneObject uniqueResult = findByIdentifier(info.getIdentifier().getValue());
        if (uniqueResult == null) {
            DataOneObject obj = new DataOneObject();
            obj.setChecksum(info.getChecksum().getValue());
            obj.setIdentifier(info.getIdentifier().getValue());
            obj.setDateCreated(new Date());
            obj.setType(type);
            obj.setTdarId(tdarId);
            obj.setSeriesId(seriesId);
            obj.setSysMetadataModified(info.getDateSysMetadataModified());
            obj.setSize(info.getSize().longValue());
            obj.setSubmitter(submitter);
            obj.setFormatId(info.getFormatId().getValue());
            genericDao.saveOrUpdate(obj);
            return obj;
        }
        return uniqueResult;
    }

    
    public DataOneObject findAndObsoleteLastHarvestedVersion(String seriesId, DataOneObject current) {
        DataOneObject uniqueResult = findLastHarvestedVersion(seriesId, current);
        if (uniqueResult != null) {
            logger.debug("find and obsolete: {} --> {}", seriesId, current, uniqueResult);
            uniqueResult.setObsoletedBy(current.getIdentifier());
            current.setObsoletes(uniqueResult.getIdentifier());
            genericDao.saveOrUpdate(uniqueResult);
            genericDao.saveOrUpdate(current);
            return uniqueResult;
        }
        return null;
    }
    
    public DataOneObject findLastHarvestedVersion(String seriesId, DataOneObject current) {
        Query namedQuery = genericDao.createQuery("from DataOneObject where seriesId=:seriesId and (obsoletedBy is null or obsoletedBy='') and identifier !=:identifier");// and type=:type
        namedQuery.setParameter("seriesId", seriesId);
        namedQuery.setParameter("identifier", current.getIdentifier());
        List<DataOneObject> list = namedQuery.list();
        if (list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            logger.warn(" >> found {} results where expected 1", list.size());
        }
        return list.get(0);
        
    }

    public DataOneObject findByIdentifier(String id) {
        Query namedQuery = genericDao.createQuery("from DataOneObject where identifier=:identifier");
        namedQuery.setParameter("identifier", id);
        DataOneObject uniqueResult = (DataOneObject) namedQuery.uniqueResult();
        return uniqueResult;
    }


}
