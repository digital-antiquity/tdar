package org.tdar.struts.interceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.external.session.SessionDataAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import com.atlassian.crowd.integration.http.HttpAuthenticatorFactory;

/**
 * Determine if a given request contains a token for a crowd SSO session.  If crowd session does exist, make sure
 * that the corresponding tDAR user is "logged in".
 */
public class CrowdTokenInterceptor implements Interceptor, SessionDataAware {

    @Autowired
    AuthenticationService authenticationService;

    SessionData sessionData = new SessionData();
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void destroy() {

    }

    @Override
    public void init() {
    }



    @Override
    public String intercept(ActionInvocation actionInvocation) throws Exception {
        logger.debug("intercept called");

        //TODO: option 1: get cookie named "crowd.token_key", lookup user and groups from CrowdClient
        //TODO: option 2: get httprequest instance,  pass to either authenticator.getUser()
        //TODO: option 3: use deprecated API call via CrowdRestDao.

        ActionContext invocationContext = actionInvocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) invocationContext.get(ServletActionContext.HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) invocationContext.get(ServletActionContext.HTTP_RESPONSE);

        logger.debug("request:{}", request);
        logger.debug("response:{}", response);

        return actionInvocation.invoke();
    }

    @Override public SessionData getSessionData() {
        return sessionData;
    }

    @Override public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }
}
