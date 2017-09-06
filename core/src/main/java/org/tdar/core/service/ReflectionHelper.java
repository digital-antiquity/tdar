package org.tdar.core.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.annotation.AnnotationUtils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

public class ReflectionHelper {

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String ORG_TDAR = "org.tdar.";
    private static final String EXECUTE = "execute";

    private static transient Logger staticLogger = LoggerFactory.getLogger(ReflectionServiceImpl.class);

    /**
     * Find all classes that implement the identified Class
     * 
     * @param cls
     * @return
     */
    public static <C> Set<Class<? extends C>> findClassesThatImplement(Class<C> cls) {
        Reflections reflection = new Reflections(ORG_TDAR);
       return  reflection.getSubTypesOf(cls);
    }

    private static Map<String, Boolean> annotationLookupCache = new ConcurrentHashMap<String, Boolean>();

    /**
     * Check whether the identified Method or Action has the annotation
     * 
     * @param invocation
     * @param annotationClass
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static boolean methodOrActionContainsAnnotation(ActionInvocation invocation, Class<? extends Annotation> annotationClass) throws SecurityException,
            NoSuchMethodException {
        Object action = invocation.getAction();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        Method method = null;

        if (methodName == null) {
            methodName = EXECUTE;
        }

        String key = annotationClass.getCanonicalName() + "|" + action.getClass().getCanonicalName() + "$" + methodName;
        Boolean found = annotationLookupCache.get(key);
        staticLogger.trace("key: {}, found: {}", key, found);
        if (found != null) {
            return found;
        }

        found = Boolean.FALSE;

        if (action != null) {
            method = action.getClass().getMethod(methodName);
        }

        if (method != null) {
            Object class_ = AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationClass);
            Object method_ = AnnotationUtils.findAnnotation(method, annotationClass);
            found = ((class_ != null) || (method_ != null));
        }

        Annotation parentClassAnnotation = AnnotationUtils.findAnnotation(action.getClass(), annotationClass);
        if (parentClassAnnotation != null) {
            found = true;
        }
        annotationLookupCache.put(key, found);

        return found;
    }

    /**
     * For the specified Method, return the annotation of the identified Class.
     * 
     * @param method
     * @param annotationClass
     * @return
     */
    public static <C extends Annotation> C getAnnotationFromMethodOrClass(Method method, Class<C> annotationClass) {
        C method_ = AnnotationUtils.findAnnotation(method, annotationClass);
        if (method_ != null) {
            return method_;
        }
        C class_ = AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationClass);
        if (class_ != null) {
            return class_;
        }
        return null;
    }

    /**
     * Find all classes or methods that have the identified annotation
     * 
     * @param method
     * @param annotationClass
     * @return
     */
    public static boolean classOrMethodContainsAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        return getAnnotationFromMethodOrClass(method, annotationClass) != null;
    }

    /**
     * find all Classes that support the identified Annotation
     * 
     * @param annots
     * @return
     * @throws NoSuchBeanDefinitionException
     * @throws ClassNotFoundException
     */
    @SafeVarargs
    public static Class<?>[] scanForAnnotation(Class<? extends Annotation>... annots) throws ClassNotFoundException {

        Reflections reflections = new Reflections(ORG_TDAR);
        Set<Class<?>> annotated = new HashSet<Class<?>>();
        for (Class<? extends Annotation> annot : annots) {
            annotated.addAll(reflections.getTypesAnnotatedWith(annot));
        }
        Iterator<Class<?>> iter = annotated.iterator();
        while (iter.hasNext()) {
            Class<?> next = iter.next();
            if (next == null || next.getCanonicalName() == null || next.getSimpleName().contains("$1") || next.getCanonicalName().contains("$1")) {
                staticLogger.trace("removing: {}",next);
                iter.remove();
            }
        }
        return annotated.toArray(new Class<?>[0]);
    }
    

    /**
     * Get the CamelCase name for a field
     * 
     * @param prefix
     * @param name
     * @return
     */
    private static String generateName(String prefix, String name) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Get the return type of a field
     * 
     * @param accessibleObject
     * @return
     */
    public static Class<?> getFieldReturnType(AccessibleObject accessibleObject) {
        final Logger log = LoggerFactory.getLogger(ReflectionServiceImpl.class);

        if (accessibleObject instanceof Field) {
            Field field = (Field) accessibleObject;
            log.trace("generic type: {}", field.getGenericType());
            return getType(field.getGenericType());
        }
        if (accessibleObject instanceof Method) {
            Method method = (Method) accessibleObject;
            log.trace("generic type: {}", method.getGenericReturnType());
            return getType(method.getGenericReturnType());
        }
        return null;
    }

    /**
     * Get the Class of the return type/or generic type
     * 
     * @param type
     * @return
     */
    static Class<?> getType(Type type) {
        Logger logger = LoggerFactory.getLogger(ReflectionServiceImpl.class);

        if (WildcardType.class.isAssignableFrom(type.getClass())) {
            WildcardType subType = (WildcardType) type;
            logger.trace(" wildcard type: {} [{}]", type, type.getClass());
            logger.trace(" lower: {} upper: {}", subType.getLowerBounds(), subType.getUpperBounds());
            return subType.getUpperBounds().getClass();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType collectionType = (ParameterizedType) type;
            logger.trace(" parameterized type: {} [{} - {}]", type, type.getClass(), collectionType.getActualTypeArguments());
            Type subtype = collectionType.getActualTypeArguments()[0];
            logger.trace(" type: {} subtype: {} ", type, subtype);
            if (subtype instanceof Type) {
                return getType(subtype);
            }
            return (Class<?>) subtype;
        }

        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    

    /**
     * Take the method name and try and replace it with the same
     * logic that Hibernate uses
     * 
     * @param name
     * @return
     */
    public static String cleanupMethodName(String name_) {
        String name = name_;
        name = name.replaceAll("^(get|set)", "");
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return name;
    }

    /**
     * String the getter or setter prefix from a method name to get the field name
     * 
     * @param method
     * @return
     */
    public static String cleanupMethodName(Method method) {
        return cleanupMethodName(method.getName());
    }

    /**
     * from the field, generate the appropriate Getter name
     * 
     * @param field
     * @return
     */
    public static String generateGetterName(Field field) {
        return generateGetterName(field.getName());
    }

    /**
     * From the string generate the getter name
     * 
     * @param name
     * @return
     */
    public static String generateGetterName(String name) {
        return generateName(GET, name);
    }

    /**
     * From the field, generate the Setter name
     * 
     * @param field
     * @return
     */
    public static String generateSetterName(Field field) {
        return generateSetterName(field.getName());
    }

    /**
     * From the String, generate the appropriate setter
     * 
     * @param name
     * @return
     */
    public static String generateSetterName(String name) {
        return generateName(SET, name);
    }
    
}
