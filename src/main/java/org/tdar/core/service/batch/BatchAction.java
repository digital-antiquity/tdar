package org.tdar.core.service.batch;

import java.io.Serializable;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.dao.GenericDao;

public abstract class BatchAction implements Serializable {

    private static final long serialVersionUID = 8434507061461095484L;
    private ResourceCollection collection;

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
        this.collection = collection;
    }

    abstract public ResourceRevisionLog performAction(Resource resource, TdarUser user);

    public void setup(GenericDao genericDao) {
        setCollection(genericDao.loadFromSparseEntity(getCollection(), ResourceCollection.class));
    }

}
