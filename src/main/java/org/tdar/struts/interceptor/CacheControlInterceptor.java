package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.interceptor.annotation.CacheControl;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * $Id$
 * 
 * Sets pragma no-cache headers
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */
public class CacheControlInterceptor implements Interceptor {

    private static final long serialVersionUID = 7971484508393773725L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ReflectionService reflectionService;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        HttpServletResponse response = ServletActionContext.getResponse();
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, CacheControl.class)) {
            response.setHeader("Cache-Control", "no-store,no-Cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
        String invoke = invocation.invoke();
        return invoke;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {
    }

}
