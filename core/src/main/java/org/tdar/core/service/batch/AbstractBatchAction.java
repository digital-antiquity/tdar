package org.tdar.core.service.batch;

import java.io.Serializable;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.dao.base.GenericDao;

public abstract class AbstractBatchAction<C> implements Serializable {

    private static final long serialVersionUID = 8434507061461095484L;
    private TdarUser user;
    private GenericDao genericDao;
    private C existingValue;
    private C newValue;
    
    public String prepareLog(Resource resource) {
        String msg = String.format("changed %s from %s to %s", getFieldName(), getCurrentValue(resource), getNewValue() );
        return msg;
    }
    
    abstract public void performAction(Resource resource, BatchActionType type);
    
    public void setup() {}

    public void complete(Resource resource, String message) {
        ResourceRevisionLog log = new ResourceRevisionLog(message, resource, getUser(), RevisionLogType.EDIT);
        resource.markUpdated(getUser());
        getGenericDao().saveOrUpdate(resource);
        getGenericDao().saveOrUpdate(log);
    }

    public abstract String getFieldName();

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public GenericDao getGenericDao() {
        return genericDao;
    }

    public void setGenericDao(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    public C getNewValue() {
        return newValue;
    }

    public void setNewValue(C newValue) {
        this.newValue = newValue;
    }

    public C getExistingValue() {
        return existingValue;
    }

    public void setExistingValue(C existingValue) {
        this.existingValue = existingValue;
    }

    public abstract C getCurrentValue(Resource resource); 

}
