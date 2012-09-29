package org.tdar.core.service.resource;

import java.lang.reflect.InvocationTargetException;
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
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.dao.resource.InformationResourceDao;
import org.tdar.core.exception.TdarRuntimeException;
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
    public void setDao(InformationResourceDao dao) {
        super.setDao(dao);
    }

    @Transactional(readOnly = true)
    public List<InformationResource> findAllResources() {
        return getDao().findAll();
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
            resource.setDateRegistered(proxy.getDateRegistered());
            resource.markUpdated(proxy.getSubmitter());
            resource.setStatus(proxy.getStatus());
            saveOrUpdate(resource);
            resource.setCultureKeywords(new HashSet<CultureKeyword>(proxy.getCultureKeywords()));
            resource.setInvestigationTypes(new HashSet<InvestigationType>(proxy.getInvestigationTypes()));
            resource.setOtherKeywords(new HashSet<OtherKeyword>(proxy.getOtherKeywords()));
            resource.setSiteNameKeywords(new HashSet<SiteNameKeyword>(proxy.getSiteNameKeywords()));
            resource.setSiteTypeKeywords(new HashSet<SiteTypeKeyword>(proxy.getSiteTypeKeywords()));
            resource.setGeographicKeywords(new HashSet<GeographicKeyword>(proxy.getGeographicKeywords()));
            resource.setManagedGeographicKeywords(new HashSet<GeographicKeyword>(proxy.getManagedGeographicKeywords()));
            // CLONE if internal, otherwise just add

            for (ResourceCollection collection : proxy.getResourceCollections()) {
                logger.info("cloning collection: {}", collection);
                if (collection.isInternal()) {
                    ResourceCollection newInternal = new ResourceCollection(CollectionType.INTERNAL);
                    newInternal.setName(collection.getName());
                    newInternal.markUpdated(collection.getOwner());
                    getDao().save(newInternal);

                    for(AuthorizedUser proxyAuthorizedUser : collection.getAuthorizedUsers())  {
                        AuthorizedUser newAuthorizedUser = new AuthorizedUser(proxyAuthorizedUser.getUser(), proxyAuthorizedUser.getGeneralPermission());
                        newAuthorizedUser.setResourceCollection(newInternal);
                        newInternal.getAuthorizedUsers().add(newAuthorizedUser);
                        getDao().save(newAuthorizedUser);
                    }
                    resource.getResourceCollections().add(newInternal);
                    newInternal.getResources().add(resource);
                } else {
                    logger.info("shared collection : {} ", collection);
                    if (collection.isTransient()) {
                        save(collection);
                    }
                    collection.getResources().add(resource);
                    resource.getResourceCollections().add(collection);
                }
            }
            resource.setLatitudeLongitudeBoxes(new HashSet<LatitudeLongitudeBox>(proxy.getLatitudeLongitudeBoxes()));
            resource.setMaterialKeywords(new HashSet<MaterialKeyword>(proxy.getMaterialKeywords()));
            resource.setTemporalKeywords(new HashSet<TemporalKeyword>(proxy.getTemporalKeywords()));
            resource.setCoverageDates(proxy.getCoverageDates());

            resource.setResourceCreators(cloneSet(resource, proxy.getResourceCreators()));
            resource.setResourceAnnotations(cloneSet(resource, proxy.getResourceAnnotations()));
            resource.setResourceNotes(cloneSet(resource, proxy.getResourceNotes()));
            resource.setRelatedComparativeCollections(cloneSet(resource, proxy.getRelatedComparativeCollections()));
            resource.setSourceCollections(cloneSet(resource, proxy.getSourceCollections()));

            if (resource instanceof InformationResource) {
                InformationResource ires = (InformationResource) resource;
                ires.setDateCreated(proxy.getDateCreated());
                ires.setProject(proxy.getProject());
                ires.setDateMadePublic(proxy.getDateMadePublic());
                ires.setResourceProviderInstitution(proxy.getResourceProviderInstitution());
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
            }
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
                // FIXME: can replace this with a common interface (setResource/getResource) on ResourceAnnotation/Notes/Read/FullUsers
                clone.setResource(resource);
                clonedSet.add(clone);
                if (clone instanceof ResourceNote) {
                    logger.debug("resource note has resource: " + ((ResourceNote) clone).getResource());
                }
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
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

}
