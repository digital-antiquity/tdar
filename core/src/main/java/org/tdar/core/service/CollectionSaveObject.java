package org.tdar.core.service;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;

public class CollectionSaveObject implements Serializable {

    private static final long serialVersionUID = -8676785579332687294L;

    private SharedCollection collection;
    private Long parentId;
    private Long alternateParentId;
    private SharedCollection parent;
    private SharedCollection alternateParent;
    private TdarUser user;
    private List<AuthorizedUser> users;
    private List<Long> toAdd;
    private List<Long> toRemove;
    private boolean shouldSave = true;
    private FileProxy fileProxy;
    private Long startTime;
    private List<Long> publicToAdd;
    private List<Long> publicToRemove;

    public CollectionSaveObject(SharedCollection persistable, TdarUser authenticatedUser, Long startTime2) {
        this.collection = persistable;
        this.user = authenticatedUser;
        this.startTime = startTime2;
    }

    public SharedCollection getCollection() {
        return collection;
    }

    public void setCollection(SharedCollection collection) {
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

    public SharedCollection getParent() {
        return parent;
    }

    public void setParent(SharedCollection parent) {
        this.parent = parent;
    }

    public SharedCollection getAlternateParent() {
        return alternateParent;
    }

    public void setAlternateParent(SharedCollection alternateParent) {
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
