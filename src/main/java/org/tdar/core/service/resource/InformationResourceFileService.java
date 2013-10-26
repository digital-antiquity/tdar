package org.tdar.core.service.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.dao.resource.InformationResourceFileVersionDao;
import org.tdar.core.service.ServiceInterface;
import org.tdar.utils.MessageHelper;

@Service
public class InformationResourceFileService extends ServiceInterface.TypedDaoBase<InformationResourceFile, InformationResourceFileDao> {

    @Autowired
    private InformationResourceFileVersionDao informationResourceFileVersionDao;

    /**
     * Deletes this information resource file from the filestore, database. Also
     * removes the translated file if it exists.
     * 
     * @throws NotImplementedException -- need to work through what should really happen here
     * @param file
     */
    public void delete(InformationResourceFile file) {
        throw new NotImplementedException(MessageHelper.getMessage("error.not_implemented"));
        
        //        purgeFromFilestore(file);
        //        if (file.getInformationResource() != null) {
        //            file.getInformationResource().getInformationResourceFiles().remove(file);
        //        }
        //        super.delete(file);
    }
    
    /*
     * Find @link InformationResourceFile entries with the specified @link FileStatus
     */
    @Transactional
    public List<InformationResourceFile> findFilesWithStatus(FileStatus ... statuses) {
        return getDao().findFilesWithStatus(statuses);
    }

    /*
     * Remove InformationResourceFile
     * @throws NotImplementedException -- need to work through what should really happen here
     */
    public void purgeFromFilestore(InformationResourceFile file) {
        List<InformationResourceFileVersion> versions = new ArrayList<>(file.getInformationResourceFileVersions());
        for (InformationResourceFileVersion version : versions) {
            informationResourceFileVersionDao.delete(version, true);
        }
    }

    
    /**
     * Deletes the TranslatedFiles from the Filestore and database
     * @param irVersion
     */
    @Transactional(readOnly = false)
    public void deleteTranslatedFiles(InformationResourceFile irFile) {
        getDao().deleteTranslatedFiles(irFile);
    }

    /*
     * Returns a Map of Extensions and count() for Files in the filestore
     */
    @Transactional
    public Map<String, Float> getAdminFileExtensionStats() {
        return getDao().getAdminFileExtensionStats();
    }

    /*
     * Given a @link InformationResourceFile grab the download count from the database and set the transient value on the InformationResourceFile
     */
    @Transactional(readOnly = true)
    public void updateTransientDownloadCount(InformationResourceFile irFile) {
        irFile.setTransientDownloadCount(getDao().getDownloadCount(irFile).longValue());
    }

}