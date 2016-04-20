package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.utils.MessageHelper;

import com.google.common.base.Objects;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class HttpMethodInterceptor implements Interceptor {

    private static final long serialVersionUID = -3378318981792368491L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, PostOnly.class)) {
            return doPostIntercept(invocation);
            //FIXME: it shouldn't be necessary to check for get request, and in fact might break requests such as HEAD, OPTIONS, PUT, and DELETE
        } else if (request.getMethod().equalsIgnoreCase("get") || Objects.equal(invocation.getProxy().getMethod(), "delete")) {
            // not annotated... business as usual.
            return invocation.invoke();
        } else {
            return TdarActionSupport.BAD_REQUEST;
        }
    }

    private String doPostIntercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (request.getMethod().equalsIgnoreCase("post")) {
            return invocation.invoke();
        }

        logger.warn("ERROR_POST_ONLY");
        if (invocation.getAction() instanceof TdarActionSupport) {

            ((TdarActionSupport) invocation.getAction()).addActionError(MessageHelper.getMessage("httpMethodInterceptor.error_post_only", invocation
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
