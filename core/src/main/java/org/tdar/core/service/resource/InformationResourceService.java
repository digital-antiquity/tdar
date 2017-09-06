package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.cache.BrowseDecadeCountCache;
import org.tdar.core.cache.BrowseYearCountCache;
import org.tdar.core.cache.Caches;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.filestore.FileAnalyzer;

public interface InformationResourceService {

    /*
     * Given a @link Resource and list of @link FileProxy objects, process the files and report any errors to the @link ActionMessageErrorSupport listener,
     * which is likely a controller.
     */
    <T extends InformationResource> ErrorTransferObject importFileProxiesAndProcessThroughWorkflow(T resource, TdarUser user, Long ticketId,
            List<FileProxy> fileProxiesToProcess) throws IOException;

    /*
     * Given an @link InformationResource, find all of the latest versions and reprocess them.
     */
    <T extends InformationResource> ErrorTransferObject reprocessInformationResourceFiles(T ir) throws Exception;

    /**
     * Find all Resources ... not suggested
     */
    List<InformationResource> findAllResources();

    /**
     * Generate the BrowseByDecatedCountCache for a set of @link Status (s).
     * 
     * @param statuses
     * @return
     */
    List<BrowseDecadeCountCache> findResourcesByDecade();

    /**
     * Find an @link InformationResourceFile by it's filename when specifying the @link InformationResourceFile
     * 
     * @param resource
     * @param filename
     * @return
     */
    InformationResourceFile findFileByFilename(InformationResource resource, String filename);

    /**
     * Find a random set of resources to be featured on the homepage ...
     * 
     * @param restrictToFiles
     *            show only resources with Files
     * @param maxResults
     *            how many to return
     * @return
     */
    <E extends Resource> List<E> findRandomFeaturedResource(boolean restrictToFiles, int maxResults);

    /**
     * Find a random set of resources, but limit them to be part of a project for the homepage
     * 
     * @param restrictToFiles
     * @param project
     * @param maxResults
     * @return
     */
    <E extends Resource> List<E> findRandomFeaturedResourceInProject(boolean restrictToFiles, Project project, int maxResults);

    /**
     * Find a random set of resources, but limit them to be part of a collection for the homepage
     * 
     * @param restrictToFiles
     * @param collectionId
     * @param maxResults
     * @return
     */
    <E extends Resource> List<E> findRandomFeaturedResourceInCollection(boolean restrictToFiles, Long collectionId, int maxResults);

    /**
     * Generate the BrowseByYearCountCache for a set of @link Status (s).
     * 
     * @param statuses
     * @return
     */
    List<BrowseYearCountCache> findResourceCountsByYear();

    List<Resource> getFeaturedItems();

    InformationResource findByDoi(String doi);

    /**
     * We autowire the setter to help with autowiring issues
     * 
     * @param analyzer
     *            the analyzer to set
     */
    void setAnalyzer(FileAnalyzer analyzer);

    FileAnalyzer getAnalyzer();

}