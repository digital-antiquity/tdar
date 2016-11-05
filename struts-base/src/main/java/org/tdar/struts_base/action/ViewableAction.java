package org.tdar.struts_base.action;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

//FIXME: can we eliminate the throwable here?  A boolean return is easy to understand but, for example, what are the circumstances that isViewable() should throw an exception instead of simply returning false?
public interface ViewableAction<P extends Persistable> {
    /**
     * @return true if model objects governed by this controller supports 'view' semantics (i.e. if 'view' a valid action
     *         in the current context). Otherwise return false if 'view' semantics not supported
     * 
     * @throws TdarActionException
     */
    boolean authorize() throws TdarActionException;

    /**
     * @return Person object representing the user currently logged-in
     */
    TdarUser getAuthenticatedUser();

    /**
     * @return the model object instance
     */
    Persistable getPersistable();

    /**
     * @return the class object of the model object
     */
    Class<P> getPersistableClass();

}
