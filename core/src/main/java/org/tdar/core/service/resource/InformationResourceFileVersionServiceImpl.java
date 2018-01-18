package org.tdar.core.service.resource;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.resource.InformationResourceFileVersionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ServiceInterface;

@Service
public class InformationResourceFileVersionServiceImpl extends ServiceInterface.TypedDaoBase<InformationResourceFileVersion, InformationResourceFileVersionDao>
        implements InformationResourceFileVersionService {

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceFileVersionService#delete(org.tdar.core.bean.resource.file.InformationResourceFileVersion)
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file) {
        delete(file, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceFileVersionService#delete(org.tdar.core.bean.resource.file.InformationResourceFileVersion,
     * boolean)
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(InformationResourceFileVersion file, boolean purge) {
        if (file.isArchival() || file.isUploaded()) {
            throw new TdarRecoverableRuntimeException("informationResourceFileVersion.cannot_delete_original");
        }
        getDao().delete(file, purge);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.InformationResourceFileVersionService#delete(java.util.Collection)
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(Collection<InformationResourceFileVersion> files) {
        for (InformationResourceFileVersion object : files) {
            delete(object);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.resource.InformationResourceFileVersionService#deleteDerivatives(org.tdar.core.bean.resource.file.InformationResourceFileVersion)
     */
    @Override
    @Transactional
    public int deleteDerivatives(InformationResourceFileVersion version) {
        return getDao().deleteDerivatives(version);
    }

}
