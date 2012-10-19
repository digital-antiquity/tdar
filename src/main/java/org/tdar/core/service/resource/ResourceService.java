package org.tdar.core.service.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.Video;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.XmlService;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.struts.data.ResourceUsageStatistic;

@Service
public class ResourceService extends GenericService {
    public enum ErrorHandling {
        NO_VALIDATION,
        VALIDATE_SKIP_ERRORS,
        VALIDATE_WITH_EXCEPTION
    }

    @Autowired
    private XmlService xmlService;

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private GeoSearchService geoSearchService;

    @Transactional(readOnly = true)
    public boolean isOntology(Long id) {
        return getGenericDao().find(Ontology.class, id) != null;
    }

    @Transactional(readOnly = true)
    public boolean isDataset(Long id) {
        return getGenericDao().find(Dataset.class, id) != null;
    }

    @Transactional(readOnly = true)
    public boolean isProject(Long id) {
        return getGenericDao().find(Project.class, id) != null;
    }

    @Transactional(readOnly = true)
    public boolean isCodingSheet(Long id) {
        return getGenericDao().find(CodingSheet.class, id) != null;
    }

    @Transactional(readOnly = true)
    public boolean isSensoryData(Long id) {
        return getGenericDao().find(SensoryData.class, id) != null;
    }

    @Transactional(readOnly = true)
    public boolean isVideo(Long id) {
        return getGenericDao().find(Video.class, id) != null;
    }

    @Transactional(readOnly = true)
    public <R extends Resource> R find(Long id) {
        if (id == null)
            return null;
        ResourceType rt = datasetDao.findResourceType(id);
        logger.info("finding resource " + id + " type:" + rt);
        if (rt == null) {
            return null;
        }
        return (R) getGenericDao().find(rt.getResourceClass(), id);
    }

    /**
     * @param <T>
     * @param modifiedResource
     * @param message
     * @param payload
     */
    @Transactional
    public <T extends Resource> void logResourceModification(T modifiedResource, Person person, String message) {
        logResourceModification(modifiedResource, person, message, null);
    }

    /**
     * @param <T>
     * @param modifiedResource
     * @param message
     * @param payload
     */
    @Transactional
    public <T extends Resource> void logResourceModification(T modifiedResource, Person person, String message, String payload) {
        ResourceRevisionLog log = new ResourceRevisionLog();
        log.setLogMessage(message);
        log.setResource(modifiedResource);
        log.setPerson(person);
        log.setTimestamp(new Date());
        log.setPayload(payload);
        save(log);
    }

    // FIXME: I'm pretty sure @Transactional is pointless here. you can't rollback file io
    @Transactional(readOnly = true)
    public <T extends Resource> void saveRecordToFilestore(T resource) {
        @SuppressWarnings("deprecation")
        InformationResourceFileVersion version = new InformationResourceFileVersion();
        version.setFilename("record.xml");
        version.setExtension("xml");
        version.setFileVersionType(VersionType.RECORD);
        version.setInformationResourceId(resource.getId());
        try {
            TdarConfiguration.getInstance().getFilestore().storeAndRotate(new StringInputStream(xmlService.convertToXML(resource)), version, 5);
        } catch (Exception e) {
            // FIXME: This catch is too wide. E.g. a LazyInitializationException is arguably not a "recoverable exception" and we should throw something
            // more appropriate like TdarRuntimeException so that the caller knows to (for example) rollback any DB work that happened to this record
            // before we tried to write it to the filestore. On the other hand, if just a file IO issue, it may be acceptable to simply log the error
            // but keep the DB transaction.
            logger.error("something happend when converting record to XML: {}", e);
            throw new TdarRecoverableRuntimeException("could not save xml record");
        }
        logger.trace("done saving");
    }

    public List<Status> findAllStatuses() {
        return Arrays.asList(Status.values());
    }

    @Transactional(readOnly = false)
    public void incrementAccessCounter(Resource r) {
        ResourceAccessStatistic rac = new ResourceAccessStatistic(new Date(), r);
        save(rac);
    }

    @Transactional(readOnly = true)
    public void updateTransientAccessCount(Resource resource) {
        resource.setTransientAccessCount(datasetDao.getAccessCount(resource).longValue());
    }

    @Transactional(readOnly = true)
    public Number countActiveResources(Class<? extends Resource> resourceClass) {
        return datasetDao.countActiveResources(resourceClass);
    }

    @Transactional(readOnly = true)
    public List<HomepageGeographicKeywordCache> getISOGeographicCounts() {
        return datasetDao.getISOGeographicCounts();
    }

    @Transactional(readOnly = true)
    public Long countResourcesForUserAccess(Person user) {
        return datasetDao.countResourcesForUserAccess(user);
    }

    @Transactional
    public void processManagedKeywords(Resource resource, Collection<LatitudeLongitudeBox> allLatLongBoxes) {
        List<String> ignoreProperties = new ArrayList<String>(Arrays.asList("approved", "selectable", "level"));
        Set<GeographicKeyword> incomingManagedKeywords = new HashSet<GeographicKeyword>();
        for (LatitudeLongitudeBox latLong : allLatLongBoxes) {
            Set<GeographicKeyword> managedKeywords = geoSearchService.extractAllGeographicInfo(latLong);
            logger.debug(resource.getId() + " :  " + managedKeywords + " " + managedKeywords.size());
            incomingManagedKeywords.addAll(
                    getGenericDao().findByExamples(GeographicKeyword.class, managedKeywords, ignoreProperties, FindOptions.FIND_FIRST_OR_CREATE));
        }
        Persistable.Base.reconcileSet(resource.getManagedGeographicKeywords(), incomingManagedKeywords);
    }

    @Transactional
    /**
     * Given a collection of hibernate-managed beans (the 'current' collection)  and another collection of transient beans (the 'incoming' collection),
     * update the current collection to match the contents of the incoming collection. This method will associate all elements in the incoming collection 
     * with the specified resource.  Contents of both collections should satisfy the HasResource interface.
     * 
     * @param resource  the 'owner' of the elements in the incoming collection.  This method will associate all elements of the incoming collection with this resource.
     * @param shouldSave if true,  this method will persist elements of the incoming collection.
     * @param validateMethod determines what validation steps (if any) to perform on each element of the incoming collection
     * @param incoming_ the incoming collection of HasResource elements.  
     * @param current the current collection of HasResource elements.  This method will modify collection to contain the same elements as the incoming collection.
     * @param cls type of the collection elements.
     */
    public <H extends HasResource<R>, R extends Resource> void saveHasResources(R resource, boolean shouldSave, ErrorHandling validateMethod,
            Collection<H> incoming_,
            Set<H> current, Class<H> cls) {
        if (CollectionUtils.isEmpty(incoming_) && CollectionUtils.isEmpty(current)) {
            // skip a complete no-op
            return;
        }

        // FIXME: the last parameter should be able to be determined via generics/reflection instead of passing in
        if (incoming_ == null) {
            incoming_ = new ArrayList<H>();
        }
        Collection<H> incoming = incoming_;
        // there are cases where current and incoming_ are the same object, if that's the case
        // then we need to copy incoming_ before
        if (incoming_ == current && !CollectionUtils.isEmpty(incoming_)) {
            incoming = new ArrayList<H>();
            incoming.addAll(incoming_);
            current.clear();
        }

        // assume everything that's incoming is valid or deduped and tied back into
        // tDAR entities/beans
        logger.debug("Current Collection of {}s ({}) : {} ", new Object[] { cls.getSimpleName(), current.size(), current });

        // iterate through the current and remove unreferences beans
        // NOTE: this assumes that we're working with @OneToMany with orphanRemoval=true
        Iterator<H> currentIterator = current.iterator();
        while (currentIterator.hasNext()) {
            H current_ = currentIterator.next();
            if (!incoming.contains(current_)) {
                currentIterator.remove();
                logger.debug("deleting : {} ", current_);
            }
        }

        if (!CollectionUtils.isEmpty(incoming)) {
            logger.debug("Incoming Collection of {}s ({})  : {} ", new Object[] { cls.getSimpleName(), incoming.size(), incoming });
            Iterator<H> incomingIterator = incoming.iterator();
            while (incomingIterator.hasNext()) {
                H hasResource_ = incomingIterator.next();

                if (hasResource_ != null) {
                    // there are cases where we may not want to validate or where we don't want to throw an exception
                    // setting resource, as the resource "type" may be what makes something valid or not
                    hasResource_.setResource(resource);

                    if (validateMethod != ErrorHandling.NO_VALIDATION && !hasResource_.isValid()) {
                        hasResource_.setResource(null);
                        logger.debug("skipping: {} - INVALID", hasResource_);
                        if (validateMethod == ErrorHandling.VALIDATE_WITH_EXCEPTION) {
                            throw new TdarRecoverableRuntimeException(hasResource_ + " is not valid");
                        }
                        continue;
                    }

                    // attach the incoming notes to a hibernate session
                    if (shouldSave) {
                        hasResource_ = getGenericDao().merge(hasResource_);
                    }
                    logger.debug("adding {} to {} ", hasResource_, current);
                    current.add(hasResource_);
                }
            }
        }
    }

    /**
     * @return
     */
    public List<HomepageResourceCountCache> getResourceCounts() {
        return datasetDao.getResourceCounts();
    }

    public List<?> findResourceLinkedValues(Class<?> cls) {
        return datasetDao.findResourceLinkedValues(cls);

    }

    public Map<ResourceType, Map<Status, Long>> getResourceCountAndStatusForUser(Person p, List<ResourceType> resourceTypes) {
        return datasetDao.getResourceCountAndStatusForUser(p, resourceTypes);
    }

    public Long getResourceCount(ResourceType resourceType, Status status) {
        return datasetDao.getResourceCount(resourceType, status);
    }

    @Transactional
    public ResourceUsageStatistic getResourceUsageStatistics(List<Long> personId, List<Long> resourceId, List<Long> collectionId, List<Long> projectId,
            List<Status> statuses, List<VersionType> types) {
        return datasetDao.getResourceUsageStatistics(personId, resourceId, collectionId, projectId, statuses, types);
    }
}
