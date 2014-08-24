package org.tdar.struts.interceptor;

import org.apache.log4j.NDC;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.activity.Activity;
import org.tdar.utils.activity.IgnoreActivity;
import org.tdar.web.SessionData;
import org.tdar.web.SessionDataAware;

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
public class ActivityLoggingInterceptor implements SessionDataAware, Interceptor {

    private static final long serialVersionUID = -9075492140512991012L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ReflectionService reflectionService;
    @Autowired
    private transient GenericService genericService;
    private SessionData sessionData;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        // create a tag for this action so that we can (when paired w/ thread name) track its lifecycle in the logs
        // String actionTag = "" + proxy.getNamespace() + "/" + proxy.getActionName();
        if (methodName == null) {
            methodName = "execute";
        }

        Activity activity = null;
        if (!ReflectionService.methodOrActionContainsAnnotation(invocation, IgnoreActivity.class)) {
            activity = new Activity(ServletActionContext.getRequest(), null);
            if ((getSessionData() != null) && getSessionData().isAuthenticated()) {
                activity.setUser(genericService.find(TdarUser.class, sessionData.getTdarUserId()));
            }
            ActivityManager.getInstance().addActivityToQueue(activity);
            logger.debug("<< activity begin: {} ", activity);
        }

        // ASSUMPTION: this interceptor and the invoked action run in the _same_ thread. We tag the NDC so we can follow this action in the logfile
        NDC.push(Activity.formatRequest(ServletActionContext.getRequest()));
        logger.trace(String.format("marking %s/%s session", action.getClass().getSimpleName(), methodName));
        String invoke = TdarActionSupport.SUCCESS;
        try {
            invoke = invocation.invoke();
        } catch (Throwable t) {
            throw t;
        } finally {
            if (activity != null) {
                activity.end();
                logger.debug(">> activity end: {} ", activity);
            }
            NDC.pop();
        }
        return invoke;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {
    }

    @Override
    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }
}
