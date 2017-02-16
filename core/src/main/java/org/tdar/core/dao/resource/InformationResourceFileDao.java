package org.tdar.core.dao.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceProxy;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.dao.Dao.HibernateBase;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.PersistableUtils;

@Component
public class InformationResourceFileDao extends HibernateBase<InformationResourceFile> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public InformationResourceFileDao() {
        super(InformationResourceFile.class);
    }

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return findByProperty("filestoreId", filestoreId);
    }

    public Map<String, Long> getAdminFileExtensionStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_KEYWORD_COUNT_FILE_EXTENSION);
        query.setParameter("internalTypes", Arrays.asList(VersionType.ARCHIVAL,
                VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL));
        Map<String, Long> toReturn = new HashMap<>();
        for (Object o : query.getResultList()) {
            try {
                Object[] objs = (Object[]) o;
                if (ArrayUtils.isEmpty(objs)) {
                    continue;
                }
                // 0 == extension
                // 1 == count
                toReturn.put((String)objs[0], (Long)objs[1]);
            } catch (Exception e) {
                logger.debug("exception get admin file extension stats", e);
            }
        }

        return toReturn;
    }

    public Number getDownloadCount(InformationResourceFile irFile) {
        String sql = String.format(TdarNamedQueries.DOWNLOAD_COUNT_SQL, irFile.getId(), new Date());
        return (Number)getCurrentSession().createNativeQuery(sql).getSingleResult();
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
                // HQL here avoids issue where hibernate delays the delete
                deleteVersionImmediately(version);
                // we don't need safeguards on a translated file, so tell the dao to delete no matter what.
                // informationResourceFileVersionDao.forceDelete(version);
            }
        }
    }

    public void deleteVersionImmediately(InformationResourceFileVersion version) {
        if (PersistableUtils.isNullOrTransient(version)) {
            throw new TdarRecoverableRuntimeException("error.cannot_delete_transient");
        }

        if (version.isUploadedOrArchival()) {
            throw new TdarRecoverableRuntimeException("error.cannot_delete_archival");
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.DELETE_INFORMATION_RESOURCE_FILE_VERSION_IMMEDIATELY);
        query.setParameter("id", version.getId()).executeUpdate();
    }

    public List<InformationResourceFile> findFilesWithStatus(FileStatus[] statuses) {
        Query<InformationResourceFile> query = getCurrentSession().createNamedQuery(QUERY_FILE_STATUS, InformationResourceFile.class);
        query.setParameter("statuses", Arrays.asList(statuses));
        return query.getResultList();
    }

    public List<InformationResource> findInformationResourcesWithFileStatus(
            Person authenticatedUser, List<Status> resourceStatus,
            List<FileStatus> fileStatus) {
        Query<ResourceProxy> query = getCurrentSession().createNamedQuery(QUERY_RESOURCE_FILE_STATUS, ResourceProxy.class);
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        query.setParameter("statuses", resourceStatus);
        query.setParameter("fileStatuses", fileStatus);
        query.setParameter("submitterId", authenticatedUser.getId());
        List<InformationResource> list = new ArrayList<>();
        for (ResourceProxy proxy : query.getResultList()) {
            try {
                list.add((InformationResource) proxy.generateResource());
            } catch (IllegalAccessException | InvocationTargetException
                    | InstantiationException e) {
                logger.error("error happened manifesting: {} ", e);
            }
        }
        return list;
    }

    public ScrollableResults findScrollableVersionsForVerification() {
        Query query = getCurrentSession().getNamedQuery(QUERY_INFORMATION_RESOURCE_FILE_VERSION_VERIFICATION);
        return query.setReadOnly(true).setCacheable(false).scroll(ScrollMode.FORWARD_ONLY);
    }

    public List<InformationResourceFile> findAllExpiredEmbargoes() {
        Query<InformationResourceFile> query = getCurrentSession().createNamedQuery(QUERY_RESOURCE_FILE_EMBARGO_EXIPRED, InformationResourceFile.class);
        DateTime today = new DateTime().withTimeAtStartOfDay();
        query.setParameter("dateStart", today.toDate());
        return query.getResultList();
    }

    public List<InformationResourceFile> findAllEmbargoFilesExpiringTomorrow() {
        Query<InformationResourceFile> query = getCurrentSession().createNamedQuery(QUERY_RESOURCE_FILE_EMBARGOING_TOMORROW, InformationResourceFile.class);
        DateTime today = new DateTime().plusDays(1).withTimeAtStartOfDay();
        query.setParameter("dateStart", today.toDate());
        query.setParameter("dateEnd", today.plusDays(1).toDate());
        return query.getResultList();
    }
}
