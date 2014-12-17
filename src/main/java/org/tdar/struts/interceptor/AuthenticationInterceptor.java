package org.tdar.struts.interceptor;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.web.SessionData;
import org.tdar.web.SessionDataAware;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * $Id$
 * 
 * Verifies requests made for protected resources, or redirects the user to the login screen
 * while preserving the initially requested URL in the session.
 * 
 * Performs group membership checks if the {@link RequiresTdarUserGroup} annotation is set on the Action class or method.
 * By default assumes a group membership of {@link TdarGroup#TDAR_USERS}
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class AuthenticationInterceptor implements SessionDataAware, Interceptor {

    private static final long serialVersionUID = -3147151913316273258L;

    public static final String SKIP_REDIRECT = "(.*)/(lookup|page-not-found|unauthorized|datatable\\/browse)/(.*)";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final WeakHashMap<Class<?>, RequiresTdarUserGroup> requiredGroupClassCache = new WeakHashMap<>();
    private final WeakHashMap<Method, RequiresTdarUserGroup> requiredGroupMethodCache = new WeakHashMap<>();

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient AuthenticationService authenticationService;
    // A Spring AOP session scoped proxy is injected here that retrieves the appropriate SessionData bound to the HTTP Session.
    private SessionData sessionData;

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    transient GenericService genericService;

    @Override
    public void destroy() {
        authorizationService = null;
        sessionData = null;
    }

    @Override
    public void init() {
        // we don't do anything here, yet...
    }

    private RequiresTdarUserGroup getRequiresTdarUserGroupAnnotation(Class<?> clazz) {
        return checkAndUpdateCache(clazz, requiredGroupClassCache);
    }

    private RequiresTdarUserGroup getRequiresTdarUserGroupAnnotation(Method method) {
        return checkAndUpdateCache(method, requiredGroupMethodCache);
    }

    private <K> RequiresTdarUserGroup checkAndUpdateCache(K key, WeakHashMap<K, RequiresTdarUserGroup> cache) {
        synchronized (cache) {
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            RequiresTdarUserGroup requiredGroup = null;
            if (key instanceof Method) {
                requiredGroup = AnnotationUtils.findAnnotation((Method) key, RequiresTdarUserGroup.class);
            }
            else if (key instanceof Class<?>) {
                requiredGroup = AnnotationUtils.findAnnotation((Class<?>) key, RequiresTdarUserGroup.class);
            }
            cache.put(key, requiredGroup);
            return requiredGroup;
        }
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        SessionData sessionData = getSessionData();
        Object action = invocation.getAction();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        if (methodName == null) {
            methodName = "execute";
        }
        Method method = action.getClass().getMethod(methodName);
        Object[] token = (Object[]) ActionContext.getContext().getParameters().get(TdarConfiguration.getInstance().getRequestTokenName());
        if (sessionData.isAuthenticated() || isValidToken(token)) {
            // FIXME: consider caching these in a local Map
            // check for group authorization
            RequiresTdarUserGroup classLevelRequiresGroupAnnotation = getRequiresTdarUserGroupAnnotation(action.getClass());
            RequiresTdarUserGroup methodLevelRequiresGroupAnnotation = getRequiresTdarUserGroupAnnotation(method);

            TdarGroup group = TdarGroup.TDAR_USERS;
            if (methodLevelRequiresGroupAnnotation != null) {
                group = methodLevelRequiresGroupAnnotation.value();
            }
            else if (classLevelRequiresGroupAnnotation != null) {
                group = classLevelRequiresGroupAnnotation.value();
            }
            TdarUser user = genericService.find(TdarUser.class, sessionData.getTdarUserId());
            if (getAuthorizationService().isMember(user, group)) {
                // user is authenticated and authorized to perform requested action
                return invocation.invoke();
            }
            logger.debug(String.format("unauthorized access to %s/%s from %s with required group %s", action.getClass().getSimpleName(), methodName, user,
                    group));
            // NOTE, for whatever reason, Struts is not allowing us to swap out the body of the message when we change the http status code
            // thus we need to use the redirect here to get a tDAR error message.  This seems to be an issue specific to the FreemarkerHttpResult and this interceptor
            // probably because the action has not been invoked, so we redirect
            return TdarActionSupport.UNAUTHORIZED_REDIRECT;
        }

        if (ReflectionService.methodOrActionContainsAnnotation(invocation, HttpForbiddenErrorResponseOnly.class)) {
            return "forbidden-status-only";
        }

        setReturnUrl(invocation);
        return Action.LOGIN;
    }

    private boolean isValidToken(Object[] token_) {
        if (ArrayUtils.isEmpty(token_)) {
            return false;
        }
        String token = (String)token_[0];
        if (StringUtils.isNotBlank(token)) {
            logger.debug("checking valid token: {}", token);
            boolean result = authenticationService.checkToken((String)token, getSessionData(), ServletActionContext.getRequest()).getType().isValid();
            logger.debug("token authentication result: {}", result);
            return result;
        }
        return false;
    }

    protected void setReturnUrl(ActionInvocation invocation) {
        HttpServletRequest request = ServletActionContext.getRequest();
        ActionProxy proxy = invocation.getProxy();
        String returnUrl = String.format("%s/%s", proxy.getNamespace(), proxy.getActionName());
        if (!request.getMethod().equalsIgnoreCase("get") || returnUrl.matches(SKIP_REDIRECT)) {
            logger.warn("Not setting return url for anything other than a get {}", request.getMethod());
            return;
        }
        sessionData.setReturnUrl(returnUrl);
        sessionData.setParameters(invocation.getInvocationContext().getParameters());
        logger.debug("setting returnUrl to: {}", sessionData.getReturnUrl());

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
