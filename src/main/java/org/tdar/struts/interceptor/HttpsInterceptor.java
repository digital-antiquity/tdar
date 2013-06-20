package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.UrlService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.TdarActionSupport;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class HttpsInterceptor implements Interceptor {

    private static final long serialVersionUID = 5032186873591920365L;
    public static final String ERROR_HTTPS_ONLY = "Only Https requests accepted";
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        if (ReflectionService.methodOrActionContainsAnnotation(invocation, HttpsOnly.class)) {
            return doHttpsIntercept(invocation);
        }
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, HttpOnlyIfUnauthenticated.class)) {
            return doHttpIntercept(invocation);
        }
        // not annotated... business as usual.
        return invocation.invoke();
    }

    private String doHttpIntercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        /*
         * If we're not secured or user is authenticated, just go on as usual, otherwise, force unauthenticated users to HTTP
         * this means you google.
         */
        if (request.isSecure() && invocation.getAction() instanceof AuthenticationAware && !((AuthenticationAware) invocation.getAction()).isAuthenticated()) {
            String baseUrl = changeUrlProtocol("http", request);
            response.sendRedirect(baseUrl);
        }
        return invocation.invoke();
    }

    private String changeUrlProtocol(String protocol, HttpServletRequest request) {
        TdarConfiguration config = TdarConfiguration.getInstance();
        int newPort = config.getPort();
        if (protocol == "https") {
            newPort = config.getHttpsPort();
        }

        String baseUrl = String.format("%s://%s%s%s%s", protocol, config.getHostName(), config.getPort() == 80 ? "" : ":" + newPort,
                request.getServletPath(), request.getQueryString() == null ? "" : "?" + request.getQueryString());
        try {
            baseUrl = UrlService.reformatViewUrl(baseUrl);
        } catch (Exception e) {
            logger.error("error in reformatting view URL",e);
        }
        
        return baseUrl;
    }

    private String doHttpsIntercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        if (request.isSecure() || !TdarConfiguration.getInstance().isHttpsEnabled()) {
            return invocation.invoke();
        }

        if (request.getMethod().equalsIgnoreCase("get")) {
            response.sendRedirect(changeUrlProtocol("https", request));
        } else if (invocation.getAction() instanceof TdarActionSupport) {
            logger.warn("ERROR_HTTPS_ONLY");
            ((TdarActionSupport) invocation.getAction()).addActionError(ERROR_HTTPS_ONLY);
        }

        return TdarActionSupport.BAD_REQUEST;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init() {
    }

}
