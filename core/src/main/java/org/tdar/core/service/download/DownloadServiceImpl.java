package org.tdar.core.service.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.FileSystemResourceDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.PdfService;
import org.tdar.core.service.collection.WhiteLabelFiles;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.PersistableUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.opensymphony.xwork2.TextProvider;

/**
 * $Id$
 * 
 * 
 * @author Jim deVos
 * @version $Rev$
 */
@Service
public class DownloadServiceImpl implements DownloadService  {

    private static final int DOWNLOAD_LOCK_CACHE_AGE_MINUTES = 5;
    private static final int MAX_DOWNLOADS = 10;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final PdfService pdfService;
    private final GenericService genericService;

    private final Cache<Long, List<Integer>> downloadLock;

    private final AuthorizationService authorizationService;

    private final ResourceCollectionDao resourceCollectionDao;

    private final FileSystemResourceDao fileSystemResourceDao;

    @Autowired
    public DownloadServiceImpl(PdfService pdfService, GenericService genericService, AuthorizationService authorizationService,
            ResourceCollectionDao resourceCollectionDao, FileSystemResourceDao fileSystemResourceDao) {
        this.pdfService = pdfService;
        this.genericService = genericService;
        this.authorizationService = authorizationService;
        this.resourceCollectionDao = resourceCollectionDao;
        this.fileSystemResourceDao = fileSystemResourceDao;
        this.downloadLock = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .maximumSize(10000)
                .expireAfterWrite(DOWNLOAD_LOCK_CACHE_AGE_MINUTES, TimeUnit.MINUTES)
                .build();

    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#registerDownload(java.util.List, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void registerDownload(List<FileDownloadStatistic> stats, TdarUser user) {
        if (CollectionUtils.isNotEmpty(stats)) {
            genericService.saveOrUpdate(stats);
            if (PersistableUtils.isNotNullOrTransient(user)) {
                TdarUser writeableUser = genericService.markWritableOnExistingSession(user);
                writeableUser.setTotalDownloads(writeableUser.getTotalDownloads() + stats.size());
                logger.debug("totalDownloadForUser: {} {}",writeableUser.getId(),  writeableUser.getTotalDownloads());
                genericService.saveOrUpdate(writeableUser);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#constructDownloadTransferObject(org.tdar.core.service.download.DownloadTransferObject)
     */
    @Override
    @Transactional(readOnly = true)
    public DownloadTransferObject constructDownloadTransferObject(DownloadTransferObject dto) {
        for (InformationResourceFileVersion irFileVersion : dto.getVersionsToDownload()) {
            addFileToDownload(irFileVersion, dto);
            dto.setFileName(irFileVersion.getFilename());
            dto.setMimeType(irFileVersion.getMimeType());
            if (!irFileVersion.isDerivative()) {
                logger.debug("User {} is trying to DOWNLOAD: {} ({}: {})", dto.getAuthenticatedUser(), irFileVersion, TdarConfiguration.getInstance()
                        .getSiteAcronym(),
                        irFileVersion.getInformationResourceFile().getInformationResource().getId());
                InformationResourceFile irFile = irFileVersion.getInformationResourceFile();
                addStatistics(dto, irFile);
            }
        }
        return dto;
    }

    private void addStatistics(DownloadTransferObject dto, InformationResourceFile irFile) {
        // don't count download stats if you're downloading your own stuff
        TdarUser user = dto.getAuthenticatedUser();
        if (!PersistableUtils.isEqual(irFile.getInformationResource().getSubmitter(), user) && !authorizationService.isEditor(user)) {
            FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
            dto.getStatistics().add(stat);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#releaseDownloadLock(org.tdar.core.bean.entity.TdarUser, java.util.List)
     */
    @Override
    public void releaseDownloadLock(TdarUser authenticatedUser, List<InformationResourceFileVersion> versionsToDownload) {
        InformationResourceFileVersion[] array = new InformationResourceFileVersion[0];

        if (CollectionUtils.isEmpty(versionsToDownload)) {
            return;
        }

        if (shouldNotCountInDownloadLock(authenticatedUser, versionsToDownload)) {
            return;
        }
        Long key = authenticatedUser.getId();
        List<Integer> list = downloadLock.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(list)) {
            list.remove(new Integer(array.hashCode()));
        }

        if (CollectionUtils.isEmpty(list)) {
            downloadLock.invalidate(key);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#enforceDownloadLock(org.tdar.core.bean.entity.TdarUser, java.util.List)
     */
    @Override
    public void enforceDownloadLock(TdarUser authenticatedUser, List<InformationResourceFileVersion> versionsToDownload) {
        if (CollectionUtils.isEmpty(versionsToDownload)) {
            return;
        }

        if (shouldNotCountInDownloadLock(authenticatedUser, versionsToDownload)) {
            return;
        }

        Long key = authenticatedUser.getId();
        List<Integer> list = downloadLock.getIfPresent(key);
        if (list == null) {
            list = new ArrayList<>();
        }

        if (list.contains(versionsToDownload.hashCode())) {
            if (TdarConfiguration.getInstance().shouldThrowExceptionOnConcurrentUserDownload()) {
                throw new TdarRecoverableRuntimeException("downloadService.duplicate_download");
            } else {
                logger.warn("concurrent downloads of the same file {} by one user: {}", versionsToDownload, authenticatedUser);
            }
        }

        if (list.size() > MAX_DOWNLOADS) {
            if (TdarConfiguration.getInstance().shouldThrowExceptionOnConcurrentUserDownload()) {
                throw new TdarRecoverableRuntimeException("downloadService.too_many_concurrent_download");
            } else {
                logger.warn("too many concurrent downloads ({}) by one user: {}", list.size(), authenticatedUser);
            }
        }

        list.add(new Integer(versionsToDownload.hashCode()));
        downloadLock.put(key, list);
    }

    private boolean shouldNotCountInDownloadLock(TdarUser authenticatedUser, List<InformationResourceFileVersion> irFileVersions) {
        if (CollectionUtils.isEmpty(irFileVersions)) {
            return true;
        }
        boolean ret = false;
        for (InformationResourceFileVersion version : irFileVersions) {
            if (PersistableUtils.isNullOrTransient(authenticatedUser) || version.isDerivative()) {
                ret = true;
            }
        }
        return ret;
    }

    private String addFileToDownload(InformationResourceFileVersion irFileVersion, DownloadTransferObject dto) {
        File transientFile = irFileVersion.getTransientFile();
        // setting original filename on file
        String actualFilename = irFileVersion.getInformationResourceFile().getFilename();
        DownloadFile resourceFile = new DownloadFile(transientFile, actualFilename, irFileVersion);

        // If it's a PDF, add the cover page if we can, if we fail, just send the original file
        if (dto.isIncludeCoverPage() && pdfService.coverPageSupported(irFileVersion)) {
            resourceFile = new DownloadPdfFile((Document) dto.getInformationResource(), irFileVersion, pdfService, dto.getAuthenticatedUser(),
                    dto.getTextProvider(), dto.getCoverPageLogo());
        }

        if (FileType.IMAGE != irFileVersion.getInformationResourceFile().getInformationResourceFileType()) {
            dto.setAttachment(true);
        }

        dto.getDownloads().add(resourceFile);
        return actualFilename;
        // downloadMap.put(resourceFile, irFileVersion.getFilename());
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#validateDownload(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.file.InformationResourceFileVersion, org.tdar.core.bean.resource.InformationResource, boolean, com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.collection.DownloadAuthorization)
     */
    @Override
    @Transactional(readOnly = true)
    public DownloadTransferObject validateDownload(TdarUser authenticatedUser, InformationResourceFileVersion versionToDownload,
            InformationResource resourceToDownload_, boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization) {
        DownloadTransferObject dto = setupDownload(authenticatedUser, versionToDownload, resourceToDownload_, includeCoverPage, textProvider, authorization);
        if (dto.getResult() != DownloadResult.SUCCESS && dto.getResult() != null) {
            return dto;
        }
        try {
            constructDownloadTransferObject(dto);
        } catch (TdarRecoverableRuntimeException tre) {
            logger.error("ERROR IN Download: {}", tre);
            dto.setResult(DownloadResult.ERROR);
            return dto;
        }
        dto.setResult(DownloadResult.SUCCESS);
        return dto;
    }
    

    
    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#handleDownload(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.file.InformationResourceFileVersion, org.tdar.core.bean.resource.InformationResource, boolean, com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.collection.DownloadAuthorization)
     */
    @Override
    @Transactional(readOnly = false)
    public DownloadTransferObject handleDownload(TdarUser authenticatedUser, InformationResourceFileVersion versionToDownload,
            InformationResource resourceToDownload_, boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization) {
        DownloadTransferObject dto = setupDownload(authenticatedUser, versionToDownload, resourceToDownload_, includeCoverPage, textProvider, authorization);
        if (dto.getResult() != DownloadResult.SUCCESS && dto.getResult() != null) {
            return dto;
        }
        try {
            constructDownloadTransferObject(dto);
            logger.info("user {} downloaded {} ({})", authenticatedUser, versionToDownload, resourceToDownload_);
            registerDownload(dto.getStatistics(), authenticatedUser);
        } catch (TdarRecoverableRuntimeException tre) {
            logger.error("ERROR IN Download: {}", tre);
            dto.setResult(DownloadResult.ERROR);
            return dto;
        }
        dto.setResult(DownloadResult.SUCCESS);
        return dto;
    }
    

    


    private DownloadTransferObject setupDownload(TdarUser authenticatedUser, InformationResourceFileVersion versionToDownload, InformationResource resourceToDownload_,
            boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization) {
        InformationResource resourceToDownload = resourceToDownload_;
        List<InformationResourceFileVersion> versionsToDownload = new ArrayList<>();
        if (PersistableUtils.isNotNullOrTransient(versionToDownload)) {
            versionsToDownload.add(versionToDownload);
        }

        DownloadResult issue = DownloadResult.SUCCESS;
        if (PersistableUtils.isNotNullOrTransient(resourceToDownload)) {
            for (InformationResourceFile irf : resourceToDownload.getInformationResourceFiles()) {
                if (irf.isDeleted()) {
                    continue;
                }
                versionsToDownload.add(irf.getLatestUploadedOrArchivalVersion());
                logger.trace("adding: {}", irf.getLatestUploadedVersion());
            }
        } else if (CollectionUtils.isNotEmpty(versionsToDownload)) {
            // trying to address a casting issue where we're getting a javassist version and not a tdar information resource
            resourceToDownload = versionsToDownload.get(0).getInformationResourceFile().getInformationResource();
            if (resourceToDownload.getResourceType().isDocument()) {
                resourceToDownload = genericService.find(Document.class, resourceToDownload.getId());
            }
        } else {
            issue = DownloadResult.ERROR;
        }

        File coverLogo = getCoverLogo(resourceToDownload);

        DownloadTransferObject dto = new DownloadTransferObject(resourceToDownload, authenticatedUser, textProvider, this, authorization);
        dto.setCoverPageLogo(coverLogo);
        dto.setIncludeCoverPage(includeCoverPage);
        if (issue != DownloadResult.SUCCESS) {
            dto.setResult(issue);
            return dto;
        }
        Iterator<InformationResourceFileVersion> iter = versionsToDownload.iterator();
        while (iter.hasNext()) {
            InformationResourceFileVersion version = iter.next();
            if (!authorizationService.canDownload(authenticatedUser, version) && authorization == null) {
                logger.warn("thumbail request: resource is confidential/embargoed: {}", version);
                dto.setResult(DownloadResult.FORBIDDEN);
                return dto;
            }

            if (version.getInformationResourceFile().isDeleted() && !authorizationService.isEditor(authenticatedUser)) {
                logger.debug("requesting deleted file");
                iter.remove();
                continue;
            }

            File resourceFile = null;
            try {
                resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version);
                version.setTransientFile(resourceFile);
            } catch (FileNotFoundException e1) {
                logNotFound(version, e1, null);
                dto.setResult(DownloadResult.NOT_FOUND);
                return dto;
            }

            if ((resourceFile == null)) {
                logNotFound(version, null, null);
                dto.setResult(DownloadResult.NOT_FOUND);
                return dto;
            }

            if (!resourceFile.exists()) {
                logNotFound(version, null, resourceFile.getAbsolutePath());
                dto.setResult(DownloadResult.NOT_FOUND);
                return dto;
            }

        }

        if (CollectionUtils.isEmpty(versionsToDownload)) {
            dto.setResult(DownloadResult.ERROR);
            return dto;
        }

        dto.setVersionsToDownload(versionsToDownload);  
        return dto;
    }

    private File getCoverLogo(InformationResource resourceToDownload) {
        ResourceCollection whiteLabelCollection = resourceCollectionDao.getWhiteLabelCollectionForResource(resourceToDownload);
        if (whiteLabelCollection == null || whiteLabelCollection.getProperties() == null || !whiteLabelCollection.getProperties().getCustomDocumentLogoEnabled()) {
            return null;
        }
        return fileSystemResourceDao.getHostedFile(WhiteLabelFiles.PDF_COVERPAGE_FILENAME, FilestoreObjectType.COLLECTION, whiteLabelCollection.getId());
    }

    private void logNotFound(InformationResourceFileVersion version, FileNotFoundException e1, String path) {
        if (TdarConfiguration.getInstance().isProductionEnvironment()) {
            if (e1 == null) {
                logger.error("FILE NOT FOUND: {} [{}]", version, path);
            } else {
                logger.error("FILE NOT FOUND: {} [{}]", version, path, e1);
            }
        } else {
            logger.warn("FileNotFound (FilestoreConfigured?):: {} ",path);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#validateDownload(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.file.InformationResourceFile, boolean, com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.collection.DownloadAuthorization)
     */
    @Override
    @Transactional(readOnly = false)
    public DownloadTransferObject validateDownload(TdarUser authenticatedUser, InformationResourceFile fileToDownload,
            boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization) {

        InformationResourceFileVersion fileVersion = fileToDownload.getLatestUploadedVersion();

        return validateDownload(authenticatedUser, fileVersion, null, includeCoverPage, textProvider, authorization);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.download.DownloadService#handleDownload(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.file.InformationResourceFile, boolean, com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.collection.DownloadAuthorization)
     */
    @Override
    @Transactional(readOnly = false)
    public DownloadTransferObject handleDownload(TdarUser authenticatedUser, InformationResourceFile fileToDownload,
            boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization) {

        InformationResourceFileVersion fileVersion = fileToDownload.getLatestUploadedVersion();

        return handleDownload(authenticatedUser, fileVersion, null, includeCoverPage, textProvider, authorization);
    }

}
