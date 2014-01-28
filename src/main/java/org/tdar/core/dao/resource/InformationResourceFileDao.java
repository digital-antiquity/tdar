package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.FileDownloadStatistic;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.Dao.HibernateBase;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;

@Component
public class InformationResourceFileDao extends HibernateBase<InformationResourceFile> {

    public InformationResourceFileDao() {
        super(InformationResourceFile.class);
    }

    @Autowired
    InformationResourceFileVersionDao informationResourceFileVersionDao;

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return findByProperty("filestoreId", filestoreId);
    }

    public Map<String, Float> getAdminFileExtensionStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_KEYWORD_COUNT_FILE_EXTENSION);
        query.setParameterList("internalTypes", Arrays.asList(VersionType.ARCHIVAL,
                VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL));
        Map<String, Float> toReturn = new HashMap<String, Float>();
        Long total = 0l;
        for (Object o : query.list()) {
            try {
                Object[] objs = (Object[]) o;
                if (objs == null || objs[0] == null)
                    continue;
                toReturn.put(String.format("%s (%s)", objs[0], objs[1]), ((Long) objs[1]).floatValue());
                total += (Long) objs[1];
            } catch (Exception e) {
                logger.debug("exception get admin file extension stats", e);
            }
        }

        for (String key : toReturn.keySet()) {
            toReturn.put(key, (toReturn.get(key) * 100) / total.floatValue());
        }

        return toReturn;
    }

    public Number getDownloadCount(InformationResourceFile irFile) {
        Criteria createCriteria = getCriteria(FileDownloadStatistic.class).setProjection(Projections.rowCount())
                .add(Restrictions.eq("reference", irFile));
        return (Number) createCriteria.list().get(0);
    }

    public void deleteTranslatedFiles(Dataset dataset) {
        for (InformationResourceFile irFile : dataset.getInformationResourceFiles()) {
            logger.debug("deleting {}", irFile);
            deleteTranslatedFiles(irFile);
        }
    }

    public void deleteTranslatedFiles(InformationResourceFile irFile) {
        for (InformationResourceFileVersion version : irFile.getLatestVersions()) {
            logger.debug("deleting version:{}  isTranslated:{}", version, version.isTranslated());
            if (version.isTranslated()) {
                //HQL here avoids issue where hibernate delays the delete
                deleteVersionImmediately(version);
                // we don't need safeguards on a translated file, so tell the dao to delete no matter what.
                // informationResourceFileVersionDao.forceDelete(version);
            }
        }
    }

    public void deleteVersionImmediately(InformationResourceFileVersion version) {
        if (version.isUploadedOrArchival()) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("error.cannot_delete_archival"));
        }
        Query query = getCurrentSession().createQuery(TdarNamedQueries.DELETE_INFORMATION_RESOURCE_FILE_VERSION_IMMEDIATELY);
        query.setParameter("id", version.getId()).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<InformationResourceFile> findFilesWithStatus(FileStatus[] statuses) {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_STATUS);
        query.setParameterList("statuses", Arrays.asList(statuses));
        return query.list();
    }
}
