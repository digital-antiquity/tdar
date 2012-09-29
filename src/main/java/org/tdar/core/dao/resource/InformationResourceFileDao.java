package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.dao.Dao.HibernateBase;

@Component
public class InformationResourceFileDao extends HibernateBase<InformationResourceFile> {

    public InformationResourceFileDao() {
        super(InformationResourceFile.class);
    }

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return findByProperty("filestoreId", filestoreId);
    }
}
