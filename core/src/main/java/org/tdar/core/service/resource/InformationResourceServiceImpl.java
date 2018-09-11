package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.cache.BrowseDecadeCountCache;
import org.tdar.core.cache.BrowseYearCountCache;
import org.tdar.core.cache.Caches;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.workflow.WorkflowResult;
import org.tdar.fileprocessing.workflows.WorkflowContext;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.personal.PersonalFilestore;
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
public class InformationResourceServiceImpl extends ServiceInterface.TypedDaoBase<InformationResource, InformationResourceDao>
        implements InformationResourceService {

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    private static final TdarConfiguration config = TdarConfiguration.getInstance();

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;
    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    private FileAnalyzer analyzer;

    /*
     * Given a @link Resource and list of @link FileProxy objects, process the files and report any errors to the @link ActionMessageErrorSupport listener,
     * which is likely a controller.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#importFileProxiesAndProcessThroughWorkflow(T, org.tdar.core.bean.entity.TdarUser,
     * java.lang.Long, java.util.List)
     */
    @Override
    @Transactional
    public <T extends InformationResource> ErrorTransferObject importFileProxiesAndProcessThroughWorkflow(T resource, TdarUser user, Long ticketId,
            List<FileProxy> fileProxiesToProcess) throws IOException {
        if (CollectionUtils.isEmpty(fileProxiesToProcess)) {
            getLogger().debug("Nothing to process, returning.");
            return null;
        }

        // prepare the metadata
        FileProxyWrapper wrapper = new FileProxyWrapper(resource, analyzer, datasetDao, fileProxiesToProcess);

        wrapper.processMetadataForFileProxies();

        analyzer.processFiles(resource.getResourceType(), wrapper.getFilesToProcess(), resource.getResourceType().isCompositeFilesEnabled());

        /*
         * FIXME: When we move to an asynchronous model, this section and below will need to be moved into their own dedicated method
         */
        WorkflowResult workflowResult = new WorkflowResult(fileProxiesToProcess);
        ErrorTransferObject errorsAndMessages = workflowResult.getActionErrorsAndMessages();

        // If successful and no errors:
        // purge the filestore
        // mark the uploaded files as "read only"
        if (workflowResult.isSuccess()) {
            List<FileStoreFileProxy> proxies = new ArrayList<>();
            for (InformationResourceFileVersion file : wrapper.getFilesToProcess()) {
                proxies.add(file);
            }
            config.getFilestore().markReadOnly(FilestoreObjectType.RESOURCE, proxies);
        }
        if (ticketId != null) {
            PersonalFilestore personalFilestore = personalFilestoreService.getPersonalFilestore(user);
            personalFilestore.purge(getDao().find(PersonalFilestoreTicket.class, ticketId));
        }
        return errorsAndMessages;
    }

    /*
     * Given an @link InformationResource, find all of the latest versions and reprocess them.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#reprocessInformationResourceFiles(T)
     */
    @Override
    @Transactional(readOnly = false)
    public <T extends InformationResource> ErrorTransferObject reprocessInformationResourceFiles(T ir) throws Exception {
        List<InformationResourceFileVersion> latestVersions = new ArrayList<>();
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            if (irFile.isDeleted()) {
                continue;
            }
            InformationResourceFileVersion original = irFile.getLatestUploadedVersion();
            original.setTransientFile(config.getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, original));
            latestVersions.add(original);
            Iterator<InformationResourceFileVersion> iterator = irFile.getInformationResourceFileVersions().iterator();
            while (iterator.hasNext()) {
                InformationResourceFileVersion version = iterator.next();
                if (!version.equals(original) && !version.isUploaded() && !version.isArchival()) {
                    iterator.remove();
                    informationResourceFileDao.deleteVersionImmediately(version);
                }
            }
        }
        analyzer.processFiles(ir.getResourceType(), latestVersions, ir.getResourceType().isCompositeFilesEnabled());
        // this is a known case where we need to purge the session
        // getDao().synchronize();

        ErrorTransferObject eto = null;
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            final WorkflowContext workflowContext = irFile.getWorkflowContext();
            // may be null for "skipped" or composite file
            if ((workflowContext != null) && !workflowContext.isProcessedSuccessfully()) {
                WorkflowResult workflowResult = new WorkflowResult(workflowContext);
                eto = workflowResult.getActionErrorsAndMessages();
            }
        }
        return eto;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findAllResources()
     */
    @Override
    @Transactional(readOnly = true)
    public List<InformationResource> findAllResources() {
        return getDao().findAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findResourcesByDecade()
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = Caches.BROWSE_DECADE_COUNT_CACHE)
    public List<BrowseDecadeCountCache> findResourcesByDecade() {
        return getDao().findResourcesByDecade(Status.ACTIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findFileByFilename(org.tdar.core.bean.resource.InformationResource, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public InformationResourceFile findFileByFilename(InformationResource resource, String filename) {
        return getDao().findFileByFilename(resource, filename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findRandomFeaturedResource(boolean, int)
     */
    @Override
    public <E extends Resource> List<E> findRandomFeaturedResource(boolean restrictToFiles, int maxResults) {
        return getDao().findRandomFeaturedResource(restrictToFiles, maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findRandomFeaturedResourceInProject(boolean, org.tdar.core.bean.resource.Project, int)
     */
    @Override
    public <E extends Resource> List<E> findRandomFeaturedResourceInProject(boolean restrictToFiles, Project project, int maxResults) {
        return getDao().findRandomFeaturedResourceInProject(restrictToFiles, project, maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findRandomFeaturedResourceInCollection(boolean, java.lang.Long, int)
     */
    @Override
    public <E extends Resource> List<E> findRandomFeaturedResourceInCollection(boolean restrictToFiles, Long collectionId, int maxResults) {
        List<ResourceCollection> collections = null;
        if (PersistableUtils.isNotNullOrTransient(collectionId)) {
            collections.addAll(resourceCollectionDao.findCollectionsOfParent(collectionId, false));
            return getDao().findRandomFeaturedResourceInCollection(restrictToFiles, collections, maxResults);
        }
        return findRandomFeaturedResource(restrictToFiles, maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findResourceCountsByYear()
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = Caches.BROWSE_YEAR_COUNT_CACHE)
    public List<BrowseYearCountCache> findResourceCountsByYear() {
        return getDao().findResourcesByYear(Status.ACTIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#getFeaturedItems()
     */
    @Override
    @Cacheable(value = Caches.HOMEPAGE_FEATURED_ITEM_CACHE)
    @Transactional(readOnly = true)
    public List<Resource> getFeaturedItems() {
        Long featuredCollectionId = TdarConfiguration.getInstance().getFeaturedCollectionId();
        return findRandomFeaturedResourceInCollection(true, featuredCollectionId, 5);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#findByDoi(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public InformationResource findByDoi(String doi) {
        return getDao().findByDoi(doi);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#setAnalyzer(org.tdar.filestore.FileAnalyzer)
     */
    @Override
    @Autowired
    public void setAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceService#getAnalyzer()
     */
    @Override
    public FileAnalyzer getAnalyzer() {
        return analyzer;
    }

}
