package org.tdar.core.service.integration.dto;

import org.tdar.core.bean.Persistable;

public interface IntegrationDTO<T extends Persistable> extends Persistable {

    public T getPersistable();

    public void setPersistable(T persistable);
}
