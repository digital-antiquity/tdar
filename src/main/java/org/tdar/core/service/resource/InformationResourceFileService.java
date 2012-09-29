package org.tdar.core.service.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.service.ServiceInterface;

@Service
public class InformationResourceFileService extends ServiceInterface.TypedDaoBase<InformationResourceFile, InformationResourceFileDao> {

    @Autowired
    public void setDao(InformationResourceFileDao dao) {
        super.setDao(dao);
    }

    @Autowired
    public InformationResourceFileVersionService informationResourceFileVersionService;

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
        getInformationResourceFileVersionService().delete(file);
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
            informationResourceFileVersionService.delete(version);
        }
    }

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return getDao().findByFilestoreId(filestoreId);
    }

    public InformationResourceFileVersionService getInformationResourceFileVersionService() {
        return informationResourceFileVersionService;
    }

    public void setInformationResourceFileVersionService(InformationResourceFileVersionService informationResourceFileVersionService) {
        this.informationResourceFileVersionService = informationResourceFileVersionService;
    }

    /**
     * @param irVersion
     */
    @Transactional(readOnly = false)
    public void deleteTranslatedFiles(InformationResourceFile irFile) {
        // FIXME: CALLING THIS REPEATEDLY WILL CAUSE SQL ERRORS DUE TO KEY
        // ISSUES (DELETE NOT
        // HAPPENING BEFORE INSERT)
        for (InformationResourceFileVersion version : irFile.getLatestVersions()) {
            if (version.isTranslated()) {
                delete(version);
            }
        }
        // saveOrUpdate(irFile);
    }
}