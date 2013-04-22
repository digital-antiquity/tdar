package org.tdar.core.service.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.dao.resource.InformationResourceFileVersionDao;
import org.tdar.core.service.ServiceInterface;

@Service
public class InformationResourceFileService extends ServiceInterface.TypedDaoBase<InformationResourceFile, InformationResourceFileDao> {

    @Autowired
    private InformationResourceFileVersionDao informationResourceFileVersionDao;

    /**
     * Deletes this information resource file from the filestore, database. Also
     * removes the translated file if it exists.
     * 
     * @param file
     */
    public void delete(InformationResourceFile file) {
        purgeFromFilestore(file);
        super.delete(file);
    }

    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file) {
        InformationResourceFile irFile = file.getInformationResourceFile();
        informationResourceFileVersionDao.delete(file);
        saveOrUpdate(irFile);
    }

    @Override
    public void delete(Collection<?> files) {
        for (Object object : files) {
            if (object instanceof InformationResourceFile) {
                delete((InformationResourceFile) object);
            } else {
                super.delete(object);
            }
        }
    }

    public void purgeFromFilestore(InformationResourceFile file) {
        List<InformationResourceFileVersion> versions = new ArrayList<InformationResourceFileVersion>(file.getInformationResourceFileVersions());
        for (InformationResourceFileVersion version : versions) {
            informationResourceFileVersionDao.delete(version);
        }
    }

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return getDao().findByFilestoreId(filestoreId);
    }

    /**
     * @param irVersion
     */
    @Transactional(readOnly = false)
    public void deleteTranslatedFiles(InformationResourceFile irFile) {
        getDao().deleteTranslatedFiles(irFile);
    }

    public Map<String, Float> getAdminFileExtensionStats() {
        return getDao().getAdminFileExtensionStats();
    }

    @Transactional(readOnly = true)
    public void updateTransientDownloadCount(InformationResourceFile irFile) {
        irFile.setTransientDownloadCount(getDao().getDownloadCount(irFile).longValue());
    }

}