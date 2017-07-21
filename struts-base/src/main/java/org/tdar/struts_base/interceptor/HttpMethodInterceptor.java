package org.tdar.struts_base.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class HttpMethodInterceptor implements Interceptor {

    private static final long serialVersionUID = -3378318981792368491L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, PostOnly.class)) {
            return doPostIntercept(invocation);
        } else {
            return invocation.invoke();
        }
    }

    private String doPostIntercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (logger.isTraceEnabled()) {
            logger.trace(" request: {}, {}" , request.getMethod(), invocation.getProxy().getAction(), invocation.getProxy().getActionName());
        }
        if (StringUtils.equalsIgnoreCase("post",request.getMethod())) {
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
