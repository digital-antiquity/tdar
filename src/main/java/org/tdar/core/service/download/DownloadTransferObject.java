package org.tdar.core.service.download;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.struts.action.download.DownloadLockInputStream;

import com.opensymphony.xwork2.TextProvider;

/**
 * Represents a transfer object for a download. The DownloadService will build this and pass it back to the DownloadController, the controller will then
 * ask this for the inputStream and various info it needs. This means that the streaming of the result happens through the calls to this
 * 
 * @author abrin
 *
 */
public class DownloadTransferObject implements Serializable {

    private static final String ZIP_MIME_TYPE = "application/zip";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 7856219475924463528L;

    private List<FileDownloadStatistic> statistics = new ArrayList<>();
    private List<DownloadFile> downloads = new ArrayList<>();

    private String mimeType;
    private Long contentLength;
    private InformationResource informationResource;
    private List<InformationResourceFileVersion> versionsToDownload;
    private String fileName;
    private InputStream stream;
    private DownloadResult result;
    private TdarUser authenticatedUser;
    private boolean includeCoverPage;
    private String dispositionPrefix;
    private TextProvider textProvider;

    private DownloadService downloadService;

    public DownloadTransferObject(InformationResource resourceToDownload, List<InformationResourceFileVersion> versionsToDownload, TdarUser user,
            TextProvider textProvider, DownloadService downloadService) {
        this.informationResource = resourceToDownload;
        this.versionsToDownload = versionsToDownload;
        this.downloadService = downloadService;
        this.setTextProvider(textProvider);
        this.setAuthenticatedUser(user);
    }

    public DownloadTransferObject(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public List<FileDownloadStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<FileDownloadStatistic> statistics) {
        this.statistics = statistics;
    }

    public List<DownloadFile> getDownloads() {
        return downloads;
    }

    public void setDownloads(List<DownloadFile> downloads) {
        this.downloads = downloads;
    }

    public String getMimeType() {
        if (isZipDownload()) {
            mimeType = ZIP_MIME_TYPE;
        }
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        if (isZipDownload()) {
            return String.format("files-%s.zip", getInformationResource().getId());
        }
        return fileName;
    }

    public InputStream getInputStream() throws Exception {
        logger.debug("calling getInputStream");
        if (CollectionUtils.size(downloads) > 1) {
            return new DownloadLockInputStream(getZipInputStream(), this);
        }
        logger.debug("{}", downloads.get(0));
        return new DownloadLockInputStream(downloads.get(0).getInputStream(), this);
    }

    private InputStream getZipInputStream() throws Exception {
        PipedInputStream is = new PipedInputStream();
        final ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new PipedOutputStream(is)));
        final List<DownloadFile> downloadFiles = downloads;
        Thread thread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            for (DownloadFile df : downloadFiles) {
                                String filename = df.getFileName();

                                ZipEntry zentry = new ZipEntry(filename);
                                zout.putNextEntry(zentry);
                                InputStream fin = df.getInputStream();
                                logger.debug("adding to archive: {}", df.getFileName());
                                IOUtils.copy(fin, zout);
                                IOUtils.closeQuietly(fin);
                            }
                        } catch (Exception e) {
                            logger.error("exception when processing zip file: {}", e.getMessage(), e);
                        } finally {
                            IOUtils.closeQuietly(zout);
                        }
                    }
                }
                );
        thread.start();

        return is;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InformationResource getInformationResource() {
        return informationResource;
    }

    public void setInformationResource(InformationResource informationResource) {
        this.informationResource = informationResource;
    }

    public DownloadResult getResult() {
        return result;
    }

    public void setResult(DownloadResult result) {
        this.result = result;
    }

    public List<InformationResourceFileVersion> getVersionsToDownload() {
        return versionsToDownload;
    }

    public void setVersionsToDownload(List<InformationResourceFileVersion> versionsToDownload) {
        this.versionsToDownload = versionsToDownload;
    }

    public TdarUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(TdarUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public String getDispositionPrefix() {
        return dispositionPrefix;
    }

    public void setDispositionPrefix(String string) {
        this.dispositionPrefix = string;

    }

    public Long getContentLength() {
        if (isZipDownload()) {
            return contentLength;
        } else {
            return downloads.get(0).getFileLength();
        }
    }

    private boolean isZipDownload() {
        return CollectionUtils.size(downloads) > 1;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return StringUtils.join(downloads.toArray());
    }

    public boolean isIncludeCoverPage() {
        return includeCoverPage;
    }

    public void setIncludeCoverPage(boolean includeCoverPage) {
        this.includeCoverPage = includeCoverPage;
    }

    public TextProvider getTextProvider() {
        return textProvider;
    }

    public void setTextProvider(TextProvider textProvider) {
        this.textProvider = textProvider;
    }

    public void releaseLock() {
        downloadService.releaseDownloadLock(authenticatedUser, versionsToDownload);
    }

    public void registerDownloadLock() {
        downloadService.enforceDownloadLock(authenticatedUser, versionsToDownload);
    }

}
