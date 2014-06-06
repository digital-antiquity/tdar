package org.tdar.struts.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
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

            // if method has matching "setter" than call the getter, replace the object with a proxy and move on
            Object obj = method.invoke(action);
            if (obj == null) {
                continue;
            }
            Method setter = reflectionService.findMatchingSetter(method);
            if (setter != null) {
                // generate proxy wrapper
                Class<?> actual = obj.getClass(); // method.getReturnType().getDeclaringClass();
                try {
                    if (obj instanceof Collection && CollectionUtils.isEmpty((Collection) obj) || obj == Project.NULL || obj == DataTableColumn.TDAR_ROW_ID) {
                        logger.trace("SKIPPING: {} EMPTY COLLECTION | FINAL OBJECT", obj);
                        continue;
                    }
                    
                    Object result = enhance(obj, obfuscationService, user);
                    reflectionService.callFieldSetter(action, reflectionService.getFieldForGetterOrSetter(setter), actual.cast(result));
                } catch (Exception e) {
                    logger.error("exception in calling: {} {} {}", method, obj, actual, e);
                }
            } else {
                obfuscationService.obfuscateObject(obj, user);
            }
        }
        logger.trace("complete obfuscation");
    }

    public static Object enhance(Object obj, ObfuscationService obfuscationService, TdarUser user) {
        if (obj == null || obj.getClass() == null) {
            return obj;
        }
        Class<? extends Object> actual = obj.getClass();
        while (Enhancer.isEnhanced(actual)) {
            actual = actual.getSuperclass();
        }
        return Enhancer.create(actual, new CollectionMethodInterceptor(obj, obfuscationService, user));
    }

    static class CollectionMethodInterceptor implements InvocationHandler {

        private Object object;
        private ObfuscationService obfuscationService;
        private TdarUser user;

        protected final transient Logger logger = LoggerFactory.getLogger(getClass());

        public CollectionMethodInterceptor(Object object, ObfuscationService obfuscationService, TdarUser user) {
            this.object = object;
            this.obfuscationService = obfuscationService;
            this.user = user;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            Object invoke = method.invoke(object, arguments);
            if (invoke != null && (method.getName().startsWith("iterator"))) {
                if (invoke instanceof Iterator) {
                    logger.debug("intercepting: {}", method);
                    return new AbstractIteratorDecorator((Iterator) invoke) {
                        @Override
                        public Object next() {
                            Object next = super.next();
                            if (next instanceof Obfuscatable) {
                                logger.trace("\tobfuscating: {} ", next);
                                obfuscationService.obfuscate((Obfuscatable) next, user);
                                return enhance(next, obfuscationService, user);
                            } else {
                                return next;
                            }
                        }
                    };
                }
                return enhance(invoke, obfuscationService, user);
            }
            return invoke;
        }

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

}
