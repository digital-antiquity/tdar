package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.base.HibernateBase;

@Component
public class FileProcessingDao extends HibernateBase<TdarFile> {

    public FileProcessingDao() {
        super(TdarFile.class);
    }

    public List<TdarFile> findFiles(ImportFileStatus... status) {
        List<ImportFileStatus> statuses = new ArrayList<ImportFileStatus>();
        statuses.addAll(Arrays.asList(status));
        Query<TdarFile> query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_FILES_BY_STATUS);
        query.setParameter("status", statuses);
        List<TdarFile> resultList = query.getResultList();

        if (ArrayUtils.contains(status, ImportFileStatus.UPLOADED)) {
            Query<TdarFile> query2 = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_FILES_BY_STATUS);
            query2.setParameter("status", Arrays.asList(ImportFileStatus.VALIDATION_FAILED));
            List<TdarFile> resultList2 = query2.getResultList();
        }
        Map<TdarDir, List<TdarFile>> uploaded = new HashMap<>();
        Map<TdarDir, List<TdarFile>> invalid = new HashMap<>();
        // for (TdarFile file : )

        return resultList;
    }

    public List<AbstractFile> listFilesFor(TdarDir parent, BillingAccount account, String term, TdarUser authenticatedUser) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.LIST_FILES_FOR_DIR);
        if (parent == null && StringUtils.isNotBlank(term)) {
            query.setParameter("topLevel", true);
            query.setParameter("parentId", -1L);

        } else if (parent == null){
            query.setParameter("topLevel", true);
            query.setParameter("parentId", -1L);
        } else {
            query.setParameter("parentId", parent.getId());
            query.setParameter("topLevel", false);
        }
        query.setParameter("account", account);
        if (StringUtils.isBlank(term)) {
            query.setParameter("term", null);
        } else {
            query.setParameter("term", "%" + term.toLowerCase() + "%");
        }
        query.setParameter("uploader", authenticatedUser);
        return query.getResultList();
    }

    public TdarDir findUnfiledDirByName(TdarUser authenticatedUser) {
        try {
            Query<TdarDir> query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_DIR_BY_NAME);
            query.setParameter("name", TdarDir.UNFILED);
            query.setParameter("account", null);
            query.setParameter("uploader", authenticatedUser);
            return query.getSingleResult();
        } catch (Exception e) {
            TdarDir dir = new TdarDir();
            dir.setDateCreated(new Date());
            dir.setFilename(TdarDir.UNFILED);
            dir.setInternalName(TdarDir.UNFILED);
            dir.setUploader(authenticatedUser);
            saveOrUpdate(dir);
            return dir;
        }
    }

}
