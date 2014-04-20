package org.tdar.struts.interceptor;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

public class ObfuscationResultListener implements PreResultListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private ObfuscationService obfuscationService;
    private ReflectionService reflectionService;
    private SessionSecurityInterceptor sessionSecurityInterceptor;
    TdarUser user;

    public ObfuscationResultListener(ObfuscationService obfuscationService, ReflectionService reflectionService,
            SessionSecurityInterceptor sessionSecurityInterceptor, TdarUser user) {
        this.obfuscationService = obfuscationService;
        this.reflectionService = reflectionService;
        this.sessionSecurityInterceptor = sessionSecurityInterceptor;
        this.user = user;
    }

    public void prepareResult(Action action) throws Exception {
        logger.trace("begin obfuscation");
        Class<? extends Object> controllerClass = action.getClass();
        List<Pair<Method, Class<? extends Obfuscatable>>> testReflection = reflectionService.findAllObfuscatableGetters(controllerClass);

        for (Pair<Method, Class<? extends Obfuscatable>> pair : testReflection) {
            Method method = pair.getFirst();
            Class<? extends Obfuscatable> cls = pair.getSecond();
            if (method.isAnnotationPresent(DoNotObfuscate.class)) {
                continue;
            }
            logger.trace("{} <==> {}", method, cls);
            // try {
            Object obj = method.invoke(action);
            if (obj == null) {
                continue;
            }
            obfuscateObject(obj);
            // } catch (Exception e) {
            // logger.error("{}", e);
            // }
        }
        logger.trace("complete obfuscation");
    }

    @Override
    public void beforeResult(ActionInvocation invocation, String resultCode) {
        try {
            prepareResult((Action) invocation.getProxy().getAction());
        } catch (Exception e) {
            // if the session is already closed, then we don't want to actually worry about session closed errors
            // if the session is not closed, then we probably have a real error here
            if ((sessionSecurityInterceptor != null) && !sessionSecurityInterceptor.isSessionClosed()) {
                logger.error("error durring obfuscation", e);
                invocation.setResultCode("error");
            }
        }
    }

    private void obfuscateObject(Object obj) {
        // because of generic type arguments, the following (duplicate) instance-of checks are necessary in cases where system
        // returns type of List<I> but we can't figure out what
        if (Iterable.class.isAssignableFrom(obj.getClass())) {
            for (Object obj_ : (Iterable<?>) obj) {
                if (obj_ instanceof Obfuscatable) {
                    obfuscationService.obfuscate((Obfuscatable) obj_, user);
                } else {
                    logger.warn("trying to obfsucate something we shouldn't {}", obj.getClass());
                }
            }
        } else {
            if (obj instanceof Obfuscatable) {
                obfuscationService.obfuscate((Obfuscatable) obj, user);
            } else {
                logger.error("trying to obfsucate something we shouldn't {}", obj.getClass());
            }
        }
    }

}
