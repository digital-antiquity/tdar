package org.tdar.balk.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.tdar.core.bean.AbstractPersistable;

@Entity(name = "dropbox_items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "item_type")
public abstract class AbstractDropboxItem extends AbstractPersistable {

    private static final long serialVersionUID = 4105422216656230212L;
    @Column(name = "dropbox_id", length = 255, nullable = false)
    private String dropboxId;
    @Column(name = "name", length = 512, nullable = false)
    private String name;
    @Column(name = "path", length = 2048)
    private String path;
    @Column(name = "owner_id", length = 512)
    private String ownerId;
    @Column(name = "size")
    private Integer size;
    @Column(name = "item_type", length = 10, nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ItemType type;
    @Column(name = "date_added", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAdded;

    @Column(name = "date_modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModified;

    @Column(name = "tdar_id")
    private Long tdarId;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "parent_id", length = 512)
    private String parentId;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDropboxId() {
        return dropboxId;
    }

    public void setDropboxId(String dropboxId) {
        this.dropboxId = dropboxId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public Long getTdarId() {
        return tdarId;
    }

    public void setTdarId(Long tdarId) {
        this.tdarId = tdarId;
    }

}
