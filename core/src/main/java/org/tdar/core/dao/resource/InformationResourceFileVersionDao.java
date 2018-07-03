package org.tdar.core.dao.resource;

import java.io.IOException;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.HibernateBase;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.filestore.VersionType;

@Component
public class InformationResourceFileVersionDao extends HibernateBase<InformationResourceFileVersion> {

    private static final Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public InformationResourceFileVersionDao() {
        super(InformationResourceFileVersion.class);
    }

    public int deleteDerivatives(InformationResourceFileVersion version) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES);
        query.setParameter("informationResourceFileId", version.getInformationResourceFileId());
        query.setParameter("derivativeFileVersionTypes", VersionType.getDerivativeVersionTypes());
        return query.executeUpdate();
    }

    public void delete(InformationResourceFileVersion file) {
        delete(file, false);
    }

    public void delete(InformationResourceFileVersion file, boolean purge) {
        if (file.isUploadedOrArchival()) {
            throw new TdarRecoverableRuntimeException("error.cannot_delete_archival");
        }
        if (purge) {
            purgeFromFilestore(file);
        }
        if (file.getInformationResourceFile() != null) {
            file.getInformationResourceFile().getInformationResourceFileVersions().remove(file);
        }
        logger.debug("I'm about to delete file:{}", file);
        super.delete(file);

    }

    public void purgeFromFilestore(InformationResourceFileVersion file) {
        try {
            filestore.purge(FilestoreObjectType.RESOURCE, file);
        } catch (IOException e) {
            getLogger().warn("Problems purging file with filestoreID of {} from the filestore.", file.getFilename(), e);
        }

    }

}
