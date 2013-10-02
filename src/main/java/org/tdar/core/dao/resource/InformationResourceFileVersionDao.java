package org.tdar.core.dao.resource;

import java.io.IOException;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.Dao.HibernateBase;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.filestore.Filestore;

@Component
public class InformationResourceFileVersionDao extends HibernateBase<InformationResourceFileVersion> {
    private static final Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    public InformationResourceFileVersionDao() {
        super(InformationResourceFileVersion.class);
    }

    public int deleteDerivatives(InformationResourceFileVersion version) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES);
        query.setParameter("informationResourceFileId", version.getInformationResourceFileId());
        query.setParameterList("derivativeFileVersionTypes", VersionType.getDerivativeVersionTypes());
        return query.executeUpdate();
    }

    public void delete(InformationResourceFileVersion file, boolean purge) {
        if (purge) {
            purgeFromFilestore(file);
        }
        if (file.getInformationResourceFile() != null) {
            file.getInformationResourceFile().getInformationResourceFileVersions().remove(file);
        }
        super.delete(file);

    }

    public void purgeFromFilestore(InformationResourceFileVersion file) {
        try {
            filestore.purge(file);
        } catch (IOException e) {
            getLogger().warn("Problems purging file with filestoreID of" +
                    file.getFilename() + " from the filestore.", e);
        }

    }

}
