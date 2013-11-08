package org.tdar.core.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.PdfCoverPageGenerationException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.utils.DeleteOnCloseFileInputStream;
import org.tdar.utils.MessageHelper;

/**
 * Helps handle all downloads of tDAR resources
 * 
 * 
 * @author Jim deVos
 * @version $Rev$
 */
@Service
public class DownloadService {

    private static final String ARCHIVE_DOWNLOAD = "archiveDownload";

    private static final String ZIP = ".zip";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    PdfService pdfService;

    @Autowired
    GenericService genericService;

    /**
     * Generate the Zip for for a download of multiple files, should include non-deleted files.
     * 
     * @param files
     * @param destinationFile
     * @throws IOException
     */
    public void generateZipArchive(Map<File, String> files, File destinationFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(destinationFile);
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout)); 
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


    /**
     * Treat the @link DownloadHandler as a proxy for the controller and generate PDFs or zip as necessary
     * @param authenticatedUser
     * @param dh
     * @param irFileVersions
     * @throws TdarActionException
     */
    @Transactional
    public void handleDownload(Person authenticatedUser, DownloadHandler dh, InformationResourceFileVersion... irFileVersions) throws TdarActionException {
        if (ArrayUtils.isEmpty((irFileVersions))) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("error.unsupported_action"));
        }

        File resourceFile = null;
        String mimeType = null;
        Map<File, String> files = new HashMap<>();
        for (InformationResourceFileVersion irFileVersion : irFileVersions) {
            resourceFile = addFileToDownload(files, authenticatedUser, dh, irFileVersion);
            mimeType = irFileVersion.getMimeType();

            if (!irFileVersion.isDerivative()) {
                InformationResourceFile irFile = irFileVersion.getInformationResourceFile();
                FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
                genericService.save(stat);
                if (irFileVersions.length > 1) {
                    initDispositionPrefix(irFile.getInformationResourceFileType(), dh);
                } else {
                    initDispositionPrefix(FileType.FILE_ARCHIVE, dh);
                }
            }
        }

        try {
            if (irFileVersions.length > 1) {
                resourceFile = File.createTempFile(ARCHIVE_DOWNLOAD, ZIP, TdarConfiguration.getInstance().getTempDirectory());
                generateZipArchive(files, resourceFile);
                mimeType = "application/zip";
                dh.setInputStream(new FileInputStream(resourceFile));
            }
        } catch (FileNotFoundException ex) {
            logger.error("Could not generate zip file to download: file not found", ex);
            throw new TdarActionException(StatusCode.UNKNOWN_ERROR, MessageHelper.getMessage("downloadService.could_not_generate_zip"));
        } catch (IOException ex) {
            logger.error("Could not generate zip file to download: IO exeption", ex);
            throw new TdarActionException(StatusCode.UNKNOWN_ERROR, MessageHelper.getMessage("downloadService.could_not_generate_zip"));
        }
        try {
            logger.debug("downloading file:" + resourceFile.getCanonicalPath());
        } catch (IOException e) {
            // Note: this was being "eaten" ... so not sure if we should throw exception here or not
            logger.error("{}", e);
        }
        dh.setContentLength(resourceFile.length());
        dh.setContentType(mimeType);
        if (dh.getInputStream() == null) {
            try {
                dh.setInputStream(new FileInputStream(resourceFile));
            } catch (FileNotFoundException e) {
                throw new TdarActionException(StatusCode.NOT_FOUND, MessageHelper.getMessage("error.file_not_found",""));
            }
        }
    }

    /**
     * Adds the specified file to the download. This method should encapsulate both handling of Zip File downloads and explicit file downloads.
     * 
     * @param downloadMap
     * @param authenticatedUser
     * @param dh
     * @param irFileVersion
     * @return
     * @throws TdarActionException
     */
    private File addFileToDownload(Map<File, String> downloadMap, Person authenticatedUser, DownloadHandler dh, InformationResourceFileVersion irFileVersion)
            throws TdarActionException {
        File resourceFile = null;
        try {
            resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(irFileVersion);
        } catch (FileNotFoundException e1) {
            throw new TdarActionException(StatusCode.NOT_FOUND,MessageHelper.getMessage("error.file_not_found",""));
        }
        dh.setFileName(irFileVersion.getFilename());
        if (resourceFile == null || !resourceFile.exists()) {
            throw new TdarActionException(StatusCode.NOT_FOUND, MessageHelper.getMessage("error.file_not_found",""));
        }

        String filename = null;
        // If it's a PDF, add the cover page if we can, if we fail, just send the original file
        if (irFileVersion.getExtension().equalsIgnoreCase("PDF")) {
            try {
                resourceFile = pdfService.mergeCoverPage(authenticatedUser, irFileVersion);
                filename = irFileVersion.getFilename();
                dh.setInputStream(new DeleteOnCloseFileInputStream(resourceFile));
            } catch (PdfCoverPageGenerationException cpge) {
                logger.trace("Error occured while merging cover page onto " + irFileVersion, cpge);
            } catch (Exception e) {
                logger.error("Error occured while merging cover page onto " + irFileVersion, e);
            }
        }
        downloadMap.put(resourceFile, filename);
        return resourceFile;
    }

    /**
     *  indicate in the header whether the file should be received as an attachment (e.g. give user download prompt)
     *  
     * @param fileType
     * @param dh
     */
    private void initDispositionPrefix(InformationResourceFile.FileType fileType, DownloadHandler dh) {
        if (InformationResourceFile.FileType.IMAGE != fileType) {
            dh.setDispositionPrefix("attachment;");
        }
    }

}