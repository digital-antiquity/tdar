package org.tdar.core.service.resource;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.poi.hssf.util.HSSFColor.VIOLET;
import org.hibernate.ScrollableResults;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.ColumnVisibiltiy;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.cache.Caches;
import org.tdar.core.cache.HomepageGeographicCache;
import org.tdar.core.cache.HomepageResourceCountCache;
import org.tdar.core.dao.AggregateStatisticsDao;
import org.tdar.core.dao.BillingAccountDao;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.dao.resource.ResourceTypeStatusInfo;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.AuthenticationService;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.exception.TdarRuntimeException;
import org.tdar.search.geosearch.GeoSearchService;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.transform.MetaTag;
import org.tdar.transform.ScholarMetadataTransformer;
import org.tdar.transform.jsonld.SchemaOrgResourceTransformer;
import org.tdar.utils.ImmutableScrollableCollection;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;
import com.redfin.sitemapgenerator.GoogleImageSitemapGenerator;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private GenericDao genericDao;
    @Autowired
    private AggregateStatisticsDao aggregateStatisticsDao;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private DataTableDao dataTableDao;
    @Autowired
    private BillingAccountDao accountDao;
    @Autowired
    private SerializationService serializationService;
    @Autowired
    private ResourceCollectionDao resourceCollectionDao;
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private GeoSearchService geoSearchService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findSkeletonsForSearch(boolean, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findSkeletonsForSearch(boolean trustCache, List<Long> ids) {
        return datasetDao.findSkeletonsForSearch(trustCache, ids);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findOld(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findOld(Long... ids) {
        return datasetDao.findOld(ids);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findResourcesSubmittedByUser(org.tdar.core.bean.entity.Person)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> findResourcesSubmittedByUser(Person person) {
        return datasetDao.findResourcesSubmittedByUser(person);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#find(java.lang.Long)
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <R extends Resource> R find(Long id) {
        if (id == null) {
            return null;
        }
        return (R) datasetDao.find(Resource.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findAllSparseActiveResources()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findAllSparseActiveResources() {
        return datasetDao.findAllSparseActiveResources();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findRecentlyUpdatedItemsInLastXDays(int)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findRecentlyUpdatedItemsInLastXDays(int days) {
        return datasetDao.findRecentlyUpdatedItemsInLastXDays(days);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#logResourceModification(T, org.tdar.core.bean.entity.TdarUser, java.lang.String,
     * org.tdar.core.bean.resource.RevisionLogType)
     */
    @Override
    @Transactional
    public <T extends Resource> void logResourceModification(T modifiedResource, TdarUser person, String message, RevisionLogType type) {
        logResourceModification(modifiedResource, person, message, null, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#logResourceModification(T, org.tdar.core.bean.entity.TdarUser, java.lang.String, java.lang.String,
     * org.tdar.core.bean.resource.RevisionLogType, java.lang.Long)
     */
    @Override
    @Transactional
    public <T extends Resource> void logResourceModification(T modifiedResource, TdarUser person, String message, String payload, RevisionLogType type,
            Long startTime) {
        ResourceRevisionLog log = new ResourceRevisionLog(message, modifiedResource, person, type);
        log.setTimestamp(new Date());
        log.setPayload(payload);
        log.setTimeBasedOnStart(startTime);
        genericDao.save(log);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#logResourceModification(T, org.tdar.core.bean.entity.TdarUser, java.lang.String, java.lang.String,
     * org.tdar.core.bean.resource.RevisionLogType)
     */
    @Override
    @Transactional
    public <T extends Resource> void logResourceModification(T modifiedResource, TdarUser person, String message, String payload, RevisionLogType type) {
        logResourceModification(modifiedResource, person, message, payload, type, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findAllStatuses()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Status> findAllStatuses() {
        return Arrays.asList(Status.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#incrementAccessCounter(org.tdar.core.bean.resource.Resource, boolean)
     */
    @Override
    @Transactional(readOnly = false)
    public void incrementAccessCounter(Resource r, boolean b) {
        ResourceAccessStatistic rac = new ResourceAccessStatistic(new Date(), r, b);
        datasetDao.markWritable(rac);
        genericDao.save(rac);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#updateTransientAccessCount(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public void updateTransientAccessCount(Resource resource) {
        resource.setTransientAccessCount(datasetDao.getAccessCount(resource).longValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#countActiveResources(org.tdar.core.bean.resource.ResourceType)
     */
    @Override
    @Transactional(readOnly = true)
    public Number countActiveResources(ResourceType type) {
        return datasetDao.countActiveResources(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#countActiveResourcesWithFiles(org.tdar.core.bean.resource.ResourceType)
     */
    @Override
    @Transactional(readOnly = true)
    public Number countActiveResourcesWithFiles(ResourceType type) {
        return datasetDao.countActiveResourcesWithFiles(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getISOGeographicCounts()
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = Caches.HOMEPAGE_MAP_CACHE)
    public List<HomepageGeographicCache> getISOGeographicCounts() {
        logger.debug("requesting homepage cache");
        return datasetDao.getISOGeographicCounts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getMappedDataForInformationResource(org.tdar.core.bean.resource.InformationResource, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<DataTableColumn, String> getMappedDataForInformationResource(InformationResource resource,TdarUser tdarUser, boolean failOnMissing) {
        try {
            Map<DataTableColumn, String> map = datasetDao.getMappedDataForInformationResource(resource);
            boolean canViewConfidentialInformation = authorizationService.canViewConfidentialInformation(tdarUser, resource);
            map.keySet().forEach(key -> {
                if (key.getVisible() == null) {
                    key.setVisible(ColumnVisibiltiy.VISIBLE);
                }
                switch (key.getVisible()) {
                    case CONFIDENTIAL:
                        if (canViewConfidentialInformation == false) {
                            map.remove(key);
                        }
                        break;
                    case HIDDEN:
                        map.remove(key);
                        break;
                    case VISIBLE:
                    default:
                        break;
                }
            });
            return map;
        } catch (Throwable t) {
            logger.error("could not attach additional dataset data to resource", t);
            if (failOnMissing) {
                throw t;
            }
            return new HashMap<>();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#processManagedKeywords(org.tdar.core.bean.resource.Resource, java.util.Collection)
     */
    @Override
    @Transactional
    public void processManagedKeywords(Resource resource, Collection<LatitudeLongitudeBox> allLatLongBoxes) {
        geoSearchService.processManagedGeographicKeywords(resource, allLatLongBoxes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#saveHasResources(R, boolean, org.tdar.core.service.resource.ResourceServiceImpl.ErrorHandling,
     * java.util.Collection, java.util.Set, java.lang.Class)
     */
    @Override
    @Transactional
    /**
     * Given a collection of hibernate-managed beans (the 'current' collection) and another collection of transient beans (the 'incoming' collection),
     * update the current collection to match the contents of the incoming collection. This method will associate all elements in the incoming collection
     * with the specified resource. Contents of both collections should satisfy the HasResource interface.
     * 
     * @param resource
     *            the 'owner' of the elements in the incoming collection. This method will associate all elements of the incoming collection with this resource.
     * @param shouldSave
     *            if true, this method will persist elements of the incoming collection.
     * @param validateMethod
     *            determines what validation steps (if any) to perform on each element of the incoming collection
     * @param incoming_
     *            the incoming collection of HasResource elements.
     * @param current
     *            the current collection of HasResource elements. This method will modify collection to contain the same elements as the incoming collection.
     * @param cls
     *            type of the collection elements.
     */
    public <H extends HasResource<R>, R extends Resource> void saveHasResources(R resource, boolean shouldSave, ErrorHandling validateMethod,
            Collection<H> incoming__,
            Set<H> current, Class<H> cls) {
        Collection<H> incoming_ = incoming__;
        if (CollectionUtils.isEmpty(incoming_) && CollectionUtils.isEmpty(current)) {
            // skip a complete no-op
            return;
        }

        if (incoming_ == null) {
            incoming_ = new ArrayList<H>();
        }
        Collection<H> incoming = incoming_;
        // there are cases where current and incoming_ are the same object, if that's the case
        // then we need to copy incoming_ before
        if ((incoming_ == current) && !CollectionUtils.isEmpty(incoming_)) {
            incoming = new ArrayList<H>();
            incoming.addAll(incoming_);
            current.clear();
        }

        // assume everything that's incoming is valid or deduped and tied back into tDAR entities/beans
        logger.debug("Current Collection of {}s ({}) : {} ", new Object[] { cls.getSimpleName(), current.size(), current });

        /*
         * Because we're using ID for the equality and hashCode, we have no way to avoid deleting everything and re-adding it.
         * This is an issue as what'll end up happening otherwise is something like editing a Date results in no persisted change because the
         * "retainAll" below keeps the older version
         */

        current.retainAll(incoming);
        Map<Long, H> idMap = PersistableUtils.createIdMap(current);
        if (CollectionUtils.isNotEmpty(incoming)) {
            logger.debug("Incoming Collection of {}s ({})  : {} ", new Object[] { cls.getSimpleName(), incoming.size(), incoming });
            Iterator<H> incomingIterator = incoming.iterator();
            while (incomingIterator.hasNext()) {
                H hasResource_ = incomingIterator.next();

                if (hasResource_ != null) {

                    // attach the incoming notes to a hibernate session
                    logger.trace("adding {} to {} ", hasResource_, current);
                    H existing = idMap.get(hasResource_.getId());
                    /*
                     * If we're not transient, compare the two beans on all of their local properties (non-recursive) -- if there are differences
                     * copy. otherwise, move on. Question -- it may be more work to compare than to just "copy"... is it worth it?
                     */
                    if (PersistableUtils.isNotNullOrTransient(existing) && !EqualsBuilder.reflectionEquals(existing, hasResource_)) {
                        try {
                            logger.trace("copying bean properties for entry in existing set");
                            BeanUtils.copyProperties(existing, hasResource_);
                        } catch (Exception e) {
                            logger.error("exception setting bean property", e);
                        }
                    }

                    if (validateMethod != ErrorHandling.NO_VALIDATION) {
                        boolean isValid = false;
                        if (hasResource_ instanceof ResourceCreator) {
                            isValid = ((ResourceCreator) hasResource_).isValidForResource(resource);
                        } else {
                            isValid = hasResource_.isValid();
                        }

                        if (!isValid) {
                            logger.debug("skipping: {} - INVALID", hasResource_);
                            if (validateMethod == ErrorHandling.VALIDATE_WITH_EXCEPTION) {
                                throw new TdarRecoverableRuntimeException(hasResource_ + " is not valid");
                            }
                            continue;
                        }
                    }

                    current.add(hasResource_);

                    // if (shouldSave) {
                    // getGenericDao().saveOrUpdate(hasResource_);
                    // }
                }
            }
        }
        logger.debug("Resulting Collection of {}s ({}) : {} ", new Object[] { cls.getSimpleName(), current.size(), current });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getResourceCounts()
     */
    @Override
    @Cacheable(value = Caches.HOMEPAGE_RESOURCE_COUNT_CACHE)
    @Transactional(readOnly = true)
    public List<HomepageResourceCountCache> getResourceCounts() {
        return datasetDao.getResourceCounts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getResourceCountAndStatusForUser(org.tdar.core.bean.entity.Person, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceTypeStatusInfo getResourceCountAndStatusForUser(Person p, List<ResourceType> resourceTypes) {
        return datasetDao.getResourceCountAndStatusForUser(p, resourceTypes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getResourceCount(org.tdar.core.bean.resource.ResourceType, org.tdar.core.bean.resource.Status)
     */
    @Override
    @Transactional(readOnly = true)
    public Long getResourceCount(ResourceType resourceType, Status status) {
        return datasetDao.getResourceCount(resourceType, status);
    }

    // /**
    // * Use by the @link BulkUploadService, we use a proxy @link Resource (image) to create a new @link Resource of the specified type.
    // *
    // * @param proxy
    // * @param resourceClass
    // * @return
    // */
    // @Transactional
    // public <T extends Resource> T createResourceFrom(Resource proxy, Class<T> resourceClass) {
    // return createResourceFrom(proxy, resourceClass, true);
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#clearOneToManyIds(T)
     */
    @Override
    public <T extends Resource> void clearOneToManyIds(T resource) {
        datasetDao.clearOneToManyIds(resource, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#createResourceFrom(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource,
     * java.lang.Class, boolean)
     */
    @Override
    @Transactional
    public <T extends Resource> T createResourceFrom(TdarUser authenticatedUser, Resource proxy, Class<T> resourceClass, boolean save) {
        try {
            T resource = resourceClass.newInstance();
            genericDao.detachFromSession(resource);
            resource.setTitle(proxy.getTitle());
            resource.setDescription(proxy.getDescription());
            if (StringUtils.isEmpty(resource.getDescription())) {
                resource.setDescription(" ");
            }
            resource.setDateCreated(proxy.getDateCreated());
            resource.markUpdated(proxy.getSubmitter());
            resource.setStatus(proxy.getStatus());

            resource.getMaterialKeywords().addAll(proxy.getMaterialKeywords());
            resource.getTemporalKeywords().addAll(proxy.getTemporalKeywords());
            resource.getInvestigationTypes().addAll(proxy.getInvestigationTypes());
            resource.getCultureKeywords().addAll(proxy.getCultureKeywords());
            resource.getOtherKeywords().addAll(proxy.getOtherKeywords());
            resource.getSiteNameKeywords().addAll(proxy.getSiteNameKeywords());
            resource.getSiteTypeKeywords().addAll(proxy.getSiteTypeKeywords());
            resource.getGeographicKeywords().addAll(proxy.getGeographicKeywords());
            resource.getManagedGeographicKeywords().addAll(proxy.getManagedGeographicKeywords());
            // CLONE if internal, otherwise just add

            for (AuthorizedUser au : proxy.getAuthorizedUsers()) {
                AuthorizedUser newAuthorizedUser = new AuthorizedUser(authenticatedUser, au.getUser(),
                        au.getGeneralPermission());
                resource.getAuthorizedUsers().add(newAuthorizedUser);

            }
            for (ResourceCollection collection : proxy.getManagedResourceCollections()) {
                if (collection instanceof ResourceCollection) {
                    ResourceCollection shared = (ResourceCollection) collection;
                    logger.info("adding to shared collection : {} ", collection);
                    if (collection.isTransient() && save) {
                        genericDao.save(shared);
                    } else {
                        // manage session (when called from bulk upload, need to make sure we're on the session, as the incomming may be on a different thread)
                        shared = genericDao.find(ResourceCollection.class, shared.getId());
                    }
                    shared.getManagedResources().add(resource);
                    resource.getManagedResourceCollections().add(shared);
                } else {
                    throw new TdarRecoverableRuntimeException("resourceService.invalid_collectiontype");
                }
            }

            cloneSet(resource, resource.getCoverageDates(), proxy.getCoverageDates());
            cloneSet(resource, resource.getLatitudeLongitudeBoxes(), proxy.getLatitudeLongitudeBoxes());
            cloneSet(resource, resource.getResourceCreators(), proxy.getResourceCreators());
            cloneSet(resource, resource.getResourceAnnotations(), proxy.getResourceAnnotations());
            cloneSet(resource, resource.getResourceNotes(), proxy.getResourceNotes());
            cloneSet(resource, resource.getRelatedComparativeCollections(), proxy.getRelatedComparativeCollections());
            cloneSet(resource, resource.getSourceCollections(), proxy.getSourceCollections());

            if ((resource instanceof InformationResource) && (proxy instanceof InformationResource)) {
                InformationResource proxyInformationResource = (InformationResource) proxy;
                InformationResource informationResource = (InformationResource) resource;
                informationResource.setDate(proxyInformationResource.getDate());
                // force project into the session
                if (PersistableUtils.isNotNullOrTransient(proxyInformationResource.getProject())) {
                    Project project = proxyInformationResource.getProject();
                    // refresh(project);
                    informationResource.setProject(project);
                }
                informationResource.setPublisher(proxyInformationResource.getPublisher());
                informationResource.setCopyrightHolder(proxyInformationResource.getCopyrightHolder());
                informationResource.setLicenseText(proxyInformationResource.getLicenseText());
                informationResource.setLicenseType(proxyInformationResource.getLicenseType());
                informationResource.setPublisherLocation(proxyInformationResource.getPublisherLocation());
                informationResource.setResourceProviderInstitution(proxyInformationResource.getResourceProviderInstitution());
                informationResource.setResourceLanguage(proxyInformationResource.getResourceLanguage());
                informationResource.setMetadataLanguage(proxyInformationResource.getMetadataLanguage());
                informationResource.setInheritingCulturalInformation(proxyInformationResource.isInheritingCulturalInformation());
                informationResource.setInheritingInvestigationInformation(proxyInformationResource.isInheritingInvestigationInformation());
                informationResource.setInheritingMaterialInformation(proxyInformationResource.isInheritingMaterialInformation());
                informationResource.setInheritingOtherInformation(proxyInformationResource.isInheritingOtherInformation());
                informationResource.setInheritingSiteInformation(proxyInformationResource.isInheritingSiteInformation());
                informationResource.setInheritingSpatialInformation(proxyInformationResource.isInheritingSpatialInformation());
                informationResource.setInheritingTemporalInformation(proxyInformationResource.isInheritingTemporalInformation());
                informationResource.setInheritingIdentifierInformation(proxyInformationResource.isInheritingIdentifierInformation());
                informationResource.setInheritingNoteInformation(proxyInformationResource.isInheritingNoteInformation());
                informationResource.setInheritingCollectionInformation(proxyInformationResource.isInheritingCollectionInformation());
                informationResource.setInheritingIndividualAndInstitutionalCredit(proxyInformationResource.isInheritingIndividualAndInstitutionalCredit());
            }
            return resource;
            // NOTE: THIS SHOULD BE THE LAST THING DONE AS IT BRINGS EVERYTHING BACK ONTO THE SESSION PROPERLY
        } catch (Exception exception) {
            logger.error("{}", exception, exception);
            throw new TdarRuntimeException(exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#cloneSet(org.tdar.core.bean.resource.Resource, java.util.Set, java.util.Set)
     */
    @Override
    @Transactional
    public <T extends HasResource<Resource>> Set<T> cloneSet(Resource resource, Set<T> targetCollection, Set<T> sourceCollection) {
        logger.trace("cloning: " + sourceCollection);
        for (T t : sourceCollection) {
            datasetDao.detachFromSessionAndWarn(t);
            try {
                @SuppressWarnings("unchecked")
                T clone = (T) BeanUtils.cloneBean(t);
                targetCollection.add(clone);
            } catch (Exception e) {
                logger.warn("Exception in clone set: {} ", e);
            }
        }
        // getDao().save(targetCollection);
        return targetCollection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getResourceSpaceUsageStatistics(java.util.List, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceSpaceUsageStatistic getResourceSpaceUsageStatistics(List<Long> resourceId, List<Status> status) {
        return datasetDao.getResourceSpaceUsageStatistics(resourceId, status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getResourceSpaceUsageStatisticsForProject(java.lang.Long, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceSpaceUsageStatistic getResourceSpaceUsageStatisticsForProject(Long id, List<Status> status) {
        return datasetDao.getResourceSpaceUsageStatisticsForProject(id, status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getSpaceUsageForCollections(java.util.List, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceSpaceUsageStatistic getSpaceUsageForCollections(List<Long> collectionId, List<Status> statuses) {
        return datasetDao.getSpaceUsageForCollections(collectionId, statuses);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getResourceSpaceUsageStatisticsForUser(java.util.List, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceSpaceUsageStatistic getResourceSpaceUsageStatisticsForUser(List<Long> accountId, List<Status> status) {
        return datasetDao.getResourceSpaceUsageStatisticsForUser(accountId, status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getLogsForResource(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceRevisionLog> getLogsForResource(Resource resource) {
        if (PersistableUtils.isNullOrTransient(resource)) {
            return Collections.emptyList();
        }
        return datasetDao.getLogEntriesForResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findAllResourceIdsWithFiles()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllResourceIdsWithFiles() {
        return datasetDao.findAllResourceIdsWithFiles();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getAllResourceTypes()
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceType> getAllResourceTypes() {
        ArrayList<ResourceType> arrayList = new ArrayList<>(Arrays.asList(ResourceType.values()));
        if (!TdarConfiguration.getInstance().isVideoEnabled()) {
            arrayList.remove(ResourceType.VIDEO);
            arrayList.remove(ResourceType.AUDIO);
        }

        if (!TdarConfiguration.getInstance().isArchiveFileEnabled()) {
            arrayList.remove(ResourceType.ARCHIVE);
        }
        return arrayList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findAllResourcesWithPublicImagesForSitemap(com.redfin.sitemapgenerator.GoogleImageSitemapGenerator)
     */
    @Override
    @Transactional
    public int findAllResourcesWithPublicImagesForSitemap(GoogleImageSitemapGenerator gisg) {
        return datasetDao.findAllResourcesWithPublicImagesForSitemap(gisg);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findByTdarYear(org.tdar.search.query.SearchResultHandler, int)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findByTdarYear(SearchResultHandler<Resource> resultHandler, int year) {
        return datasetDao.findByTdarYear(resultHandler, year);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getWeeklyPopularResources()
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = Caches.WEEKLY_POPULAR_RESOURCE_CACHE)
    public List<Resource> getWeeklyPopularResources() {
        return getWeeklyPopularResources(10);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getWeeklyPopularResources(int)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> getWeeklyPopularResources(int count) {
        int max = count;
        DateTime end = new DateTime();
        DateTime start = end.minusDays(7);
        List<Resource> popular = aggregateStatisticsDao.getWeeklyPopularResources(count);
        return popular;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> getMostPopularResourcesForBillingAccount(BillingAccount billingAccount, int limit) {
        return aggregateStatisticsDao.getMostPopularResourcesForBillingAccount(billingAccount, limit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#saveResourceCreatorsFromProxies(java.util.Collection, org.tdar.core.bean.resource.Resource, boolean)
     */
    @Override
    @Transactional(readOnly = false)
    public void saveResourceCreatorsFromProxies(Collection<ResourceCreatorProxy> allProxies, Resource resource, boolean shouldSaveResource) {
        logger.info("ResourceCreators before DB lookup: {} ", allProxies);
        int sequence = 0;
        List<ResourceCreator> incomingResourceCreators = new ArrayList<>();
        // convert the list of proxies to a list of resource creators
        for (ResourceCreatorProxy proxy : allProxies) {
            if ((proxy != null) && proxy.isValid()) {
                ResourceCreator resourceCreator = proxy.getResourceCreator();
                resourceCreator.setSequenceNumber(sequence++);
                logger.trace("{} - {}", resourceCreator, resourceCreator.getCreatorType());

                entityService.findOrSaveResourceCreator(resourceCreator);
                incomingResourceCreators.add(resourceCreator);
                logger.trace("{} - {}", resourceCreator, resourceCreator.getCreatorType());
            } else {
                logger.trace("can't create creator from proxy {} {}", proxy);
            }
        }

        // FIXME: Should this throw errors?
        saveHasResources(resource, shouldSaveResource, ErrorHandling.VALIDATE_SKIP_ERRORS, incomingResourceCreators,
                resource.getResourceCreators(), ResourceCreator.class);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getDeletionIssues(com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public DeleteIssue getDeletionIssues(TextProvider provider, Resource persistable) {
        DeleteIssue issue = new DeleteIssue();
        switch (persistable.getResourceType()) {
            case PROJECT:
                ScrollableResults findAllResourcesInProject = projectDao.findAllResourcesInProject((Project) persistable, Status.ACTIVE, Status.DRAFT);
                Set<InformationResource> inProject = new HashSet<>();
                for (InformationResource ir : new ImmutableScrollableCollection<InformationResource>(findAllResourcesInProject)) {
                    inProject.add(ir);
                }
                if (CollectionUtils.isNotEmpty(inProject)) {
                    issue.getRelatedItems().addAll(inProject);
                    issue.setIssue(provider.getText("resourceDeleteController.delete_project"));
                    return issue;
                }
                return null;
            case CODING_SHEET:
            case ONTOLOGY:
                List<DataTable> related = dataTableDao.findDataTablesUsingResource(persistable);
                if (CollectionUtils.isNotEmpty(related)) {
                    for (DataTable table : related) {
                        Dataset dataset = dataTableDao.findDatasetForTable(table);
                        if (!dataset.isDeleted()) {
                            issue.getRelatedItems().add(dataset);
                        }
                    }
                    issue.setIssue(provider.getText("abstractSupportingInformationResourceController.remove_mappings"));
                    return issue;
                }
                return null;
            default:
                return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#deleteForController(org.tdar.core.bean.resource.Resource, java.lang.String,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void deleteForController(Resource resource, String reason_, TdarUser authUser) {
        String reason = reason_;
        if (StringUtils.isNotEmpty(reason)) {
            ResourceNote note = new ResourceNote(ResourceNoteType.ADMIN, reason);
            resource.getResourceNotes().add(note);
            genericDao.save(resource);
            genericDao.save(note);
        } else {
            reason = "reason not specified";
        }
        String logMessage = String.format("%s id:%s deleted by:%s reason: %s", resource.getResourceType().name(), resource.getId(), authUser, reason);
        logResourceModification(resource, authUser, logMessage, null, RevisionLogType.DELETE);
        resource.markUpdated(authUser);
        genericDao.delete(resource);

        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            Collection<Resource> toEvaluate = Arrays.asList(resource);
            accountDao.updateTransientAccountOnResources(toEvaluate);
            BillingAccount account = resource.getAccount();
            if (account == null) {
                // if we're null, we'll assign it to the "first" account
                List<BillingAccount> accountsForUser = accountDao.findAccountsForUser(authUser);
                if (CollectionUtils.isNotEmpty(accountsForUser)) {
                    account = accountsForUser.get(0);
                    account.getResources().add(resource);
                    resource.setAccount(account);
                }
            }

            // if we have an account ... for some things we might not
            if (account != null) {
                account = genericDao.markWritableOnExistingSession(account);
                accountDao.updateQuota(account, toEvaluate, authUser);
                genericDao.saveOrUpdate(account);
            }
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findAllActiveScrollableForSitemap()
     */
    @Override
    @Transactional(readOnly = true)
    public ScrollableResults findAllActiveScrollableForSitemap() {
        return datasetDao.findAllActiveScrollableForSitemap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictHomepageMapCache()
     */
    @Override
    @CacheEvict(value = Caches.HOMEPAGE_MAP_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictHomepageMapCache() {
        logger.debug("evicting homepage cache");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictResourceCountCache()
     */
    @Override
    @CacheEvict(value = Caches.HOMEPAGE_RESOURCE_COUNT_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictResourceCountCache() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictDecadeCountCache()
     */
    @Override
    @CacheEvict(value = Caches.DECADE_COUNT_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictDecadeCountCache() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictBrowseYearCountCache()
     */
    @Override
    @CacheEvict(value = Caches.BROWSE_DECADE_COUNT_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictBrowseYearCountCache() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictPopularResourceCache()
     */
    @Override
    @CacheEvict(value = Caches.WEEKLY_POPULAR_RESOURCE_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictPopularResourceCache() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictHomepageFeaturedItemCache()
     */
    @Override
    @CacheEvict(value = Caches.HOMEPAGE_FEATURED_ITEM_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictHomepageFeaturedItemCache() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#evictBrowseYearCountcache()
     */
    @Override
    @CacheEvict(value = Caches.BROWSE_YEAR_COUNT_CACHE, allEntries = true)
    @Transactional(readOnly = false)
    public void evictBrowseYearCountcache() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findMappedResources()
     */
    @Override
    @Transactional(readOnly = true)
    public ScrollableResults findMappedResources() {
        return findMappedResources(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findMappedResources(org.tdar.core.bean.resource.Project)
     */
    @Override
    @Transactional(readOnly = true)
    public ScrollableResults findMappedResources(Project p) {
        return datasetDao.findMappedResources(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getGoogleScholarTags(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public String getGoogleScholarTags(Resource resource) {
        StringWriter sw = new StringWriter();
        try {
            ScholarMetadataTransformer trans = new ScholarMetadataTransformer();
            for (MetaTag tag : trans.convertResourceToMetaTag(resource)) {
                serializationService.convertToXMLFragment(MetaTag.class, tag, sw);
                sw.append("\n");
            }
        } catch (Exception e) {
            logger.error("error converting scholar tag for resource:", resource, e);
        }
        return sw.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#getSchemaOrgJsonLD(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public String getSchemaOrgJsonLD(Resource resource) {
        try {
            SchemaOrgResourceTransformer transformer = new SchemaOrgResourceTransformer();
            return transformer.convert(serializationService, resource);
        } catch (Exception e) {
            logger.error("error converting to json-ld", e);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#updateBatch(org.tdar.core.bean.billing.BillingAccount,
     * org.tdar.core.bean.collection.SharedCollection, java.util.List, java.util.List, java.util.List, java.util.List, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateBatch(BillingAccount account, ResourceCollection collectionToAdd, List<Long> ids, List<Integer> dates, List<String> titles,
            List<String> descriptions, TdarUser authenticatedUser) {
        List<Resource> resources = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Integer date = dates.get(i);
            String title = titles.get(i);
            String description = descriptions.get(i);
            Resource r = genericDao.find(Resource.class, id);
            r = genericDao.markWritableOnExistingSession(r);
            resources.add(r);
            boolean different = false;
            if (!Objects.equals(title, r.getTitle())) {
                different = true;
                r.setTitle(title);
            }
            if (!Objects.equals(description, r.getDescription())) {
                different = true;
                r.setDescription(description);
            }

            if (r instanceof InformationResource) {
                InformationResource ir = (InformationResource) r;
                if (!Objects.equals(date, ir.getDate())) {
                    different = true;
                    ir.setDate(date);
                }
            }
            if (PersistableUtils.isNotNullOrTransient(collectionToAdd) && !r.getManagedResourceCollections().contains(collectionToAdd)) {
                r.getManagedResourceCollections().add(collectionToAdd);
                collectionToAdd.getManagedResources().add(r);
            }
            if (different) {
                ResourceRevisionLog rrl = new ResourceRevisionLog("Resource batch modified (basic)", r, authenticatedUser, RevisionLogType.EDIT);
                genericDao.saveOrUpdate(rrl);
                r.markUpdated(authenticatedUser);
                genericDao.saveOrUpdate(r);
            }
            logger.debug("processed: {}", r);
            // throw new TdarRecoverableRuntimeException("error.not_implemented");
        }

        if (PersistableUtils.isNotNullOrTransient(account)) {
            accountDao.updateQuota(account, resources, authenticatedUser);
        }

        if (PersistableUtils.isNotNullOrTransient(collectionToAdd)) {
            genericDao.saveOrUpdate(collectionToAdd);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.ResourceService#findCustom(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public RequestCollection findCustom(Resource resource) {
        return resourceCollectionDao.findCustomRequest(resource);
    }

}
