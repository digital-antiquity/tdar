package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.ReflectionService;
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
        // not annotated... business as usual.
        return invocation.invoke();
    }

    private String doHttpsIntercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (request.isSecure()) {
            return invocation.invoke();
        }

        logger.warn("ERROR_HTTPS_ONLY");
        if (invocation.getAction() instanceof TdarActionSupport) {
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
