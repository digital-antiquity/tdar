package org.tdar.struts.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.external.CrowdService;
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
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class AuthenticationInterceptor implements SessionDataAware, Interceptor {

    private static final long serialVersionUID = -3147151913316273258L;

    public static final String SKIP_REDIRECT = "(.*)/lookup/(.*)";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private transient CrowdService crowdService;
    private SessionData sessionData;
//    private String cacheControlHeaders;
//    @Autowired
//    private TdarConfiguration tdarConfiguration;

    public CrowdService getCrowdService() {
        return crowdService;
    }

    public void setCrowdService(CrowdService crowdService) {
        this.crowdService = crowdService;
    }

    @Override
    public void destroy() {
        crowdService = null;
        sessionData = null;
    }

    @Override
    public void init() {
//        this.cacheControlHeaders = tdarConfiguration.getCacheControlHeaders();
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        if (getSessionData().isAuthenticated()) {
//            setCacheControl(cacheControlHeaders);
            return invocation.invoke();
        }
        // FIXME: these won't display without using a RedirectMessageInterceptor
        // http://glindholm.wordpress.com/2008/07/02/preserving-messages-across-a-redirect-in-struts-2/
        // addActionMessage("You must authenticate before proceeding.");
        // set return url on session data...
        setReturnUrl(invocation);
        return Action.LOGIN;
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
        if (! request.getMethod().equals("GET") || returnUrl.matches(SKIP_REDIRECT)) {
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
