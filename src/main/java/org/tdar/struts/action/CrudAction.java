package org.tdar.struts.action;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

//FIXME: can we eliminate the throwable here?  A boolean return is easy to understand but, for example, what are the circumstances that isViewable() should throw an exception instead of simply returning false?
public interface CrudAction<P extends Persistable> {

    /**
     * @return true if model objects governed by this controller supports 'create' semantics (i.e. if 'create' a valid action
     * in the current context). Otherwise return false if 'create' semantics not supported
     *
     * @throws TdarActionException
     */
    public abstract boolean isCreatable() throws TdarActionException;

    /**
     * @return true if model objects governed by this controller supports 'edit' semantics (i.e. if 'edit' a valid action
     * in the current context). Otherwise return false if 'edit' semantics not supported
     *
     * @throws TdarActionException
     */
    public abstract boolean isEditable() throws TdarActionException;

    /**
     * @return true if model objects governed by this controller supports 'save' semantics (i.e. if 'save' a valid action
     * in the current context). Otherwise return false if 'save' semantics not supported
     *
     * @throws TdarActionException
     */
    public abstract boolean isSaveable() throws TdarActionException;

    /**
     * @return true if model objects governed by this controller supports 'delete' semantics (i.e. if 'delete' a valid action
     * in the current context). Otherwise return false if 'delete' semantics not supported
     *
     * @throws TdarActionException
     */
    public abstract boolean isDeleteable() throws TdarActionException;

    /**
     * @return true if model objects governed by this controller supports 'view' semantics (i.e. if 'view' a valid action
     * in the current context). Otherwise return false if 'view' semantics not supported
     *
     * @throws TdarActionException
     */
    public abstract boolean isViewable() throws TdarActionException;

    /**
     * @return Person object representing the user currently logged-in
     */
    public abstract Person getAuthenticatedUser();

    /**
     * @return the model object instance
     */
    public  Persistable getPersistable();

    /**
     * @return the class object of the model object
     */
    public Class<P> getPersistableClass();

}
