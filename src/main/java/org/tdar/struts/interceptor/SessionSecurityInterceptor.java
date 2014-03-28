package org.tdar.struts.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.NDC;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.CacheControl;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.WriteableSession;
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
public class SessionSecurityInterceptor implements SessionDataAware, Interceptor {

    private final static long serialVersionUID = -6781980335181526980L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    enum SessionType {
        READ_ONLY,WRITEABLE
    }
    
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient ObfuscationService obfuscationService;
    @Autowired
    private transient ReflectionService reflectionService;
    private SessionData sessionData;
    private boolean sessionClosed = false;

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
            activity = new Activity(ServletActionContext.getRequest());
            if (getSessionData() != null && getSessionData().isAuthenticated()) {
                activity.setUser(sessionData.getPerson());
            }
            ActivityManager.getInstance().addActivityToQueue(activity);
        }
        logger.debug("<< activity begin: {} ", activity);

        HttpServletResponse response = ServletActionContext.getResponse();
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, CacheControl.class)) {
            response.setHeader("Cache-Control", "no-store,no-Cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
        
        
        response.setHeader("Access-Control-Allow-Origin", "*");

        SessionType mark = SessionType.READ_ONLY;
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, WriteableSession.class)) {
            genericService.markWritable();
            mark = SessionType.WRITEABLE;
        } else {
            genericService.markReadOnly();
        }
        try {
            // ASSUMPTION: this interceptor and the invoked action run in the _same_ thread. We tag the NDC so we can follow this action in the logfile
            NDC.push(Activity.formatRequest(ServletActionContext.getRequest()));
            logger.trace(String.format("marking %s/%s session %s", action.getClass().getSimpleName(), methodName, mark));
            if (!TdarConfiguration.getInstance().obfuscationInterceptorDisabled()) {
                if (SessionType.READ_ONLY.equals(mark) || !ReflectionService.methodOrActionContainsAnnotation(invocation, DoNotObfuscate.class)) {
                    invocation.addPreResultListener(new ObfuscationResultListener(obfuscationService, reflectionService, this, sessionData.getPerson()));
                }
            }
            String invoke = invocation.invoke();
            return invoke;
        } catch (TdarActionException exception) {
            if (StatusCode.shouldShowException(exception.getStatusCode())) {
                logger.warn("caught TdarActionException ({})", exception.getStatusCode(), exception);
            } else {
                logger.warn("caught TdarActionException", exception.getMessage());
            }
            response.setStatus(exception.getStatusCode());
            logger.debug("clearing session due to {} -- returning to {}", exception.getResponseStatusCode(), exception.getResultName());
            genericService.clearCurrentSession();
            setSessionClosed(true);
            return exception.getResultName();
        } finally {
            try {
                if (activity != null) {
                    activity.end();
                    logger.debug(">> activity end: {} ", activity);
                }
            } finally {
                // overkill perhaps, but we need to be absolutely certain that we pop the NDC.
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

    @Override
    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    public boolean isSessionClosed() {
        return sessionClosed;
    }

    public void setSessionClosed(boolean sessionClosed) {
        this.sessionClosed = sessionClosed;
    }

}
