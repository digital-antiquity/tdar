package org.tdar.dataone.dao;

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
	private static final String DATAONE_LIMIT = "from DataOneObject where (:type is null or type=:type) and "
	        + "(sysMetadataModified between :start and :end) and "
	        + "(:identifier is null or identifier=:identifier)";

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @SuppressWarnings("unchecked")
    public List<DataOneObject> findUpdatedResources(Date start, Date end, String formatId, String identifier, ObjectList list, int startNum, int count) {
        String queryString = "select count(id) " + DATAONE_LIMIT;
        if (DataOneConfiguration.getInstance().isLimited()) {
            queryString += " and id < " + DataOneConfiguration.getInstance().getMaxId();
        }

        Query query = setupQuery(start, end, formatId, identifier, queryString);

        list.setTotal(((Number)query.uniqueResult()).intValue());
        if (count == 0) {
            return new ArrayList<>();
        }

        query.setMaxResults(count);
        query.setFirstResult(startNum);

        queryString = DATAONE_LIMIT;
        query = setupQuery(start, end, formatId, identifier, queryString);

        return query.list();
    }

    private Query setupQuery(Date start, Date end, String formatId, String identifier, String queryString) {
        Query query = genericDao.createQuery(queryString);
        
        initStartEnd(start, end, query);
        EntryType type = null;
        if (StringUtils.isNotBlank(formatId)) {
            type = EntryType.getTypeFromFormatId(formatId);
        }
        logger.trace("{} - {}", type, identifier);
        if (type != null) {
            query.setString("type", type.name());
        } else {
            query.setString("type", null);
        }
                
        query.setString("identifier", identifier);
        return query;
    }

    
    public List<ListObjectEntry> unify() {
        Query query = genericDao.createQuery("select max(sysMetadataModified) from DataOneObject");
        Date date  = (Date) query.uniqueResult();
        if (date == null) {
            date = new Date(0L);
        }
        query = genericDao.createQuery("select id, externalId, dateUpdated from Resource where resourceType not in ('PROJECT', 'CODING_SHEET','ONTOLOGY') and"
                + "(externalId is not null and trim(externalId) != '') and (dateUpdated > :date or dateCreated  > :date)");
        logger.trace("{}", date);
        query.setParameter("date", date);
        List list2 = query.list();
        List<ListObjectEntry> entries = new ArrayList<>();
        for (Object wrap : list2) {
            try {
            Object[] obj = (Object[])wrap;
            String externalId = (String) obj[1];
            long tdarId = ((Long)obj[0]);
            DateTime dateUpdated = DataOneUtils.toUtc((Date)obj[2]);
            entries.add(new ListObjectEntry(externalId, EntryType.D1.name(), tdarId, dateUpdated.toDate(),null,null,null,null));
            entries.add(new ListObjectEntry(externalId, EntryType.TDAR.name(), tdarId, dateUpdated.toDate(),null,null,null,null));
            } catch (Exception e) {
                logger.error("{}",e,e);
            }
        }
        return entries;

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
        logger.trace("{} -> {}" , from, to);
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
            logger.trace("{} {} {}", obj, obj.getSysMetadataModified(), obj.getIdentifier());
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
            uniqueResult.setSysMetadataModified(current.getSysMetadataModified());
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
