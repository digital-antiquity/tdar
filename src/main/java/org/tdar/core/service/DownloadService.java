package org.tdar.core.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
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
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.DownloadHandler;
import org.tdar.utils.DeleteOnCloseFileInputStream;

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

    @Autowired
    PdfService pdfService;

    @Autowired
    GenericService genericService;

    // TODO
    private String slugify(InformationResource resource) {
        return "ir-archive";
    }

    public void generateZipArchive(Collection<File> files, File destinationFile) throws IOException {
        FileOutputStream fout = new FileOutputStream(destinationFile);
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout)); // what is apache ZipOutputStream? It's probably better.
        for (File file : files) {
            ZipEntry entry = new ZipEntry(file.getName());
            zout.putNextEntry(entry);
            FileInputStream fin = new FileInputStream(file);
            logger.debug("adding to archive: {}", file);
            IOUtils.copy(fin, zout);
            IOUtils.closeQuietly(fin);
        }
        IOUtils.closeQuietly(zout);
    }

    public void generateZipArchive(InformationResource resource, File destinationFile) throws IOException {
        Collection<File> files = new LinkedList<File>();

        for (InformationResourceFileVersion version : resource.getLatestVersions()) {
            files.add(TdarConfiguration.getInstance().getFilestore().retrieveFile(version));
        }
    }

    public void generateZipArchive(InformationResource resource) throws IOException {
        generateZipArchive(resource, File.createTempFile(slugify(resource), ".zip"));
    }

    @Transactional
    public void handleDownload(Person authenticatedUser, DownloadHandler dh, InformationResourceFileVersion... irFileVersions) throws TdarActionException {
        if (ArrayUtils.isEmpty((irFileVersions)) || irFileVersions.length > 1) {
            throw new TdarRecoverableRuntimeException("unsupported action");
        }
        InformationResourceFileVersion irFileVersion = irFileVersions[0];
        File resourceFile = null;
        try {
            resourceFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(irFileVersion);
        } catch (FileNotFoundException e1) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }
        dh.setFileName(irFileVersion.getFilename());
        if (resourceFile == null || !resourceFile.exists()) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
        }

        // If it's a PDF, add the cover page if we can, if we fail, just send the original file
        if (irFileVersion.getExtension().equalsIgnoreCase("PDF")) {
            try {
                resourceFile = pdfService.mergeCoverPage(authenticatedUser, irFileVersion);
                dh.setInputStream(new DeleteOnCloseFileInputStream(resourceFile));
            } catch (Exception e) {
                logger.error("Error occured while merging cover page onto " + irFileVersion, e);
            }
        }
        try {
            logger.debug("downloading file:" + resourceFile.getCanonicalPath());
        } catch (IOException e) {
            // Note: this was being "eaten" ... so not sure if we should throw exception here or not
            logger.error("{}", e);
        }
        dh.setContentLength(resourceFile.length());
        dh.setContentType(irFileVersion.getMimeType());
        if (dh.getInputStream() == null) {
            try {
                dh.setInputStream(new FileInputStream(resourceFile));
            } catch (FileNotFoundException e) {
                throw new TdarActionException(StatusCode.NOT_FOUND, "File not found");
            }
        }
        if (!irFileVersion.isDerivative()) {
            InformationResourceFile irFile = irFileVersion.getInformationResourceFile();
            FileDownloadStatistic stat = new FileDownloadStatistic(new Date(), irFile);
            genericService.save(stat);
            initDispositionPrefix(irFile.getInformationResourceFileType(), dh);
        }
    }

    // indicate in the header whether the file should be received as an attachment (e.g. give user download prompt)
    private void initDispositionPrefix(InformationResourceFile.FileType fileType, DownloadHandler dh) {
        if (InformationResourceFile.FileType.IMAGE != fileType) {
            dh.setDispositionPrefix("attachment;");
        }
    }

}