package org.tdar.balk.struts.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.balk.service.SimpleAuthenticationService;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.struts_base.interceptor.BaseAuthenticationInterceptor;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;

/**
 * $Id$
 * 
 * Verifies requests made for protected resources, or redirects the user to the login screen
 * while preserving the initially requested URL in the session.
 * 
 * Performs group membership checks if the {@link RequiresTdarUserGroup} annotation is set on the Action class or method.
 * By default assumes a group membership of {@link TdarGroup#TDAR_USERS}
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class SimpleAuthenticationInterceptor extends BaseAuthenticationInterceptor {

    private static final long serialVersionUID = 6329883520297931663L;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SimpleAuthenticationService authorizationService;

    @Override
    public void destroy() {
        super.destroy();
        authorizationService = null;
    }

    @Override
    public
    boolean isMemberOf(SessionData sessionData, TdarGroup group, Object action, String methodName) {
        if (authorizationService.isMember(sessionData.getUsername(), group)) {
            // user is authenticated and authorized to perform requested action
            return true;
        }
        logger.debug("unauthorized access to {}/{} from {} with required group {}", action.getClass().getSimpleName(), methodName, sessionData.getUsername(), group);
        return false;
    }

    @Override
    public
    String getSkipRedirectRegex() {
        return "(.*)/(lookup|page-not-found|unauthorized|datatable\\/browse)/(.*)";
    }

}
