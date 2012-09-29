package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ScheduledProcessService;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.TdarActionException;
import org.tdar.utils.activity.Activity;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * $Id$
 * 
 * Changes the default settings for the session to ensure that things coming onto the session have the
 * right properties for persistence. In most cases, we should be operating in a READ-ONLY world.
 * 
 * the @WriteableSession annotation should be used to explicitly make the default be writable for a method
 * or an entire class
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */
public class SessionSecurityInterceptor implements Interceptor {

    private final static long serialVersionUID = -6781980335181526980L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient GenericService genericService;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        if (methodName == null) {
            methodName = "execute";
        }
        WriteableSession writeableClass = AnnotationUtils.findAnnotation(action.getClass(), WriteableSession.class);
        WriteableSession writeableMethod = AnnotationUtils.findAnnotation(action.getClass().getMethod(methodName), WriteableSession.class);

        Activity activity = new Activity(ServletActionContext.getRequest());
        // scheduledProcessService.addActivityToQueue(activity);

        String mark = "READ ONLY";
        if (writeableClass != null || writeableMethod != null) {
            genericService.markWritable();
            mark = "WRITEABLE";
        } else {
            genericService.markReadOnly();
        }
        logger.trace(String.format("marking %s/%s session %s", action.getClass().getSimpleName(), methodName, mark));
        try {
            return invocation.invoke();
        } catch (TdarActionException exception) {
            logger.warn("caught TdarActionException", exception);
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setStatus(exception.getStatusCode());
            logger.debug("clearing session due to {} -- returning to {}", exception.getResponseStatusCode(), exception.getResultName());
            genericService.clearCurrentSession();
            return exception.getResultName();
        } finally {
            activity.end();
            logger.debug("activity: {} ", activity);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {
    }

}
