package org.tdar.core.service.download;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.statistics.FileDownloadStatistic;

import com.opensymphony.xwork2.TextProvider;

public interface DownloadService {

    void registerDownload(List<FileDownloadStatistic> stats, TdarUser user);

    DownloadTransferObject constructDownloadTransferObject(DownloadTransferObject dto);

    void releaseDownloadLock(TdarUser authenticatedUser, List<InformationResourceFileVersion> versionsToDownload);

    void enforceDownloadLock(TdarUser authenticatedUser, List<InformationResourceFileVersion> versionsToDownload);

    DownloadTransferObject validateDownload(TdarUser authenticatedUser, InformationResourceFileVersion versionToDownload,
            InformationResource resourceToDownload_, boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization);

    DownloadTransferObject handleDownload(TdarUser authenticatedUser, InformationResourceFileVersion versionToDownload,
            InformationResource resourceToDownload_, boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization);

    /**
     * Validate, filter, and setup download for latest uploaded version of the specified InformationResourceFile.
     */
    DownloadTransferObject validateDownload(TdarUser authenticatedUser, InformationResourceFile fileToDownload,
            boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization);

    /**
     * Validate, filter, and setup download for latest uploaded version of the specified InformationResourceFile.
     */
    DownloadTransferObject handleDownload(TdarUser authenticatedUser, InformationResourceFile fileToDownload,
            boolean includeCoverPage, TextProvider textProvider, DownloadAuthorization authorization);

}