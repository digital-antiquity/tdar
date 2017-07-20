package org.tdar.core.service.resource;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.resource.InformationResourceFileVersionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ServiceInterface;

@Service
public class InformationResourceFileVersionService extends ServiceInterface.TypedDaoBase<InformationResourceFileVersion, InformationResourceFileVersionDao> {

    /**
     * Deletes this information resource file from the filestore, database. Also removes the
     * translated file if it exists. (by default does not purge)
     * 
     * @param file
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file) {
        delete(file, false);
    }

    /**
     * Deletes this information resource file from the filestore, database. Also removes the
     * translated file if it exists.
     * 
     * @param file
     * @param purge
     *            Purge the File from the Filestore
     */
    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file, boolean purge) {
        if (file.isArchival() || file.isUploaded()) {
            throw new TdarRecoverableRuntimeException("informationResourceFileVersion.cannot_delete_original");
        }
        getDao().delete(file, purge);
    }

    /**
     * Purge a set of @link InformationResourceFileVersion fiels
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(Collection<InformationResourceFileVersion> files) {
        for (InformationResourceFileVersion object : files) {
            delete(object);
        }
    }

    /**
     * Delete only the derivatives related to the @link InformationResourceFile that's referenced by the @link InformationResourceFileVersion
     */
    @Transactional
    public int deleteDerivatives(InformationResourceFileVersion version) {
        return getDao().deleteDerivatives(version);
    }

}
