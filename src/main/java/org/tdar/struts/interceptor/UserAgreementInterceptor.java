package org.tdar.struts.interceptor;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.UserAgreementController;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.web.SessionData;
import org.tdar.web.SessionDataAware;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.Interceptor;

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
public class UserAgreementInterceptor implements SessionDataAware, Interceptor {

    private static final long serialVersionUID = 7725212619882659204L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient AuthenticationService authenticationService;
    private SessionData sessionData;

    @Autowired
    transient GenericService genericService;

    @Override
    public void destroy() {
        sessionData = null;
    }

    @Override
    public void init() {
        // we don't do anything here, yet...
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        SessionData sessionData = getSessionData();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        if (methodName == null) {
            methodName = "execute";
        }

        TdarUser user = genericService.find(TdarUser.class, sessionData.getTdarUserId());
        Object action = invocation.getAction();
        // user is authenticated and authorized to perform requested action.
        // now we check for any outstanding notices require user attention
        String result = null;
        logger.debug("namespace:{}", ServletActionContext.getActionMapping().getNamespace());

        if (authenticationService.userHasPendingRequirements(user)
                // avoid infinite redirect
                && !(action instanceof UserAgreementController)
                // dont preempt the cart pages
                && !("/cart".equalsIgnoreCase(ServletActionContext.getActionMapping().getNamespace()))) {
            logger.info("user: {} has pending agreements", user);
            result = TdarActionSupport.USER_AGREEMENT;
        } else {
            result = invocation.invoke();
        }
        return result;
    }

    @Override
    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

}
