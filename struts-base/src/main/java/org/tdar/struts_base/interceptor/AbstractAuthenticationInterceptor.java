package org.tdar.struts_base.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.external.session.SessionDataAware;

import com.opensymphony.xwork2.interceptor.Interceptor;

public abstract class AbstractAuthenticationInterceptor implements SessionDataAware, Interceptor {

    private static final long serialVersionUID = -89612159086314236L;

    Logger logger = LoggerFactory.getLogger(getClass());
    protected TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    @Autowired
    transient AuthenticationService authenticationService;

    private SessionData sessionData;
    
    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public void destroy() {
        sessionData = null;
        authenticationService = null;
    }
    
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }


    protected boolean validateSsoTokenAndAttachUser(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        if (!CONFIG.ssoEnabled()) {
            return false;
        }

        logger.trace("checking valid token: {}", token);
        AuthenticationResult result = authenticationService.checkToken((String) token, getSessionData(), ServletActionContext.getRequest());
        logger.debug("token authentication result: {}", result);
        return result.getType().isValid();
    }


    protected String getSSoTokenFromParams() {
        return authenticationService.getSsoTokenFromRequest(ServletActionContext.getRequest());
    }
    
}
