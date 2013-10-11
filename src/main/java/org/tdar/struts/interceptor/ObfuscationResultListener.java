package org.tdar.struts.interceptor;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.DoNotObfuscate;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

public class ObfuscationResultListener implements PreResultListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    ObfuscationService obfuscationService;
    ReflectionService reflectionService;

    Person user;

    public ObfuscationResultListener(ObfuscationService obfuscationService, ReflectionService reflectionService, Person user) {
        this.obfuscationService = obfuscationService;
        this.reflectionService = reflectionService;
        this.user = user;
    }

    public void prepareResult(Action action) throws Exception {
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
    }

    @Override
    public void beforeResult(ActionInvocation invocation, String resultCode) {
        try {
            prepareResult((Action) invocation.getProxy().getAction());
        } catch (Exception e) {
            logger.debug("{}", e);
            invocation.setResultCode("error");
        }
    }

    private void obfuscateObject(Object obj) {
        //because of generic type arguments, the following (duplicate) instance-of checks are necessary in cases where system
        // returns type of List<I> but we can't figure out what
        if (Iterable.class.isAssignableFrom(obj.getClass())) {
            for (Object obj_ : (Iterable<?>) obj) {
                if (obj_ instanceof Obfuscatable) {
                    obfuscationService.obfuscate((Obfuscatable) obj_, user);
                } else {
                    logger.error("trying to obfsucate something we shouldn't {}", obj.getClass());
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
