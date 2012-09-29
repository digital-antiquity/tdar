package org.tdar.struts.action;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

public interface CrudAction<P extends Persistable> {

    public abstract boolean isCreatable() throws TdarActionException;

    public abstract boolean isEditable() throws TdarActionException;

    public abstract boolean isSaveable() throws TdarActionException;

    public abstract boolean isDeleteable() throws TdarActionException;

    public abstract boolean isViewable() throws TdarActionException;

    public abstract Person getAuthenticatedUser();

    public <P> Persistable getPersistable();

    public Class<P> getPersistableClass();

}
