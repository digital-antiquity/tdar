package org.tdar.struts.action;

public abstract class AbstractAdminControllerITCase extends AbstractControllerITCase {

    @Override
    protected Long getUserId() {
        return getAdminUserId();
    }

}
