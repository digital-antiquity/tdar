package org.tdar.core.dao.citation;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.dao.Dao;

@Component
public class SourceCollectionDao extends Dao.HibernateBase<SourceCollection>
{
    public SourceCollectionDao() {
        super(SourceCollection.class);
    }
}
