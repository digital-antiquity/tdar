package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.base.HibernateBase;

@Component
public class FileProcessingDao extends HibernateBase<TdarFile>{

    public FileProcessingDao() {
        super(TdarFile.class);
    }

    public List<TdarFile> findFiles(ImportFileStatus ... status) {
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
//        for (TdarFile file : )
        
        return resultList;
    }

    
}
