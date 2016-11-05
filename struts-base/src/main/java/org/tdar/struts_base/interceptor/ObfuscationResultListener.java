package org.tdar.struts_base.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * 
 * This ResultListener is executed after the Action has been invoked and is ready to get passed into the template layer.
 * It scans for all bean properties implementing Obfuscatable (or collections) and replaces them on the Action with a lazy proxy.
 * A setter is not required but a backing instance variable must be present.
 * 
 * FIXME: Consider replacing using reflection to implicitly harvest all obfuscatable beans with a more explicit model where the Action
 * specifies directly which beans should be obfuscated. For simplicity!
 * 
 * 
 * @author Adam Brin
 */
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
        if (action instanceof TdarActionSupport) {
            ((TdarActionSupport) action).setFreemarkerProcessingTime(new Date());
        }
        logger.trace("begin obfuscation");
        Class<? extends Object> controllerClass = action.getClass();
        // get a list of the getters that are either a Collection<?> or <? extends Obfuscatable>
        List<Pair<Method, Class<? extends Obfuscatable>>> testReflection = reflectionService.findAllObfuscatableGetters(controllerClass);

        for (Pair<Method, Class<? extends Obfuscatable>> pair : testReflection) {
            Method method = pair.getFirst();
            Class<? extends Obfuscatable> cls = pair.getSecond();
            if (method.isAnnotationPresent(DoNotObfuscate.class)) {
                continue;
            }

            // try {

            // if method has matching "setter" than call the getter, replace the object with a proxy and move on
            Object obj = method.invoke(action);
            if (obj == null) {
                continue;
            }
            boolean old = true;
            // old way -- obfuscate everything
            if (old) {
                obfuscationService.obfuscateObject(obj, user);
            } else {
                // call the setter, if exists
                Method setter = reflectionService.findMatchingSetter(method);
                logger.trace("{} <==> {} {}", method, cls, setter);
                if (setter != null) {
                    // if the setter exists, generate proxy wrapper
                    Class<?> actual = obj.getClass();
                    try {
                        // if the object is a collection, and the collection is empty, or the object is one of our static types, skip
                        if (obj instanceof Collection && CollectionUtils.isEmpty((Collection<?>) obj) || obj == Project.NULL
                                || obj == DataTableColumn.TDAR_ROW_ID) {
                            logger.trace("SKIPPING: {} EMPTY COLLECTION | FINAL OBJECT", obj);
                            continue;
                        }
                        // otherwise create a CGLIB proxy of the object
                        Object result = enhance(obj, obfuscationService, user);
                        // call the setter on the object
                        setter.invoke(action, actual.cast(result));
                    } catch (Exception e) {
                        logger.error("exception in calling: {} {} {}", method, obj, actual, e);
                    }
                } else {
                    // if there's no setter, obfuscate the object directly
                    obfuscationService.obfuscateObject(obj, user);
                }
            }
        }
        logger.trace("complete obfuscation");
    }

    /*
     * Create a CGLIB enhanced version of the object
     */
    public static Object enhance(Object obj, ObfuscationService obfuscationService, TdarUser user) {
        if (obj == null || obj.getClass() == null) {
            return obj;
        }
        Class<? extends Object> actual = obj.getClass();
        // if we're dealing with a CGLIB proxy already, get the first class that's not a cglib proxy, and use that as our base-class
        // NOTE: this can mean that we're actually proxying a proxy we created, try and see if we can figure out if we've touched this instance in the future
        while (Enhancer.isEnhanced(actual)) {
            actual = actual.getSuperclass();
        }
        return Enhancer.create(actual, new CollectionMethodInterceptor(obj, obfuscationService, user));
    }

    /*
     * A wrapper for our method proxy -- if we deal with a collection, wrap the iterator. otherwise, continue recursive proxying
     */
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

        /*
         * Proxy all methods
         * 
         * @see net.sf.cglib.proxy.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            Object invoke = method.invoke(object, arguments);
            // if we're proxying an iterator, then, wrap it with an IteratorDecorator
            if (invoke != null && (method.getName().startsWith("iterator"))) {
                if (invoke instanceof Iterator) {
                    logger.debug("intercepting: {}", method);
                    return new AbstractIteratorDecorator((Iterator<?>) invoke) {
                        @Override
                        public Object next() {
                            Object next = super.next();
                            // if we're dealing wtih an obfuscatable, call obfuscate when next() is called, otherwise, return the base object
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
                // otherwise enahnce the returned object
                return enhance(invoke, obfuscationService, user);
            }
            // default
            return invoke;
        }

    }

    @Override
    public void beforeResult(ActionInvocation invocation, String resultCode) {
        Object action = invocation.getProxy().getAction();
        if (action instanceof Action) {
            try {
                prepareResult((Action) invocation.getProxy().getAction());
            } catch (Exception e) {
                // if the session is already closed, then we don't want to actually worry about session closed errors
                // if the session is not closed, then we probably have a real error here
                if ((sessionSecurityInterceptor != null) && !sessionSecurityInterceptor.isSessionClosed()) {
                    logger.error("error durring obfuscation", e);
                    // if (!invocation.getResultCode().equals(TdarActionSupport.INPUT)) {
                    invocation.setResultCode(TdarActionSupport.ERROR);
                    // }
                }
            }
        }
    }

}
