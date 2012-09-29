package org.tdar.core.service.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.cache.BrowseYearCountCache;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.InformationResourceDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.SearchIndexService;

/**
 * $Id$
 * 
 * 
 * @author Matt Cordial
 * @version $Rev$
 */

@Service("informationResourceService")
@Transactional
public class InformationResourceService extends AbstractInformationResourceService<InformationResource, InformationResourceDao> {

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Transactional(readOnly = true)
    public List<InformationResource> findAllResources() {
        return getDao().findAll();
    }

    @Transactional(readOnly = true)
    public List<BrowseYearCountCache> findResourcesByDecade(Status... statuses) {
        return getDao().findResourcesByDecade(statuses);
    }

    @Transactional(readOnly = true)
    public InformationResourceFile findFileByFilename(InformationResource resource, String filename) {
        return getDao().findFileByFilename(resource, filename);
    }

    @Transactional
    public <T extends Resource> T createResourceFrom(InformationResource proxy, Class<T> resourceClass) {
        try {
            T resource = resourceClass.newInstance();
            resource.setTitle(proxy.getTitle());
            resource.setDateCreated(proxy.getDateCreated());
            resource.markUpdated(proxy.getSubmitter());
            resource.setStatus(proxy.getStatus());
            saveOrUpdate(resource);
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
            for (ResourceCollection collection : proxy.getResourceCollections()) {
                if (collection.isInternal()) {
                    logger.info("cloning collection: {}", collection);
                    ResourceCollection newInternal = new ResourceCollection(CollectionType.INTERNAL);
                    newInternal.setName(collection.getName());
                    newInternal.markUpdated(collection.getOwner());
                    getDao().save(newInternal);

                    for (AuthorizedUser proxyAuthorizedUser : collection.getAuthorizedUsers()) {
                        AuthorizedUser newAuthorizedUser = new AuthorizedUser(proxyAuthorizedUser.getUser(),
                                proxyAuthorizedUser.getGeneralPermission());
                        newAuthorizedUser.setResourceCollection(newInternal);
                        newInternal.getAuthorizedUsers().add(newAuthorizedUser);
                        getDao().save(newAuthorizedUser);
                    }
                    resource.getResourceCollections().add(newInternal);
                    newInternal.getResources().add(resource);
                } else {
                    logger.info("adding to shared collection : {} ", collection);
                    if (collection.isTransient()) {
                        save(collection);
                    }

                    collection.getResources().add(resource);
                    resource.getResourceCollections().add(collection);
                }
            }

            resource.getCoverageDates().addAll(cloneSet(resource, proxy.getCoverageDates()));
            resource.getLatitudeLongitudeBoxes().addAll(cloneSet(resource, proxy.getLatitudeLongitudeBoxes()));
            resource.getResourceCreators().addAll(cloneSet(resource, proxy.getResourceCreators()));
            resource.getResourceAnnotations().addAll(cloneSet(resource, proxy.getResourceAnnotations()));
            resource.getResourceNotes().addAll(cloneSet(resource, proxy.getResourceNotes()));
            resource.getRelatedComparativeCollections().addAll(cloneSet(resource, proxy.getRelatedComparativeCollections()));
            resource.getSourceCollections().addAll(cloneSet(resource, proxy.getSourceCollections()));

            if (resource instanceof InformationResource) {
                InformationResource ires = (InformationResource) resource;
                ires.setDate(proxy.getDate());
                ires.setProject(proxy.getProject());
                ires.setResourceProviderInstitution(proxy.getResourceProviderInstitution());
                ires.setDateMadePublic(proxy.getDateMadePublic());
                ires.setResourceLanguage(proxy.getResourceLanguage());
                ires.setMetadataLanguage(proxy.getMetadataLanguage());
                ires.setAvailableToPublic(proxy.isAvailableToPublic());
                ires.setInheritingCulturalInformation(proxy.isInheritingCulturalInformation());
                ires.setInheritingInvestigationInformation(proxy.isInheritingInvestigationInformation());
                ires.setInheritingMaterialInformation(proxy.isInheritingMaterialInformation());
                ires.setInheritingOtherInformation(proxy.isInheritingOtherInformation());
                ires.setInheritingSiteInformation(proxy.isInheritingSiteInformation());
                ires.setInheritingSpatialInformation(proxy.isInheritingSpatialInformation());
                ires.setInheritingTemporalInformation(proxy.isInheritingTemporalInformation());
                ires.setInheritingIdentifierInformation(proxy.isInheritingIdentifierInformation());
                ires.setInheritingNoteInformation(proxy.isInheritingNoteInformation());
                ires.setInheritingCollectionInformation(proxy.isInheritingCollectionInformation());
            }
            
            // NOTE: THIS SHOULD BE THE LAST THING DONE AS IT BRINGS EVERYTHING BACK ONTO THE SESSION PROPERLY
            getDao().merge(resource);
            return resource;
        } catch (Exception exception) {
            throw new TdarRuntimeException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends HasResource<Resource>> Set<T> cloneSet(Resource resource, Collection<T> resourceCollection) {
        logger.debug("cloning collection: " + resourceCollection);
        HashSet<T> clonedSet = new HashSet<T>();
        for (T t : resourceCollection) {
            getDao().detachFromSession(t);
            try {
                T clone = (T) BeanUtils.cloneBean(t);
                clone.setResource(resource);
//                if (clone instanceof ResourceCreator) {
//                    ResourceCreator creator = (ResourceCreator)clone;
//                    creator.setCreator(getDao().merge(creator.getCreator()));
//                }
                clonedSet.add(clone);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        getDao().save(clonedSet);
        return clonedSet;
    }

    @Async
    public void updateProjectIndex(final Long projectId) {
        if (projectId == null)
            return;
        logger.debug("re-indexing project: {}", projectId);
        searchIndexService.index(getDao().find(Project.class, projectId));
    }

    public <E> List<E> findRandomFeaturedResource(boolean restrictToFiles, int maxResults) {
        return getDao().findRandomFeaturedResource(restrictToFiles, maxResults);
    }

    public <E> List<E> findRandomFeaturedResourceInProject(boolean restrictToFiles, Project project, int maxResults) {
        return getDao().findRandomFeaturedResourceInProject(restrictToFiles, project, maxResults);
    }

    public <E> List<E> findRandomFeaturedResourceInCollection(boolean restrictToFiles, Long collectionId, int maxResults) {
        List<ResourceCollection> collections = null;
        if (!Persistable.Base.isNullOrTransient(collectionId)) {
            collections = resourceCollectionDao.findCollectionsOfParent(collectionId, true, CollectionType.SHARED);
            return getDao().findRandomFeaturedResourceInCollection(restrictToFiles, collections, maxResults);
        }
        return findRandomFeaturedResource(restrictToFiles, maxResults);
    }

}
