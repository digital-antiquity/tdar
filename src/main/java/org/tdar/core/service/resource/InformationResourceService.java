package org.tdar.core.service.resource;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.cache.BrowseDecadeCountCache;
import org.tdar.core.cache.BrowseYearCountCache;
import org.tdar.core.cache.Caches;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.InformationResourceDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.utils.PersistableUtils;

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
    private ResourceCollectionDao resourceCollectionDao;

    /**
     * Find all Resources ... not suggested
     */
    @Transactional(readOnly = true)
    public List<InformationResource> findAllResources() {
        return getDao().findAll();
    }

    /**
     * Generate the BrowseByDecatedCountCache for a set of @link Status (s).
     * 
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    @Cacheable(value = Caches.BROWSE_DECADE_COUNT_CACHE)
    public List<BrowseDecadeCountCache> findResourcesByDecade() {
        return getDao().findResourcesByDecade(Status.ACTIVE);
    }

    /**
     * Find an @link InformationResourceFile by it's filename when specifying the @link InformationResourceFile
     * 
     * @param resource
     * @param filename
     * @return
     */
    @Transactional(readOnly = true)
    public InformationResourceFile findFileByFilename(InformationResource resource, String filename) {
        return getDao().findFileByFilename(resource, filename);
    }

    /**
     * Find a random set of resources to be featured on the homepage ...
     * 
     * @param restrictToFiles
     *            show only resources with Files
     * @param maxResults
     *            how many to return
     * @return
     */
    public <E extends Resource> List<E> findRandomFeaturedResource(boolean restrictToFiles, int maxResults) {
        return getDao().findRandomFeaturedResource(restrictToFiles, maxResults);
    }

    /**
     * Find a random set of resources, but limit them to be part of a project for the homepage
     * 
     * @param restrictToFiles
     * @param project
     * @param maxResults
     * @return
     */
    public <E extends Resource> List<E> findRandomFeaturedResourceInProject(boolean restrictToFiles, Project project, int maxResults) {
        return getDao().findRandomFeaturedResourceInProject(restrictToFiles, project, maxResults);
    }

    /**
     * Find a random set of resources, but limit them to be part of a collection for the homepage
     * 
     * @param restrictToFiles
     * @param collectionId
     * @param maxResults
     * @return
     */
    public <E extends Resource> List<E> findRandomFeaturedResourceInCollection(boolean restrictToFiles, Long collectionId, int maxResults) {
        List<ResourceCollection> collections = null;
        if (PersistableUtils.isNotNullOrTransient(collectionId)) {
            collections = resourceCollectionDao.findCollectionsOfParent(collectionId, false, CollectionType.SHARED);
            return getDao().findRandomFeaturedResourceInCollection(restrictToFiles, collections, maxResults);
        }
        return findRandomFeaturedResource(restrictToFiles, maxResults);
    }

    /**
     * Generate the BrowseByYearCountCache for a set of @link Status (s).
     * 
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    @Cacheable(value = Caches.BROWSE_YEAR_COUNT_CACHE)
    public List<BrowseYearCountCache> findResourceCountsByYear() {
        return getDao().findResourcesByYear(Status.ACTIVE);
    }

    @Cacheable(value = Caches.HOMEPAGE_FEATURED_ITEM_CACHE)
    @Transactional(readOnly=true)
    public List<Resource> getFeaturedItems() {
        Long featuredCollectionId = TdarConfiguration.getInstance().getFeaturedCollectionId();
        return  findRandomFeaturedResourceInCollection(true, featuredCollectionId, 5);
    }

    @Transactional(readOnly = true)
    public InformationResource findByDoi(String doi) {
        return getDao().findByDoi(doi);
    }

}
