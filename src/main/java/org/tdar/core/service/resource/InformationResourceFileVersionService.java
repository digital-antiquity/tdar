package org.tdar.core.service.resource;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.dao.resource.InformationResourceFileVersionDao;
import org.tdar.core.service.ServiceInterface;

@Service
public class InformationResourceFileVersionService extends ServiceInterface.TypedDaoBase<InformationResourceFileVersion, InformationResourceFileVersionDao> {

    /**
     * Deletes this information resource file from the filestore, database. Also removes the
     * translated file if it exists.
     * 
     * @param file
     */
    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file) {
        delete(file, true);
    }

    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file, boolean purge) {
        getDao().delete(file,purge);
    }

    @Override
    public void delete(Collection<?> files) {
        for (Object object : files) {
            if (object instanceof InformationResourceFileVersion) {
                delete((InformationResourceFileVersion) object);
            } else {
                super.delete(object);
            }
        }
    }

    public void purgeFromFilestore(InformationResourceFileVersion file) {
        getDao().purgeFromFilestore(file);
    }
    
    @Transactional
    public int deleteDerivatives(InformationResourceFileVersion version) {
        return getDao().deleteDerivatives(version);
    }

}
