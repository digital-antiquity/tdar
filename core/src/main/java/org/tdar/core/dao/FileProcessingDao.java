package org.tdar.core.dao;

import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.base.HibernateBase;

@Component
public class FileProcessingDao extends HibernateBase<TdarFile>{

    public FileProcessingDao() {
        super(TdarFile.class);
    }

    public List<TdarFile> findFiles(ImportFileStatus status) {
        Query<TdarFile> query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_FILES_BY_STATUS);
        query.setParameter("status", status);
        return query.getResultList();
    }

    
}
