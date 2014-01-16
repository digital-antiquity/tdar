package org.tdar.core.bean;

import org.tdar.core.bean.resource.Status;

/**
 * General interface for managing things that have status, ultimately, this will include more than just resources
 * but likely anything that can be deduped
 */
public interface HasStatus {

    Status getStatus();

    void setStatus(Status status);

    boolean isDeleted();

    boolean isActive();

    boolean isDraft();

    boolean isFlagged();

    boolean isDuplicate();

}
