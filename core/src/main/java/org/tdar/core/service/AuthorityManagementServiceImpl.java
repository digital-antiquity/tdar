package org.tdar.core.service;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.atteo.evo.inflector.English;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.ReflectionDao;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.authority.AuthorityManagementLog;
import org.tdar.core.service.authority.DupeMode;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.integration.dto.v1.IntegrationWorkflowData;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.LogType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.activity.Activity;

@Service
public class AuthorityManagementServiceImpl implements AuthorityManagementService  {



    @Autowired
    private ReflectionDao reflectionDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Autowired
    private ReflectionService reflectionService;

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private EmailService emailService;

    // List of classes we will evaluate when looking for references.
    private static List<Class<?>> hostClasses = Arrays.<Class<?>> asList(Resource.class, InformationResource.class, ResourceCreator.class,
            Person.class, Institution.class, TdarUser.class, IntegrationWorkflowData.class, BookmarkedResource.class, AuthorizedUser.class,
            ResourceCollection.class);

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#getReferrers(java.lang.Class, java.util.Collection)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Field, ScrollableResults> getReferrers(Class<?> referredClass, Collection<Long> dupeIds) {
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

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#getReferrerCounts(java.lang.Class, java.util.List)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#getTotalReferrerCount(java.lang.Class, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public long getTotalReferrerCount(Class<?> referredClass, List<Long> idlist) {
        Map<Class<?>, Long> map = getReferrerCounts(referredClass, idlist);
        long total = 0;
        for (long count : map.values()) {
            total += count;
        }
        return total;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#getReferrerCountMaps(java.lang.Class, java.util.List)
     */
    @Override
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

    // TODO: jim you (probably) aren't handling one-to-many correctly yet.
    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#updateReferrers(org.tdar.core.bean.entity.Person, java.lang.Class, java.util.Collection, java.lang.Long, org.tdar.core.service.authority.DupeMode, boolean)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = false)
    public <T extends Dedupable> void updateReferrers(Person user, Class<? extends Dedupable> class1, Collection<Long> dupeIds, Long authorityId,
            DupeMode dupeMode, boolean sendEmail) {
        Activity activity = new Activity();
        activity.setName(String.format("update-referrers:: referredClass:%s\tauthorityId:%s", class1.getSimpleName(), authorityId));
        ActivityManager.getInstance().addActivityToQueue(activity);
        activity.start();
        logger.info("deduping {} [{}: auth: {}]", class1, dupeMode, authorityId);
        int maxAffectedRecordsCount = TdarConfiguration.getInstance().getAuthorityManagementMaxAffectedRecords();
        int affectedRecordCount = 0;
        // get a list of all the referrer objects and the Fields that contain the reference.
        Map<Field, ScrollableResults> referrers = getReferrers(class1, dupeIds);

        // instantiate the duplicates and the authority record
        Set<T> dupes = new HashSet<T>((Collection<? extends T>) genericDao.findAll(class1, dupeIds));
        T authority = (T) genericDao.find(class1, authorityId);

        // prevent 'protected' records from being deleted
        if (countProtectedRecords(dupes) > 0) {
            activity.end();
            throw new TdarRecoverableRuntimeException("authorityManagementService.dedup_not_allowed_protected");
        }

        /*
         * -if many-to-many or one-to-many,
         * + get collection getter and remove dupes
         * + add authority records to collection
         * + (this may be unnecessary if many-to-many target deletion implicitly deletes rows in jointable... test this out!)
         * - if many-to-one
         * + get scalar setter and set to authority record
         * -hibsession.save() each reference
         */
        logger.debug("mode: {}", dupeMode);
        AuthorityManagementLog<T> authorityManagementLog = new AuthorityManagementLog<T>(authority, dupes, user, dupeMode);
        for (Map.Entry<Field, ScrollableResults> entry : referrers.entrySet()) {
            Field field = entry.getKey();
            ScrollableResults scrollableResults = entry.getValue();
            logger.debug("field:{}", field);
            while (scrollableResults.next()) {
                affectedRecordCount++;

                Persistable referrer = (Persistable) scrollableResults.get(0);
                genericDao.markWritableOnExistingSession(referrer);
                if (dupeMode != DupeMode.MARK_DUPS_ONLY) {
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        // remove all dupes from the Collection and add in the authoritative entity (unless it's there already)
                        Collection<T> collection = reflectionService.callFieldGetter(referrer, field);
                        for (T dupe : dupes) {
                            if (collection.remove(dupe)) {
                                authorityManagementLog.add(referrer, field, dupe);
                            }
                        }
                        if (!collection.contains(authority)) {
                            collection.add(authority);
                        }
                    } else {
                        T dupe = reflectionService.callFieldGetter(referrer, field);
                        authorityManagementLog.add(referrer, field, dupe);
                        reflectionService.callFieldSetter(referrer, field, authority);
                    }
                    genericDao.saveOrUpdate(referrer);
                }

            }
            scrollableResults.close();
        }

        // FIXME: this seems dodgy to me. replace with the slower/safer way.
        // Throw an exception if this operation touched on too many records. Here we rely upon the assumption that throwing an exception will rollback the
        // underlying transaction and all will be set back to normal. A much slower, but safer, way to go about it would be to pre-count the affected records.
        if ((dupeMode != DupeMode.MARK_DUPS_ONLY) && (affectedRecordCount > maxAffectedRecordsCount)) {
            throw new TdarRecoverableRuntimeException("authorityManagementService.dedup_not_allowed_too_many", Arrays.asList(maxAffectedRecordsCount));
        }
        logger.debug("Done initial round ... processing synonyms");
        // add the dupes to the authority as synonyms
        processSynonyms(authority, dupes, dupeMode);
        logAndNotify(authorityManagementLog, sendEmail);
        genericDao.saveOrUpdate(authority);
        // finally, delete each dupe
        genericDao.saveOrUpdate(dupes);
        activity.end();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#countProtectedRecords(java.util.Collection)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public <T extends Dedupable> int countProtectedRecords(Collection<T> dupes) {
        int count = 0;
        for (Dedupable d : dupes) {
            if (!d.isDedupable()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Old names become synonyms... this makrs items as synonyms as needed
     * 
     * @param authority
     * @param dupes
     * @param markAndConsoldiateDups
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T extends Dedupable> void processSynonyms(T authority, Set<T> dupes, DupeMode markAndConsoldiateDups) {
        for (T dup : dupes) {
            if (dup.equals(authority)) {
                continue;
            }
            authority.getSynonyms().addAll(dup.getSynonyms());
            authority.getSynonyms().remove(authority);
            dup.getSynonyms().clear();
            switch (markAndConsoldiateDups) {
                case DELETE_DUPLICATES:
                    dup.setStatus(Status.DELETED);
                    break;
                case MARK_DUPS_ONLY:
                case MARK_DUPS_AND_CONSOLDIATE:
                    dup.setStatus(Status.DUPLICATE);
                    authority.getSynonyms().addAll(dupes);
                    break;
            }
        }
    }

    /**
     * Log the results of the deduplication (XML/text) and notify the admins (email)
     * 
     * @param logData
     */
    private <T extends Dedupable<?>> void logAndNotify(AuthorityManagementLog<T> logData, boolean sendEmail) {
        logger.debug("{}", logData);

        // log the xml to filestore/logs
        Filestore filestore = TdarConfiguration.getInstance().getFilestore();
        String xml = "";
        String className = logData.getAuthority().getClass().getSimpleName();
        int numUpdated = logData.getUpdatedReferrers().keySet().size(); // number of records affected, not total reference count
        try {
            xml = serializationService.convertToXML(logData);
        } catch (Exception e) {
            xml = MessageHelper.getMessage("authorityManagementService.xml_conversion_error");
            logger.warn("could not completely log authmgmt operation", e);
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-m-s");
        String datePart = dateFormat.format(new Date());
        String filename = className.toLowerCase() + "-" + datePart + ".txt";
        filestore.storeLog(LogType.AUTHORITY_MANAGEMENT, filename, xml);
        if (!sendEmail) {
            return;
        }

        // now send a summary email
        String subject = MessageHelper.getMessage("authorityManagementService.email_subject",
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(),
                        MessageHelper.getMessage("authorityManagementService.service_name"),
                        logData.getUserDisplayName(), numUpdated, className, logData.getAuthority().toString()));
        Email email = new Email();
        email.setSubject(subject);
        email.setUserGenerated(false);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("log", logData);
        map.put("className", className);
        map.put("numUpdated", numUpdated);

        map.put("referrers", logData.getUpdatedReferrers().entrySet());
        try {
            emailService.queueWithFreemarkerTemplate("auth-report.ftl", map, email);
        } catch (Exception e) {
            logger.warn("could not send email: {} ", e);
        }
    }


    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#findPluralDups(java.lang.Class, org.tdar.core.bean.entity.Person, boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void findPluralDups(Class<? extends Keyword> cls, Person user, boolean listOnly) {
        Map<String, Keyword> map = new HashMap<>();
        Map<Keyword, Set<Keyword>> dups = new HashMap<>();
        for (Keyword kwd : genericDao.findAll(cls)) {
            if (kwd.getLabel().matches("\\d+s")) {
                continue;
            }
            map.put(kwd.getLabel(), kwd);
        }
        for (String label : map.keySet()) {
            String plural = English.plural(label);
            Keyword dup = map.get(plural);
            Keyword key = map.get(label);
            if (dup != null && ObjectUtils.notEqual(dup, key)) {
                if (dup.isDuplicate()) {
                    continue;
                }
                logger.debug("should set plural: {} to singular: {}", plural, label);
                Set<Keyword> list = dups.get(key);
                if (list == null) {
                    list = new HashSet<>();
                }
                list.add(dup);
                dups.put(key, list);
            }
        }
        if (listOnly) {
            return;
        }
        for (Entry<Keyword, Set<Keyword>> entry : dups.entrySet()) {
            processSynonyms(entry.getKey(), entry.getValue(), DupeMode.MARK_DUPS_ONLY);
            updateReferrers(user, (Class<? extends Dedupable<?>>) cls, PersistableUtils.extractIds(entry.getValue()), entry.getKey().getId(),
                    DupeMode.MARK_DUPS_ONLY, false);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#cleanupKeywordDups(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Async
    @Transactional(readOnly = false)
    public void cleanupKeywordDups(final TdarUser authenticatedUser) {
        findPluralDups(CultureKeyword.class, authenticatedUser, false);
        findPluralDups(GeographicKeyword.class, authenticatedUser, false);
        findPluralDups(OtherKeyword.class, authenticatedUser, false);
        findPluralDups(SiteNameKeyword.class, authenticatedUser, false);
        findPluralDups(SiteTypeKeyword.class, authenticatedUser, false);
        findPluralDups(TemporalKeyword.class, authenticatedUser, false);
        logger.debug("done pluralization");
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.AuthorityManagementService#cleanupInstitutionsWithSpaces(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Async
    @Transactional(readOnly = false)
    public void cleanupInstitutionsWithSpaces(final TdarUser user) {
        try {
            List<Institution> findInstitutionsWIthSpaces = institutionDao.findInstitutionsWIthSpaces();
            logger.debug("institutions: {}", findInstitutionsWIthSpaces);
            for (Institution space : findInstitutionsWIthSpaces) {
                Institution clean = institutionDao.findByName(space.getName().trim());
                logger.debug("working with: {} --> ", space, clean);
                if (clean != null) {
                    processSynonyms(clean, new HashSet<Institution>(Arrays.asList(space)), DupeMode.MARK_DUPS_AND_CONSOLDIATE);
                    updateReferrers(user, Institution.class, Arrays.asList(space.getId()), clean.getId(), DupeMode.MARK_DUPS_AND_CONSOLDIATE, false);
                }
            }
        } catch (Exception e) {
            logger.debug("execption in institution cleanup", e);
        }
    }
}
