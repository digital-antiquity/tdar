package org.tdar.struts.interceptor;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.HttpParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts.interceptor.annotation.HttpNotFoundErrorOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

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
public class AuthenticationInterceptor extends AbstractAuthenticationInterceptor {

    private static final long serialVersionUID = -3147151913316273258L;

    public static final String SKIP_REDIRECT = "(.*)/(lookup|page-not-found|unauthorized|datatable\\/browse)/(.*)";

    private final WeakHashMap<Class<?>, RequiresTdarUserGroup> requiredGroupClassCache = new WeakHashMap<>();
    private final WeakHashMap<Method, RequiresTdarUserGroup> requiredGroupMethodCache = new WeakHashMap<>();

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient GenericService genericService;



    @Override
    public void destroy() {
        super.destroy();
        authorizationService = null;
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
        String token = getSSoTokenFromParams();
        if (sessionData.isAuthenticated() || validateSsoTokenAndAttachUser(token)) {
            return evaluateRightsOnAction(invocation, sessionData, action, methodName, method);
        }

        if (ReflectionService.methodOrActionContainsAnnotation(invocation, HttpForbiddenErrorResponseOnly.class)) {
            return "forbidden-status-only";
        }
        if (ReflectionService.methodOrActionContainsAnnotation(invocation, HttpNotFoundErrorOnly.class)) {
            return "not-found-status-only";
        }

        setReturnUrl(invocation);
        return Action.LOGIN;
    }

    private String evaluateRightsOnAction(ActionInvocation invocation, SessionData sessionData, Object action, String methodName, Method method) throws Exception {
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
        if (authorizationService.isMember(user, group)) {
            // user is authenticated and authorized to perform requested action
            return invocation.invoke();
        }
        logger.debug("unauthorized access to {}/{} from {} with required group {}", action.getClass().getSimpleName(), methodName, user, group);
        // NOTE, for whatever reason, Struts is not allowing us to swap out the body of the message when we change the http status code
        // thus we need to use the redirect here to get a tDAR error message. This seems to be an issue specific to the FreemarkerHttpResult and this
        // interceptor
        // probably because the action has not been invoked, so we redirect
        return TdarActionSupport.UNAUTHORIZED_REDIRECT;
    }


    protected void setReturnUrl(ActionInvocation invocation) {
        HttpServletRequest request = ServletActionContext.getRequest();
        ActionProxy proxy = invocation.getProxy();
        String returnUrl = String.format("%s/%s", proxy.getNamespace(), proxy.getActionName());
        if (StringUtils.isBlank(proxy.getNamespace())) {
            returnUrl = proxy.getActionName();
        }
        logger.trace(returnUrl);
        if (!request.getMethod().equalsIgnoreCase("get") || returnUrl.matches(SKIP_REDIRECT)) {
            logger.warn("Not setting return url for anything other than a get {}", request.getMethod());
            return;
        }
        getSessionData().setReturnUrl(returnUrl);
        HttpParameters parameters = invocation.getInvocationContext().getParameters();
        getSessionData().setParameters(parameters.toMap());
        logger.debug("setting returnUrl to: {}", getSessionData().getReturnUrl());

    }

}
