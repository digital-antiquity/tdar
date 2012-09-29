package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.NDC;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.TdarActionException;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.activity.IgnoreActivity;

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
        //create a tag for this action so that we can (when paired w/ thread name) track its lifecycle in the logs 
        String actionTag =  "" + proxy.getNamespace() + "/" + proxy.getActionName();
        if (methodName == null) {
            methodName = "execute";
        }

        Activity activity = null;
        if (!ReflectionService.classOrMethodContainsAnnotation(action.getClass().getMethod(methodName), IgnoreActivity.class)) {
            activity = new Activity(ServletActionContext.getRequest());
            ActivityManager.getInstance().addActivityToQueue(activity);
        }

        String mark = "READ ONLY";
        if (ReflectionService.classOrMethodContainsAnnotation(action.getClass().getMethod(methodName), WriteableSession.class)) {
            genericService.markWritable();
            mark = "WRITEABLE";
        } else {
            genericService.markReadOnly();
        }
        try {
            //ASSUMPTION: this interceptor and the invoked action run in the _same_ thread.  We tag the NDC  so we can follow this action in the logfile
            NDC.push(actionTag);
            logger.trace(String.format("marking %s/%s session %s", action.getClass().getSimpleName(), methodName, mark));
            return invocation.invoke();
        } catch (TdarActionException exception) {
            logger.warn("caught TdarActionException", exception);
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setStatus(exception.getStatusCode());
            logger.debug("clearing session due to {} -- returning to {}", exception.getResponseStatusCode(), exception.getResultName());
            genericService.clearCurrentSession();
            return exception.getResultName();
        } finally {
            try {
                if (activity != null) {
                    activity.end();
                    logger.debug("activity: {} ", activity);
                }
            }
            finally {
                //overkill perhaps, but we need to be absolutely certain that we pop the NDC.
                NDC.pop();
            }
            
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {
    }

}
