package org.tdar.core.bean;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.resource.Status;

/**
 * General interface for managing things that have status, ultimately, this will include more than just resources
 * but likely anything that can be deduped
 */
public interface HasStatus {

    Status getStatus();

    void setStatus(Status status);


    @Transient
    @XmlTransient
    public default boolean isDeleted() {
        return getStatus() ==  Status.DELETED;
    }

    @Transient
    @XmlTransient
    public default boolean isActive() {
        return getStatus() ==  Status.ACTIVE;
    }

    @Transient
    @XmlTransient
    public default boolean isDraft() {
        return getStatus() ==  Status.DRAFT;
    }

    @Transient
    @XmlTransient
    public default boolean isDuplicate() {
        return getStatus() ==  Status.DUPLICATE;
    }

    @Transient
    @XmlTransient
    public default boolean isFlagged() {
        return getStatus() ==  Status.FLAGGED;
    }

}
