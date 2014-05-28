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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.utils.DeleteOnCloseFileInputStream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
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
    
    @Autowired
    public DownloadService(PdfService pdfService, GenericService genericService) {
        this.pdfService = pdfService;
        this.genericService = genericService;
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

    public void handleDownload(Person authenticatedUser, DownloadHandler dh, Long informationResourceId, InformationResourceFileVersion... irFileVersions)
            throws TdarActionException {
        logger.debug("requested files:{}", irFileVersions);
        if (ArrayUtils.isEmpty((irFileVersions))) {
            throw new TdarRecoverableRuntimeException("error.unsupported_action");
        }
                
        List<FileDownloadStatistic> stats = handleActualDownload(authenticatedUser, dh, informationResourceId, irFileVersions);
        registerDownload(stats);
    }

    @Transactional(readOnly=false)
    public void registerDownload(List<FileDownloadStatistic> stats) {
        if (CollectionUtils.isNotEmpty(stats)) {
            genericService.saveOrUpdate(stats);
        }
    }

    @Transactional(readOnly=true)
    public List<FileDownloadStatistic> handleActualDownload(Person authenticatedUser, DownloadHandler dh, Long informationResourceId, InformationResourceFileVersion... irFileVersions) 
            throws TdarActionException {

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
                    fileName = String.format("files-%s.zip", informationResourceId);

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
                throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
            }
            dh.setFileName(fileName);
            dh.setContentLength(resourceFile.length());
            dh.setContentType(mimeType);
            logger.debug("downloading file:" + resourceFile.getCanonicalPath());
        } catch (FileNotFoundException ex) {
            logger.error("Could not generate zip file to download: file not found", ex);
            throw new TdarActionException(StatusCode.UNKNOWN_ERROR, "Could not generate zip file to download");
        } catch (IOException ex) {
            logger.error("Could not generate zip file to download: IO exeption", ex);
            throw new TdarActionException(StatusCode.UNKNOWN_ERROR, "Could not generate zip file to download");
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
        
        if (CollectionUtils.isEmpty(list));
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
            InformationResourceFileVersion irFileVersion, boolean includeCoverPage)
            throws TdarActionException {
        File resourceFile = null;
        try {
            resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(ObjectType.RESOURCE, irFileVersion);
        } catch (FileNotFoundException e1) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }
        if ((resourceFile == null) || !resourceFile.exists()) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }

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

}