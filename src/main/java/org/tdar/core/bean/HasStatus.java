package org.tdar.core.bean;

import org.tdar.core.bean.resource.Status;

/*
 * General interface for managing things that have status, ultimately, this will include more than just resources
 * but likely anything that can be deduped
 */
public interface HasStatus {

    public Status getStatus();

    public void setStatus(Status status);

    public boolean isDeleted();

    public boolean isActive();

    public boolean isDraft();

    public boolean isFlagged();

    public boolean isDuplicate();

}
