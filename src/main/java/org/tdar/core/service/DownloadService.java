package org.tdar.core.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.utils.DeleteOnCloseFileInputStream;

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
public class DownloadService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final PdfService pdfService;
    private final GenericService genericService;

    private final Cache<Long, List<Integer>> downloadLock;

    private final AuthorizationService authorizationService;

    @Autowired
    public DownloadService(PdfService pdfService, GenericService genericService, AuthorizationService authorizationService) {
        this.pdfService = pdfService;
        this.genericService = genericService;
        this.authorizationService = authorizationService;
        this.downloadLock = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .maximumSize(10000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

    }

    public void generateZipArchive(Map<File, String> files, File destinationFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(destinationFile);
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout)); // what is apache ZipOutputStream? It's probably better.
        for (Entry<File, String> entry : files.entrySet()) {
            String filename = entry.getValue();
            if (filename == null) {
                filename = entry.getKey().getName();
            }
            ZipEntry zentry = new ZipEntry(filename);
            zout.putNextEntry(zentry);
            FileInputStream fin = new FileInputStream(entry.getKey());
            logger.debug("adding to archive: {}", entry.getKey());
            IOUtils.copy(fin, zout);
            IOUtils.closeQuietly(fin);
        }
        IOUtils.closeQuietly(zout);
    }

    @Transactional(readOnly = false)
    public void registerDownload(List<FileDownloadStatistic> stats) {
        if (CollectionUtils.isNotEmpty(stats)) {
            genericService.saveOrUpdate(stats);
        }
    }

    @Transactional(readOnly = true)
    public List<FileDownloadStatistic> handleActualDownload(TdarUser authenticatedUser, DownloadHandler dh, InformationResource informationResource,
            InformationResourceFileVersion... irFileVersions) 
    {

        Map<File, String> files = new HashMap<>();
        String mimeType = null;
        String fileName = null;
        List<FileDownloadStatistic> stats = new ArrayList<>();
        enforceDownloadLock(authenticatedUser, irFileVersions);

        for (InformationResourceFileVersion irFileVersion : irFileVersions) {

            if (irFileVersion.getInformationResourceFile().isDeleted()) {
                continue;
            }
            addFileToDownload(dh, files, authenticatedUser, irFileVersion, dh.isCoverPageIncluded());
            fileName = irFileVersion.getFilename();
            if (!irFileVersion.isDerivative()) {
                logger.debug("User {} is trying to DOWNLOAD: {} ({}: {})", authenticatedUser, irFileVersion, TdarConfiguration.getInstance().getSiteAcronym(),
                        irFileVersion.getInformationResourceFile().getInformationResource().getId());
                InformationResourceFile irFile = irFileVersion.getInformationResourceFile();

                // don't count download stats if you're downloading your own stuff
                if (!Persistable.Base.isEqual(irFile.getInformationResource().getSubmitter(), authenticatedUser) && !dh.isEditor()) {
                    FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
                    stats.add(stat);
                }

                if (irFileVersions.length > 1) {
                    initDispositionPrefix(FileType.FILE_ARCHIVE, dh);
                    fileName = String.format("files-%s.zip", informationResource.getId());

                    mimeType = "application/zip";
                } else {
                    initDispositionPrefix(irFile.getInformationResourceFileType(), dh);
                    mimeType = irFileVersions[0].getMimeType();
                    fileName = irFileVersions[0].getFilename();
                }
            }
        }

        try {
            File resourceFile = null;
            if (irFileVersions.length > 1) {
                resourceFile = File.createTempFile("archiveDownload", ".zip", TdarConfiguration.getInstance().getTempDirectory());
                generateZipArchive(files, resourceFile);
                // although in temp, it might be quite large, so let's not leave it lying around
                dh.setInputStream(new DeleteOnCloseFileInputStream(resourceFile));
            } else if (files.keySet().size() == 1) {
                resourceFile = (File) files.keySet().toArray()[0];
                dh.setInputStream(new FileInputStream(resourceFile));
            } else {
                logger.info("No files present in files.keySet() - could be thumbnail request for file w/ deleted status");
                throw new FileNotFoundException("File not found:" + resourceFile);
            }
            dh.setFileName(fileName);
            dh.setContentLength(resourceFile.length());
            dh.setContentType(mimeType);
            logger.debug("downloading file: {} [{} {}]" ,  resourceFile.getCanonicalPath(), mimeType, resourceFile.length());
        } catch (NullPointerException | IOException ex) {
            logger.error("Could not generate zip file to download: IO exeption", ex);
            throw new TdarRecoverableRuntimeException("downloadService.error_zip_generation");
        } finally {
            releaseDownloadLock(authenticatedUser, irFileVersions);
        }
        return stats;
    }

    private void releaseDownloadLock(Person authenticatedUser, InformationResourceFileVersion[] irFileVersions) {
        if (isUnauthenticatedOrThumbnail(authenticatedUser, irFileVersions)) {
            return;
        }
        Long key = authenticatedUser.getId();
        List<Integer> list = downloadLock.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(list)) {
            list.remove(new Integer(irFileVersions.hashCode()));
        }

        if (CollectionUtils.isEmpty(list))
            ;
        downloadLock.invalidate(key);
    }

    private void enforceDownloadLock(Person authenticatedUser, InformationResourceFileVersion[] irFileVersions) {
        if (isUnauthenticatedOrThumbnail(authenticatedUser, irFileVersions)) {
            return;
        }
        Long key = authenticatedUser.getId();
        List<Integer> list = downloadLock.getIfPresent(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        if (list.contains(irFileVersions.hashCode())) {
            if (TdarConfiguration.getInstance().shouldThrowExceptionOnConcurrentUserDownload()) {
                throw new TdarRecoverableRuntimeException("downloadService.duplicate_download");
            } else {
                logger.error("too many concurrent downloads of the same file by one user");
            }
        }

        if (list.size() > 4) {
            if (TdarConfiguration.getInstance().shouldThrowExceptionOnConcurrentUserDownload()) {
                throw new TdarRecoverableRuntimeException("downloadService.too_many_concurrent_download");
            } else {
                logger.error("too many concurrent downloads by one user");
            }
        }

        list.add(new Integer(irFileVersions.hashCode()));
        downloadLock.put(key, list);
    }

    private boolean isUnauthenticatedOrThumbnail(Person authenticatedUser, InformationResourceFileVersion[] irFileVersions) {
        return Persistable.Base.isNullOrTransient(authenticatedUser) || CollectionUtils.size(irFileVersions) == 1 && irFileVersions[0].isThumbnail();
    }

    private void addFileToDownload(TextProvider provider, Map<File, String> downloadMap, Person authenticatedUser,
            InformationResourceFileVersion irFileVersion, boolean includeCoverPage) {
        File resourceFile = irFileVersion.getTransientFile();

        // If it's a PDF, add the cover page if we can, if we fail, just send the original file
        if (irFileVersion.getExtension().equalsIgnoreCase("PDF") && includeCoverPage) {
            try {
                // this will be in the temp directory, so it will be scavenged at some stage.
                resourceFile = pdfService.mergeCoverPage(provider, authenticatedUser, irFileVersion);
            } catch (PdfCoverPageGenerationException cpge) {
                logger.trace("Error occurred while merging cover page onto " + irFileVersion, cpge);
            } catch (Exception e) {
                logger.error("Error occurred while merging cover page onto " + irFileVersion, e);
            }
        }
        downloadMap.put(resourceFile, irFileVersion.getFilename());
    }

    // indicate in the header whether the file should be received as an attachment (e.g. give user download prompt)
    private void initDispositionPrefix(InformationResourceFile.FileType fileType, DownloadHandler dh) {
        if (InformationResourceFile.FileType.IMAGE != fileType) {
            dh.setDispositionPrefix("attachment;");
        }
    }

    public DownloadResult validateFilterAndSetupDownload(TdarUser authenticatedUser, InformationResourceFileVersion versionToDownload,
            InformationResource resourceToDownload, DownloadHandler dh) {
        List<InformationResourceFileVersion> versionsToDownload = new ArrayList<>();
        if (Persistable.Base.isNotNullOrTransient(versionToDownload)) {
            versionsToDownload.add(versionToDownload);
        }

        if (Persistable.Base.isNotNullOrTransient(resourceToDownload)) {
            for (InformationResourceFile irf : resourceToDownload.getInformationResourceFiles()) {
                if (irf.isDeleted()) {
                    continue;
                }
                versionsToDownload.add(irf.getLatestUploadedOrArchivalVersion());
                logger.trace("adding: {}", irf.getLatestUploadedVersion());
            }
        }
        if (CollectionUtils.isEmpty(versionsToDownload)) {
            return DownloadResult.ERROR;
        }

        for (InformationResourceFileVersion version : versionsToDownload) {
            if (!authorizationService.canDownload(version, authenticatedUser)) {
                logger.warn("thumbail request: resource is confidential/embargoed: {}", versionToDownload.getId());
                return DownloadResult.FORBIDDEN;
            }
            File resourceFile = null;
            try {
                resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(ObjectType.RESOURCE, version);
                version.setTransientFile(resourceFile);
            } catch (FileNotFoundException e1) {
                logger.error("FILE NOT FOUND: {}", version);
                return DownloadResult.NOT_FOUND;
            }
            if ((resourceFile == null) || !resourceFile.exists()) {
                logger.warn("FILE NOT FOUND: {}", resourceFile);
                return DownloadResult.NOT_FOUND;
            }
        }
        logger.info("user {} downloaded {} ({})", authenticatedUser, versionToDownload, resourceToDownload);
        try {
            List<FileDownloadStatistic> stats = handleActualDownload(authenticatedUser, dh, resourceToDownload, versionsToDownload.toArray(new InformationResourceFileVersion[0]));
            registerDownload(stats);
//       } catch (FileNotFoundException fnf) {
//            logger.error("FILE NOT FOUND: {}", fnf.getMessage());
//            return DownloadResult.NOT_FOUND;
        } catch (TdarRecoverableRuntimeException tre) {
            logger.error("ERROR IN Download: {}", tre);
            return DownloadResult.ERROR;
        }
        return DownloadResult.SUCCESS;
    }
}