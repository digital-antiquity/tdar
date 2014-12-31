package org.tdar.core.service.integration.dto;

import java.util.Collection;

import org.tdar.core.bean.Persistable;
import org.tdar.core.exception.I18nException;

public class IntegrationDeserializationException extends I18nException {

    private static final long serialVersionUID = -388002332387954296L;
    private Collection<? extends Persistable> persistables;

    public IntegrationDeserializationException(Collection<? extends Persistable> collection) {
        this.setPersistables(collection);
    }

    public Collection<? extends Persistable> getPersistables() {
        return persistables;
    }

    public void setPersistables(Collection<? extends Persistable> collection) {
        this.persistables = collection;
    }

}
