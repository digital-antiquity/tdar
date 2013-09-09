package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.UserAgreementAcceptAction;
import org.tdar.struts.action.UserAgreementAction;
import org.tdar.web.SessionData;
import org.tdar.web.SessionDataAware;

import com.opensymphony.xwork2.Action;
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
public class AuthenticationInterceptor implements SessionDataAware, Interceptor {

    private static final long serialVersionUID = -3147151913316273258L;

    public static final String SKIP_REDIRECT = "(.*)/lookup/(.*)";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient AuthenticationAndAuthorizationService authenticationAndAuthorizationService;
    // A Spring AOP session scoped proxy is injected here that retrieves the appropriate SessionData bound to the HTTP Session.
    private SessionData sessionData;

    public AuthenticationAndAuthorizationService getAuthenticationAndAuthorizationService() {
        return authenticationAndAuthorizationService;
    }
    
    public void setAuthenticationAndAuthorizationService(AuthenticationAndAuthorizationService authenticationService) {
        this.authenticationAndAuthorizationService = authenticationService;
    }

    @Autowired
    GenericService genericService;


    @Override
    public void destroy() {
        authenticationAndAuthorizationService = null;
        sessionData = null;
    }

    @Override
    public void init() {
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        SessionData sessionData = getSessionData();
        Object action = invocation.getAction();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        String result;
        if (methodName == null) {
            methodName = "execute";
        }
        if (sessionData.isAuthenticated()) {
            // check for group authorization 
            RequiresTdarUserGroup classLevelRequiresGroupAnnotation = AnnotationUtils.findAnnotation(action.getClass(), RequiresTdarUserGroup.class);
            RequiresTdarUserGroup methodLevelRequiresGroupAnnotation = AnnotationUtils.findAnnotation(action.getClass().getMethod(methodName), RequiresTdarUserGroup.class);
            TdarGroup group = TdarGroup.TDAR_USERS;
            if (methodLevelRequiresGroupAnnotation != null) {
                group = methodLevelRequiresGroupAnnotation.value();                
            }
            else if (classLevelRequiresGroupAnnotation != null) {
                group = classLevelRequiresGroupAnnotation.value();
            }
            Person user = sessionData.getPerson();
            if (getAuthenticationAndAuthorizationService().isMember(user, group)) {
                // user is authenticated and authorized to perform  requested action
                return interceptPendingNotices(invocation, user);
            }
            logger.debug(String.format("unauthorized access to %s/%s from %s with required group %s", action.getClass().getSimpleName(), methodName, user, group));
            return TdarActionSupport.UNAUTHORIZED;
        }
        setReturnUrl(invocation);
        return Action.LOGIN;
    }

    private String  interceptPendingNotices(ActionInvocation invocation, Person user) throws Exception {
        //FIXME: without this refresh,  the redirect from /agreement-response to /dashboard  is broken. Why??
        genericService.refresh(user);

        Object action = invocation.getAction();
        // user is authenticated and authorized to perform  requested action.
        // now we check for any outstanding notices require user attention
        if(authenticationAndAuthorizationService.userHasPendingRequirements(user)
                //avoid infinite redirect
                && !(action instanceof UserAgreementAction) && !(action instanceof UserAgreementAcceptAction)) {
            return TdarActionSupport.USER_AGREEMENT;
        } else {
            return invocation.invoke();
        }
    }


    protected void setCacheControl(String cacheControlHeaders) {
        logger.debug("Setting cache control headers to {}", cacheControlHeaders);
        HttpServletResponse response = ServletActionContext.getResponse();
        if (response == null) {
            logger.warn("No http servlet response available to set headers: {}", cacheControlHeaders);
            return;
        }
        response.setHeader("Cache-control", cacheControlHeaders);
    }

    protected void setReturnUrl(ActionInvocation invocation) {
        HttpServletRequest request = ServletActionContext.getRequest();
        ActionProxy proxy = invocation.getProxy();
        String returnUrl = String.format("%s/%s", proxy.getNamespace(), proxy.getActionName());
        if (!request.getMethod().equalsIgnoreCase("get") || returnUrl.matches(SKIP_REDIRECT)) {
            logger.warn("Not setting return url for anything other than a get {}", request.getMethod());
            return;
        }
        sessionData.setReturnUrl(returnUrl);
        sessionData.setParameters(invocation.getInvocationContext().getParameters());
        logger.debug("setting returnUrl to: {}", sessionData.getReturnUrl());

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
