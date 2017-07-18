package org.tdar.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;

public class CollectionSaveObject<C extends HierarchicalCollection> implements Serializable {

    private static final long serialVersionUID = -8676785579332687294L;

    private C collection;
    private Long parentId;
    private Long alternateParentId;
    private C parent;
    private C alternateParent;
    private TdarUser user;
    private List<AuthorizedUser> users;
    private List<Long> toAdd;
    private List<Long> toRemove;
    private boolean shouldSave = true;
    private FileProxy fileProxy;
    private Long startTime;
    private List<Long> publicToAdd;
    private List<Long> publicToRemove;
    private Class<C> persistableClass;
    
    public CollectionSaveObject(C persistable, TdarUser authenticatedUser, Long startTime2, Class<C> class1) {
        this.collection = persistable;
        this.user = authenticatedUser;
        this.setPersistableClass(class1);
        this.startTime = startTime2;
    }

    public C getCollection() {
        return collection;
    }

    public void setCollection(C collection) {
        this.collection = collection;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getAlternateParentId() {
        return alternateParentId;
    }

    public void setAlternateParentId(Long alternateParentId) {
        this.alternateParentId = alternateParentId;
    }

    public C getParent() {
        return parent;
    }

    public void setParent(C parent) {
        this.parent = parent;
    }

    public C getAlternateParent() {
        return alternateParent;
    }

    public void setAlternateParent(C alternateParent) {
        this.alternateParent = alternateParent;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public List<AuthorizedUser> getUsers() {
        return users;
    }

    public void setUsers(List<AuthorizedUser> users) {
        this.users = users;
    }

    public List<Long> getToAdd() {
        return toAdd;
    }

    public void setToAdd(List<Long> toAdd) {
        this.toAdd = toAdd;
    }

    public List<Long> getToRemove() {
        return toRemove;
    }

    public void setToRemove(List<Long> toRemove) {
        this.toRemove = toRemove;
    }

    public boolean isShouldSave() {
        return shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public FileProxy getFileProxy() {
        return fileProxy;
    }

    public void setFileProxy(FileProxy fileProxy) {
        this.fileProxy = fileProxy;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public List<Long> getPublicToAdd() {
        return publicToAdd;
    }

    public void setPublicToAdd(List<Long> publicToAdd) {
        this.publicToAdd = publicToAdd;
    }

    public List<Long> getPublicToRemove() {
        return publicToRemove;
    }

    public void setPublicToRemove(List<Long> publicToRemove) {
        this.publicToRemove = publicToRemove;
    }


    public Class<C> getPersistableClass() {
        return persistableClass;
    }

    public void setPersistableClass(Class<C> persistableClass) {
        this.persistableClass = persistableClass;
    }

}
