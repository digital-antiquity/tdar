package org.tdar.core.bean;

import org.tdar.core.bean.resource.Status;

public interface HasStatus {

    public Status getStatus();

    public void setStatus(Status status);

    public boolean isDeleted();

    public boolean isDraft();

    public boolean isActive();
}
