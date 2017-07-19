package org.tdar.core.service.resource;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.ScrollableResults;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;

public interface InformationResourceFileService {

    /**
     * Deletes this information resource file from the filestore, database. Also
     * removes the translated file if it exists.
     * 
     * @throws NotImplementedException
     *             -- need to work through what should really happen here
     * @param file
     */
    void delete(InformationResourceFile file);

    /*
     * Find @link InformationResourceFile entries with the specified @link FileStatus
     */
    List<InformationResourceFile> findFilesWithStatus(FileStatus... statuses);

    /*
     * Remove InformationResourceFile
     * 
     * @throws NotImplementedException -- need to work through what should really happen here
     */
    void purgeFromFilestore(InformationResourceFile file);

    /**
     * Deletes the TranslatedFiles from the Filestore and database
     * 
     * @param irVersion
     */
    void deleteTranslatedFiles(InformationResourceFile irFile);

    /*
     * Returns a Map of Extensions and count() for Files in the filestore
     */
    Map<String, Long> getAdminFileExtensionStats();

    /*
     * Given a @link InformationResourceFile grab the download count from the database and set the transient value on the InformationResourceFile
     */
    void updateTransientDownloadCount(InformationResourceFile irFile);

    List<InformationResource> findInformationResourcesWithFileStatus(Person authenticatedUser, List<Status> resourceStatus, List<FileStatus> fileStatus);

    ScrollableResults findScrollableVersionsForVerification();

    List<InformationResourceFile> findAllExpiredEmbargoFiles();

    List<InformationResourceFile> findAllEmbargoFilesExpiringTomorrow();

    InformationResourceFile find(Long id);

}