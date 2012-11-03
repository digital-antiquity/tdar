/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.ReflectionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

/**
 * @author Adam Brin
 * 
 */
@Service
public class ReflectionService {

    public transient Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, Class<Persistable>> persistableLookup;

    /*
     * This method looks at a class like "Resource" and finds fields that contain the "classToFind",
     * e.g. GeographicKeyword. This would return [geographicKeywords,managedGeographicKeywords]
     */
    public Set<Field> findFieldsReferencingClass(Class<?> classToInspect, Class<?> classToFind) {
        Set<Field> matchingFields = new HashSet<Field>();
        for (Field field : classToInspect.getDeclaredFields()) {
            if (getFieldReturnType(field).equals(classToFind)) {
                matchingFields.add(field);
            }
        }
        logger.debug("Found Fields:{} on {}", matchingFields, classToInspect.getSimpleName());
        return matchingFields;
    }

    // todo: do we really need an extra function for this?
    public Set<Field> findAssignableFieldsRefererencingClass(Class<?> classToInspect, Class<?> ancestorToFind) {
        Set<Field> matchingFields = new HashSet<Field>();
        for (Field field : classToInspect.getDeclaredFields()) {
            if (getFieldReturnType(field).isAssignableFrom(ancestorToFind)) {
                matchingFields.add(field);
            }
        }
        logger.debug("Fields in {} that refer to {}:{}", new Object[] { classToInspect.getSimpleName(), ancestorToFind.getSimpleName(), matchingFields });
        return matchingFields;
    }

    public Set<Field> findFieldsWithAnnotation(Class<?> targetClass, List<Class<? extends Annotation>> list, boolean recursive) {
        Set<Field> set = new HashSet<Field>();
        for (Field field : targetClass.getDeclaredFields()) {
            for (Class<? extends Annotation> ann : list) {
                if (field.isAnnotationPresent(ann)) {
                    set.add(field);
                }
            }
        }
        if (recursive) {
            for (Class<?> parent : ReflectionTools.getClassHierarchy(targetClass)) {
                set.addAll(findFieldsWithAnnotation(parent, list, false));
            }
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    public void warmUp(Object obj, int i) {
        logger.debug("warming up: {} ", obj);
        Set<Field> fields = findFieldsWithAnnotation(obj.getClass(), Arrays.asList(ManyToMany.class, ManyToOne.class, OneToMany.class, OneToOne.class), true);
        for (Field field : fields) {
            Object result = callFieldGetter(obj, field);
            logger.trace("{}", result);
            if (result == null)
                continue;
            if (result instanceof Collection<?> && i > 0) {
                for (Object child : (Collection<?>) result) {
                    warmUp(child, i - 1);
                }
            } else if (field.getAnnotation(OneToOne.class) != null) {
                warmUp(result, 0);
            }
        }
    }

    /**
     * Take the method name and try and replace it with the same
     * logic that Hibernate uses
     * 
     * @param name
     * @return
     */
    public static String cleanupMethodName(String name) {
        name = name.replaceAll("^(get|set)", "");
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return name;
    }

    public static String cleanupMethodName(Method method) {
        return cleanupMethodName(method.getName());
    }

    public static String generateGetterName(Field field) {
        return generateGetterName(field.getName());
    }

    public static String generateGetterName(String name) {
        return generateName("get", name);
    }

    public static String generateSetterName(Field field) {
        return generateSetterName(field.getName());
    }

    public static String generateSetterName(String name) {
        return generateName("set", name);
    }

    /*
     * Based on the field and the object passed in, call the getter and return the result
     */
    @SuppressWarnings("unchecked")
    public <T> T callFieldGetter(Object obj, Field field) {
//        logger.debug("calling getter on: {} {} ", obj, field.getName());
        logger.trace("{}" , field.getDeclaringClass());
        Method method = ReflectionUtils.findMethod(field.getDeclaringClass(), generateGetterName(field));
        if (method.getReturnType() != Void.TYPE)
            try {
                return (T) method.invoke(obj);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return null;
    }

    /**
     * Call the setter of the supplied object and field with the supplied value
     */
    public <T> void callFieldSetter(Object obj, Field field, T fieldValue) {
        String setterName = generateSetterName(field);
        logger.debug("Calling {}.{}({})", new Object[] { field.getDeclaringClass().getSimpleName(), setterName, fieldValue.getClass().getSimpleName() });
        // here we assume that field's type is assignable from the fieldValue
        Method setter = ReflectionUtils.findMethod(field.getDeclaringClass(), setterName, field.getType());
        try {
            setter.invoke(obj, fieldValue);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static String generateName(String prefix, String name) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static Class<?> getFieldReturnType(AccessibleObject accessibleObject) {
        final Logger log = LoggerFactory.getLogger(ReflectionService.class);

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

    private static Class<?> getType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType collectionType = (ParameterizedType) type;
            return (Class<?>) collectionType.getActualTypeArguments()[0];
        }

        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    public Class<Persistable> getMatchingClassForSimpleName(String name) throws NoSuchBeanDefinitionException, ClassNotFoundException {
        logger.trace("scanning for: {}", name);
        scanForPersistables();
        logger.trace("scanning in: {}", persistableLookup);
        return persistableLookup.get(name);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void scanForPersistables() throws NoSuchBeanDefinitionException, ClassNotFoundException {
        if (persistableLookup != null)
            return;

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Persistable.class));
        String basePackage = "org/tdar/";
        persistableLookup = new HashMap<String, Class<Persistable>>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            String beanClassName = bd.getBeanClassName();
            Class cls = Class.forName(beanClassName);
            logger.trace("{} - {} ", cls.getSimpleName(), cls);
            if (persistableLookup.containsKey(cls.getSimpleName())) {
                throw new TdarRecoverableRuntimeException("There is an error in the JAXB Naming Mapping because of overlap in simple names "
                        + cls.getSimpleName());
            }
            persistableLookup.put(cls.getSimpleName(), cls);
        }

    }


    public static boolean methodOrActionContainsAnnotation(ActionInvocation invocation, Class<? extends Annotation> annotationClass) throws SecurityException, NoSuchMethodException {
        Object action = invocation.getAction();
        ActionProxy proxy = invocation.getProxy();
        String methodName = proxy.getMethod();
        Method method = action.getClass().getMethod(methodName);

        if (methodName == null) {
            methodName = "execute";
        }
        Object class_ = AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationClass);
        Object method_ = AnnotationUtils.findAnnotation(method, annotationClass);
        return (class_ != null || method_ != null);
    }

    
    public static boolean classOrMethodContainsAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        Object class_ = AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationClass);
        Object method_ = AnnotationUtils.findAnnotation(method, annotationClass);
        return (class_ != null || method_ != null);
    }

    public static Class<?>[] scanForAnnotation(Class<? extends Annotation>... annots) throws NoSuchBeanDefinitionException, ClassNotFoundException {
        List<Class<?>> toReturn = new ArrayList<Class<?>>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        for (Class<? extends Annotation> annot : annots) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(annot));
        }
        String basePackage = "org/tdar/";
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            String beanClassName = bd.getBeanClassName();
            Class<?> cls = Class.forName(beanClassName);
            toReturn.add(cls);
        }
        return toReturn.toArray(new Class<?>[0]);
    }




    @SuppressWarnings("unchecked")
    public List<Pair<Field, Class<? extends Persistable>>> findAllPersistableFields(Class<?> cls) {
        List<Field> declaredFields = new ArrayList<Field>();
        List<Pair<Field, Class<? extends Persistable>>> result = new ArrayList<Pair<Field, Class<? extends Persistable>>>();
        // iterate up the package hierarchy
        while (cls.getPackage().getName().startsWith("org.tdar.")) {
            CollectionUtils.addAll(declaredFields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }

        for (Field field : declaredFields) {
            Class<? extends Persistable> type = null;
            // generic collections
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || java.lang.reflect.Modifier.isTransient(field.getModifiers())
                    || java.lang.reflect.Modifier.isFinal(field.getModifiers()))
                continue;

            if (Collection.class.isAssignableFrom(field.getType())) {
                ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                if (Persistable.class.isAssignableFrom((Class<? extends Persistable>) stringListType.getActualTypeArguments()[0])) {
                    type = (Class<? extends Persistable>) stringListType.getActualTypeArguments()[0];
                    logger.trace("\t -> {}", type); // class java.lang.String.
                }
            }
            // singletons
            if (Persistable.class.isAssignableFrom(field.getType())) {
                type = (Class<? extends Persistable>) field.getType();
                logger.trace("\t -> {}", type); // class java.lang.String.
            }

            // things to add
            if (type != null) {
                result.add(new Pair<Field,Class<? extends Persistable>>(field, type));
            }
        }
        return result;
    }

}
