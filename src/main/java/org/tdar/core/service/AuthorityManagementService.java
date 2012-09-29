package org.tdar.core.service;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.ReflectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.EmailService;
import org.tdar.filestore.Filestore;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.jaxb.IdList;
import org.tdar.utils.jaxb.converters.JaxbMapConverter;
import org.tdar.utils.jaxb.converters.JaxbPersistableMapConverter;

@Service
public class AuthorityManagementService {

    @Autowired
    private ReflectionDao reflectionDao;

    @Autowired
    private ReflectionService reflectionService;

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;
    
    @Autowired
    private XmlService xmlService;
    
    @Autowired
    private EmailService emailService;
 
    // List of classes we will evaluate when looking for references.
    private static List<Class<?>> hostClasses = Arrays.<Class<?>> asList(Resource.class, InformationResource.class, ResourceCreator.class,
            Person.class, Institution.class, AuthorizedUser.class);

    @Transactional(readOnly = true)
    public Map<Field, ScrollableResults> getReferrers(Class<?> referredClass, List<Long> dupeIds) {
        Map<Field, ScrollableResults> referrers = new HashMap<Field, ScrollableResults>();
        for (Class<?> targetClass : hostClasses) {
            Set<Field> fields = reflectionService.findAssignableFieldsRefererencingClass(targetClass, referredClass);
            for (Field field : fields) {
                // get the list of referrer to the duplicates via this specific class and field, add it to the pile
                ScrollableResults fieldReferrers = reflectionDao.findReferrers(field, dupeIds);
                logger.trace("The following objects refer to dupeid list via {}:{}", field.getName(), fieldReferrers);
                referrers.put(field, fieldReferrers);
            }
        }
        return referrers;
    }

    @Transactional(readOnly = true)
    public Map<Class<?>, Long> getReferrerCounts(Class<?> referredClass, List<Long> idlist) {
        Map<Class<?>, Long> countMap = new HashMap<Class<?>, Long>();
        for (Class<?> targetClass : hostClasses) {
            Set<Field> fields = reflectionService.findAssignableFieldsRefererencingClass(targetClass, referredClass);
            long classTotal = 0;
            for (Field field : fields) {
                Long count = reflectionDao.getReferrerCount(field, idlist);
                classTotal += count;
            }
            countMap.put(targetClass, classTotal);
        }
        return countMap;
    }

    @Transactional(readOnly = true)
    public long getTotalReferrerCount(Class<?> referredClass, List<Long> idlist) {
        Map<Class<?>, Long> map = getReferrerCounts(referredClass, idlist);
        long total = 0;
        for (long count : map.values()) {
            total += count;
        }
        return total;
    }

    // return a map<id, count> of occurancecounts
    // TODO: maybe create a method that returns Map<id, Map<class, count>>
    @Transactional(readOnly = true)
    public Map<Long, Long> getReferrerCountMaps(Class<?> referredClass, List<Long> idlist) {
        Map<Long, Long> countmap = new HashMap<Long, Long>();
        for (Long id : idlist) {
            countmap.put(id, 0L);
        }
        // FIXME: this loop is kludgey and confusing and will surely be fodder for my next code review
        for (Class<?> targetClass : hostClasses) {
            Set<Field> fields = reflectionService.findAssignableFieldsRefererencingClass(targetClass, referredClass);
            for (Field field : fields) {
                List<Map<String, Long>> list = reflectionDao.getReffererCountMap(field, idlist);
                for (Map<String, Long> item : list) {
                    Map<String, Long> map = item;
                    Long id = map.get("id");
                    Long updatedCount = countmap.get(id) + map.get("referenceCount");
                    countmap.put(id, updatedCount);
                }
            }
        }
        return countmap;
    }

    private <T extends Dedupable, P extends Persistable> void logDupeModification(P referrer, Field field, T dupe, AuthorityManagementLog<T> data) {
        // if we want to aggregate all this data, store it in a ThreadLocal Map and make sure
        // we serialize it right now, otherwise the data will have been changed
        // FIXME: log to database or flatfile
        data.add(referrer, field, (Persistable) dupe);
    }

    @Transactional
    // TODO: jim you (probably) aren't handling one-to-many correctly yet.
    /**
     *  Find objects that refer to the specified duplicates and replace the references with the specified authority,
     *  saving the referrers in the process. This method handles objects that may refer to another object via scalar
     *  fields as well as via collection fields.  A few assumptions, restricions:
     *  
     *  - this method assumes that, for collection fields, it is not necessary to perform a piecewise replacement
     *  of each duplicate record with an authority record.  In other words, a collection that contains multiple  
     *  duplicates will be replaced by one (and only one) authority record (if the authority record is not already
     *  in the collection) 
     *  
     *  - all of the potential referring classes must refer to duplicate objects via fields that have public getters
     *  and setters.
     *  
     */
    public <T extends Dedupable> void updateReferrers(Class<T> referredClass, List<Long> dupeIds, Long authorityId) {

        int maxAffectedRecordsCount = TdarConfiguration.getInstance().getAuthorityManagementMaxAffectedRecords();
        int affectedRecordCount = 0;
        // get a list of all the referrer objects and the Fields that contain the reference.
        Map<Field, ScrollableResults> referrers = getReferrers(referredClass, dupeIds);

        // instantiate the duplicates and the authority record
        List<T> dupes = genericDao.findAll(referredClass, dupeIds);
        T authority = genericDao.find(referredClass, authorityId);

        // prevent 'protected' records from being deleted
        if (countProtectedRecords(dupes) > 0) {
            throw new TdarRecoverableRuntimeException("This de-dupe operation is not allowed because at least one of the selected duplicates is protected");
        }

        // -if many-to-many or one-to-many,
        // + get collection getter and remove dupes
        // + add authority records to collection
        // + (this may be unnecessary if many-to-many target deletion implicitly deletes rows in jointable... test this out!)
        // -if many-to-one
        // + get scalar setter and set to authority record
        // -hibsession.save() each reference
        AuthorityManagementLog<T> authorityManagementLog = new AuthorityManagementLog<T>(authority, new HashSet<T>(dupes));
        for (Map.Entry<Field, ScrollableResults> entry : referrers.entrySet()) {
            Field field = entry.getKey();
            ScrollableResults scrollableResults = entry.getValue();
            logger.trace("field:{}", field);
            while (scrollableResults.next()) {
                affectedRecordCount++;

                Persistable referrer = (Persistable) scrollableResults.get(0);
                if (Collection.class.isAssignableFrom(field.getType())) {
                    // remove all dupes from the Collection and add in the authoritative entity (unless it's there already)
                    Collection<T> collection = reflectionService.callFieldGetter(referrer, field);
                    for (T dupe : dupes) {
                        if (collection.remove(dupe)) {
                            logDupeModification(referrer, field, dupe, authorityManagementLog);
                        }
                    }
                    if (!collection.contains(authority)) {
                        collection.add(authority);
                    }
                }
                else {
                    T dupe = reflectionService.callFieldGetter(referrer, field);
                    logDupeModification(referrer, field, dupe, authorityManagementLog);
                    reflectionService.callFieldSetter(referrer, field, authority);
                }
                genericDao.saveOrUpdate(referrer);

            }
            scrollableResults.close();
        }

        // FIXME: this seems dodgy to me. replace with the slower/safer way.
        // Throw an exception if this operation touched on too many records. Here we rely upon the assumption that throwing an exception will rollback the
        // underlying transaction and all will be set back to normal. A much slower, but safer, way to go about it would be to pre-count the affected records.
        if (affectedRecordCount > maxAffectedRecordsCount) {
            String fmt = "This de-dupe operation is not allowed because would affect too many records. The maximum affected record count is %s.";
            String msg = String.format(fmt, NumberFormat.getNumberInstance().format(maxAffectedRecordsCount));
            throw new TdarRecoverableRuntimeException(msg);
        }
        
        sendNotifications(authorityManagementLog);

        // add the dupes to the authority as synonyms
        processSynonyms(authority, dupes);

        // finally, delete each dupe
        genericDao.delete(dupes);
    }
    
    // return number "protected" items in the dupe list. Duplicates may not be de-duped
    public <T extends Dedupable> int countProtectedRecords(List<T> dupes) {
        int count = 0;
        for (Dedupable d : dupes) {
            if (!d.isDedupable()) {
                count++;
            }
        }
        return count;
    }

    private <T extends Dedupable> void processSynonyms(T authority, List<T> dupes) {
        for (T dupe : dupes) {
            authority.addSynonym(dupe);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void sendNotifications( AuthorityManagementLog logData) {
        logger.debug("{}", logData);
        
        //log the xml to filestore/logs
        Filestore filestore = TdarConfiguration.getInstance().getFilestore();
        String xml = "";
        String className = logData.getAuthority().getClass().getSimpleName();
        try {
            xml = xmlService.convertToXML(logData);
        } catch (Exception e) {
           xml = "xml conversion failure";
           logger.warn("could not completely log authmgmt operation", e);
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-m-s");
        String datePart = dateFormat.format(new Date());
        String filename =  className.toLowerCase() + "-" + datePart + ".txt";
        filestore.storeLog(filename, xml);
        
        //now send a summary email
        String to = TdarConfiguration.getInstance().getSystemAdminEmail();
        String subject = String.format("tDAR Authority Management Service: dudupe operation by %s,  merging %s dupes of type %s", 
                logData.getUserDisplayName(), 
                logData.getDupes().size(),
                className);
        
        String body = "User " + logData.getUserDisplayName() + " performed a dedupe operation.\n" +
                "class type:\t" + className + "\n" +
                "authority:\t" + logData.getAuthority() + "\n" +
                "dupecount:\t" + logData.getDupes().size() + "\n\n";
        emailService.send(body, to, subject);
        
    }
    
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "logPart")
    public static class AuthorityManagementLogPart {

        private HashMap<String, IdList> fieldToDupeIds = new HashMap<String, IdList>();

        public void add(String fieldName, Long dupeId) {
            IdList dupeIds = fieldToDupeIds.get(fieldName);
            if (dupeIds == null) {
                dupeIds = new IdList();
                fieldToDupeIds.put(fieldName, dupeIds);
            }
            dupeIds.add(dupeId);
        }

        /**
         * @return the fieldToDupeIds
         */
        @XmlElement
        // @XmlAnyElement(lax=true)
        @XmlJavaTypeAdapter(JaxbMapConverter.class)
        public HashMap<String, IdList> getFieldToDupeIds() {
            return fieldToDupeIds;
        }

        public String toString() {
            return fieldToDupeIds.toString();
        }

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "log")
    public static class AuthorityManagementLog<R> {
        private String userDisplayName = "n/a";
        private R authority;
        private Set<R> dupes;
        // map of referrer -> ( [field containing the dupe, id of the dupe] ... )
        private Map<Persistable, AuthorityManagementLogPart> updatedReferrers = new HashMap<Persistable, AuthorityManagementLogPart>();

        public AuthorityManagementLog() {
        }
        
        public AuthorityManagementLog(R authority, Set<R> dupes) {
            this.authority = authority;
            this.dupes = dupes;
        }

        public void add(Persistable referrer, Field field, Persistable dupe) {
            AuthorityManagementLogPart dupeList = updatedReferrers.get(referrer);
            if (dupeList == null) {
                dupeList = new AuthorityManagementLogPart();
                updatedReferrers.put(referrer, dupeList);
            }
            dupeList.add(field.getName(), dupe.getId());
        }

        /**
         * @return the authority
         */
        public R getAuthority() {
            return authority;
        }

        /**
         * @param authority
         *            the authority to set
         */
        public void setAuthority(R authority) {
            this.authority = authority;
        }

        /**
         * @return the dupes
         */
        public Set<R> getDupes() {
            return dupes;
        }

        /**
         * @param dupes
         *            the dupes to set
         */
        public void setDupes(Set<R> dupes) {
            this.dupes = dupes;
        }

        /**
         * @return the updatedReferrers
         */
        @XmlJavaTypeAdapter(JaxbPersistableMapConverter.class)
        public Map<Persistable, AuthorityManagementLogPart> getUpdatedReferrers() {
            return updatedReferrers;
        }

        public String toString() {
            return String.format("Authority: %s, dupes: %s, referrers: %s", authority, dupes, updatedReferrers.values());
        }

        public String getUserDisplayName() {
            return userDisplayName;
        }

        public void setUserDisplayName(String userDisplayName) {
            this.userDisplayName = userDisplayName;
        }
        
    }

}
