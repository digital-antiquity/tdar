package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ReflectionHelper;
import org.tdar.core.service.UrlService;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.AuthenticationAware;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class HttpsInterceptor implements Interceptor {

    private static final long serialVersionUID = 5032186873591920365L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        if (ReflectionHelper.methodOrActionContainsAnnotation(invocation, HttpsOnly.class)) {
            return doHttpsIntercept(invocation);
        }
        if (ReflectionHelper.methodOrActionContainsAnnotation(invocation, HttpOnlyIfUnauthenticated.class)) {
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
        if (request.isSecure() && (invocation.getAction() instanceof AuthenticationAware) &&
                !((AuthenticationAware) invocation.getAction()).isAuthenticated()) {
            String baseUrl = changeUrlProtocol("http", request);
            response.sendRedirect(baseUrl);
            return null;
        }
        return invocation.invoke();
    }

    private String changeUrlProtocol(String protocol, HttpServletRequest request) {
        TdarConfiguration config = TdarConfiguration.getInstance();
        int newPort = config.getPort();
        if (protocol == "https") {
            newPort = config.getHttpsPort();
        }

        String port = config.getPort() == 80 ? "" : ":" + newPort;
        String baseUrl = String.format("%s://%s%s%s%s", protocol, config.getHostName(), port, request.getContextPath(), UrlService.getOriginalUrlPath(request));

        if (request.getServletPath().equals("/about")) {
            baseUrl = baseUrl.replace("/about", "/");
        }
        // try {
        // baseUrl = UrlService.reformatViewUrl(baseUrl);
        // } catch (Exception e) {
        // logger.error("error in reformatting view URL", e);
        // }

        return baseUrl;
    }

    private String doHttpsIntercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setHeader("Frame-Options:", "DENY");

        if (request.isSecure() || !TdarConfiguration.getInstance().isHttpsEnabled()
                || StringUtils.startsWithIgnoreCase(request.getRequestURL().toString(), "https:")) {
            return invocation.invoke();
        }
        logger.trace(" :: url: {} : {}", request.getRequestURI(), request.getQueryString());

        if (request.getMethod().equalsIgnoreCase("get") || request.getMethod().equalsIgnoreCase("head")) {
            // change redirect to be permanent
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", changeUrlProtocol("https", request));

            return null;
        } else if (invocation.getAction() instanceof TdarActionSupport) {
            logger.warn("ERROR_HTTPS_ONLY");
            ((TdarActionSupport) invocation.getAction()).addActionError(MessageHelper.getMessage("httpsInterceptor.error_https_only", invocation
                    .getInvocationContext().getLocale()));
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
