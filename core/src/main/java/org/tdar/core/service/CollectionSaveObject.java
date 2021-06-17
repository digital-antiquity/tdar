package org.tdar.core.service;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;

public class CollectionSaveObject implements Serializable {

    private static final long serialVersionUID = -8676785579332687294L;

    private ResourceCollection collection;
    private Long parentId;
    private Long alternateParentId;
    private ResourceCollection parent;
    private ResourceCollection alternateParent;
    private TdarUser user;
    private List<AuthorizedUser> users;
    private List<Long> toAdd;
    private List<Long> toRemove;
    private boolean shouldSave = true;
    private FileProxy fileProxy;
    private Long startTime;
    private List<Long> publicToAdd;
    private List<Long> publicToRemove;

    public CollectionSaveObject(ResourceCollection persistable, TdarUser authenticatedUser, Long startTime2) {
        this.collection = persistable;
        this.user = authenticatedUser;
        this.startTime = startTime2;

        // FIXME: this is a workaround for TDAR-6240
        // system fails to save logo because display properties id is null
        if(persistable.getProperties() != null) {
            persistable.getProperties().setId(persistable.getId());
        }
    }

    public ResourceCollection getCollection() {
        return collection;
    }

    public void setCollection(ResourceCollection collection) {
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

    public ResourceCollection getParent() {
        return parent;
    }

    public void setParent(ResourceCollection parent) {
        this.parent = parent;
    }

    public ResourceCollection getAlternateParent() {
        return alternateParent;
    }

    public void setAlternateParent(ResourceCollection alternateParent) {
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
}
