package org.tdar.struts.interceptor;

import org.tdar.struts.action.AuthenticationAware;
import org.tdar.web.SessionData;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * $Id$
 * 
 * Verifies requests for admin protected resources.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class AdminAuthenticationInterceptor extends AuthenticationInterceptor {

    private static final long serialVersionUID = -6781980335181526980L;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        SessionData sessionData = getSessionData();
        if (sessionData.isAuthenticated()) {
            // check for admin access.
            if (getCrowdService().isAdministrator(sessionData.getPerson())) {
                return invocation.invoke();
            }
            logger.warn("Unauthorized access to admin area from {} ", sessionData.getPerson());
            return AuthenticationAware.UNAUTHORIZED;
        }
        setReturnUrl(invocation);
        return Action.LOGIN;
    }
 
}
