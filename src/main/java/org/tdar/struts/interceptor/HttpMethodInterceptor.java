package org.tdar.struts.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.struts.action.TdarActionSupport;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.util.AnnotationUtils;

public class HttpMethodInterceptor implements Interceptor{

    public static final String ERROR_POST_ONLY = "Only POST requests accepted";
    private static final long serialVersionUID = -3378318981792368491L;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        
        //check if method uses annotation
        Object action = invocation.getAction();
        Method method = getActionMethod(action.getClass(), invocation.getProxy().getMethod());
        Collection<Method> annotatedMethods = AnnotationUtils.getAnnotatedMethods(action.getClass(), PostOnly.class);
        if (annotatedMethods.contains(method)) {
            return doIntercept(invocation, action);
        }

        //check if method overwites an annotated method
        Class clazz = action.getClass().getSuperclass();
        while (clazz != null) {
            annotatedMethods = AnnotationUtils.getAnnotatedMethods(clazz, PostOnly.class);
            if (annotatedMethods != null) {
                for (Method annotatedMethod : annotatedMethods) {
                    if (annotatedMethod.getName().equals(method.getName())
                            && Arrays.equals(annotatedMethod.getParameterTypes(), method.getParameterTypes())
                            && Arrays.equals(annotatedMethod.getExceptionTypes(), method.getExceptionTypes()))
                        return doIntercept(invocation, action);
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        //not annotated... business as usual.
        return invocation.invoke();
    }
    
    private String doIntercept(ActionInvocation invocation, Object action) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        if(request.getMethod().equals("POST")) {
            return invocation.invoke();
        }
        
        logger.warn("ERROR_POST_ONLY");
        if (action instanceof TdarActionSupport) {
            ((TdarActionSupport) action).addActionError(ERROR_POST_ONLY);
        }

        return TdarActionSupport.BAD_REQUEST;
    }
    
    
    @Override
    public void destroy() {}
    
    @Override
    public void init() {}
    
    // stolen from AnnotationValidationIntercepter
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Method getActionMethod( Class actionClass, String methodName) throws NoSuchMethodException {
        Method method;
        try {
            method = actionClass.getMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException e) {
            // hmm -- OK, try doXxx instead
            try {
                String altMethodName = "do" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
                method = actionClass.getMethod(altMethodName, new Class[0]);
            } catch (NoSuchMethodException e1) {
                // throw the original one
                throw e;
            }
        }
        return method;
    }

}
