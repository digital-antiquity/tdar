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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.bulk.CellMetadata;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

/**
 * Service to help with Reflection
 * 
 * @author Adam Brin
 * 
 */
@Service
public class ReflectionService {

    private static final String EXECUTE = "execute";
    private static final String ORG_TDAR2 = "org/tdar/";
    private static final String SET = "set";
    private static final String GET = "get";
    private static final String ORG_TDAR = "org.tdar.";
    private transient Logger logger = LoggerFactory.getLogger(getClass());
    private static transient Logger staticLogger = LoggerFactory.getLogger(ReflectionService.class);
    private Map<String, Class<Persistable>> persistableLookup;

    /**
     * This method looks at a class like "Resource" and finds fields that contain the "classToFind",
     * e.g. GeographicKeyword. This would return [geographicKeywords,managedGeographicKeywords]
     * 
     * @param classToInspect
     * @param classToFind
     * @return
     */
    public Set<Field> findFieldsReferencingClass(Class<?> classToInspect, Class<?> classToFind) {
        Set<Field> matchingFields = new HashSet<>();
        for (Field field : classToInspect.getDeclaredFields()) {
            if (getFieldReturnType(field).equals(classToFind)) {
                matchingFields.add(field);
            }
        }
        logger.debug("Found Fields:{} on {}", matchingFields, classToInspect.getSimpleName());
        return matchingFields;
    }

    /**
     * Find all fields with a return-type of the specified class
     * 
     * @param classToInspect
     * @param ancestorToFind
     * @return
     */
    public Set<Field> findAssignableFieldsRefererencingClass(Class<?> classToInspect, Class<?> ancestorToFind) {
        Set<Field> matchingFields = new HashSet<>();
        for (Field field : classToInspect.getDeclaredFields()) {
            if (getFieldReturnType(field).isAssignableFrom(ancestorToFind)) {
                matchingFields.add(field);
            }
        }
        logger.debug("Fields in {} that refer to {}:{}", new Object[] { classToInspect.getSimpleName(), ancestorToFind.getSimpleName(), matchingFields });
        return matchingFields;
    }

    /**
     * Find Fields with any of the annotations specified within the source tree
     * 
     * @param targetClass
     * @param list
     * @param recursive
     * @return
     */
    public Set<Field> findFieldsWithAnnotation(Class<?> targetClass, List<Class<? extends Annotation>> list, boolean recursive) {
        Set<Field> set = new HashSet<>();
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

    /**
     * Based on the field and the object passed in, call the getter and return the result
     * 
     * @param obj
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T callFieldGetter(Object obj, Field field) {
        // logger.debug("calling getter on: {} {} ", obj, field.getName());
        logger.trace("{}", field.getDeclaringClass());
        Method method = ReflectionUtils.findMethod(field.getDeclaringClass(), generateGetterName(field));
        if (method.getReturnType() != Void.TYPE)
            try {
                return (T) method.invoke(obj);
            } catch (Exception e) {
                logger.debug("cannot call field getter for field: {}", field, e);
            }
        return null;
    }

    /**
     * Call the setter of the supplied object and field with the supplied value
     * 
     * @param obj
     * @param field
     * @param fieldValue
     */
    public <T> void callFieldSetter(Object obj, Field field, T fieldValue) {
        String setterName = generateSetterName(field);
        String valClass = "null";
        if (fieldValue != null) {
            valClass = fieldValue.getClass().getSimpleName();
        }
        logger.debug("Calling {}.{}({})", new Object[] { field.getDeclaringClass().getSimpleName(), setterName, valClass });
        // here we assume that field's type is assignable from the fieldValue
        Method setter = ReflectionUtils.findMethod(field.getDeclaringClass(), setterName, field.getType());
        try {
            setter.invoke(obj, fieldValue);
        } catch (Exception e) {
            logger.debug("cannot call field setter:", e);
        }

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

    /**
     * Get the Class of the return type/or generic type
     * 
     * @param type
     * @return
     */
    private static Class<?> getType(Type type) {
        Logger logger = LoggerFactory.getLogger(ReflectionService.class);

        if (WildcardType.class.isAssignableFrom(type.getClass())) {
            WildcardType subType = (WildcardType)type;
            logger.trace(" wildcard type: {} [{}]", type, type.getClass() );
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
     * Find Classes within the org/tdar/core/bean tree that support @link Persistable and resolve the SimpleName with the specified String
     * 
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     * @throws ClassNotFoundException
     */
    public Class<Persistable> getMatchingClassForSimpleName(String name) throws ClassNotFoundException {
        logger.trace("scanning for: {}", name);
        scanForPersistables();
        logger.trace("scanning in: {}", persistableLookup);
        return persistableLookup.get(name);
    }

    /**
     * Scan the class tree to find all objects that implement @link Persistable
     * 
     * @throws NoSuchBeanDefinitionException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void scanForPersistables() throws ClassNotFoundException {
        if (persistableLookup != null)
            return;

        Set<BeanDefinition> findCandidateComponents = findClassesThatImplement(Persistable.class);
        persistableLookup = new HashMap<>();
        for (BeanDefinition bd : findCandidateComponents) {
            String beanClassName = bd.getBeanClassName();
            Class cls = Class.forName(beanClassName);
            logger.trace("{} - {} ", cls.getSimpleName(), cls);
            if (persistableLookup.containsKey(cls.getSimpleName())) {
                throw new TdarRecoverableRuntimeException("reflectionService.jaxb_mapping", Arrays.asList(cls.getSimpleName()));
            }
            persistableLookup.put(cls.getSimpleName(), cls);
        }

    }

    /**
     * Find all classes that implement the identified Class
     * 
     * @param cls
     * @return
     */
    public static Set<BeanDefinition> findClassesThatImplement(Class<?> cls) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(cls));
        String basePackage = ORG_TDAR2;
        Set<BeanDefinition> findCandidateComponents = scanner.findCandidateComponents(basePackage);
        return findCandidateComponents;
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
        staticLogger.trace("key: {}, found: {}", key,found);
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
            found = (class_ != null || method_ != null);
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
        List<Class<?>> toReturn = new ArrayList<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        for (Class<? extends Annotation> annot : annots) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(annot));
        }
        String basePackage = ORG_TDAR2;
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            String beanClassName = bd.getBeanClassName();
            Class<?> cls = Class.forName(beanClassName);
            toReturn.add(cls);
        }
        return toReturn.toArray(new Class<?>[0]);
    }

    /**
     * Find all beans that implment the @link Persistable interface
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Pair<Field, Class<? extends Persistable>>> findAllPersistableFields(Class<?> cls) {
        List<Field> declaredFields = new ArrayList<>();
        List<Pair<Field, Class<? extends Persistable>>> result = new ArrayList<>();
        // iterate up the package hierarchy
        while (cls.getPackage().getName().startsWith(ORG_TDAR)) {
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
                result.add(new Pair<Field, Class<? extends Persistable>>(field, type));
            }
        }
        return result;
    }

    /**
     * @see #findBulkAnnotationsOnClass(Class, Stack, String)
     * 
     * @param class2
     * @return
     */
    public LinkedHashSet<CellMetadata> findBulkAnnotationsOnClass(Class<?> class2) {
        Stack<List<Class<?>>> classStack = new Stack<>();
        return findBulkAnnotationsOnClass(class2, classStack, "");
    }

    /**
     * Find all @link BulkImportField annotations on all resource classes.
     * 
     * @param class2
     * @param stack
     * @param prefix
     * @return
     */
    public LinkedHashSet<CellMetadata> findBulkAnnotationsOnClass(Class<?> class2, Stack<List<Class<?>>> stack, String prefix) {
        Class<BulkImportField> annotationToFind = BulkImportField.class;
        LinkedHashSet<CellMetadata> set = new LinkedHashSet<>();
        if (class2.getSuperclass() != Object.class) {
            set.addAll(findBulkAnnotationsOnClass(class2.getSuperclass(), stack, prefix));
        }

        Field runMultiple = null;
        List<Class<?>> runWith = new ArrayList<Class<?>>();
        for (Field field : class2.getDeclaredFields()) {
            BulkImportField annotation = field.getAnnotation(annotationToFind);
            if (annotation != null && ArrayUtils.isNotEmpty(annotation.implementedSubclasses())) {
                runWith.addAll(Arrays.asList(annotation.implementedSubclasses()));
                runMultiple = field;
            }
        }
        List<Class<?>> classList = new ArrayList<Class<?>>();
        stack.add(classList);
        classList.add(class2);

        if (runMultiple == null) {
            set.addAll(handleClassAnnotations(class2, stack, annotationToFind, null, null, prefix));
        } else {
            for (Class<?> runAs : runWith) {
                classList.add(runAs);
                set.addAll(handleClassAnnotations(class2, stack, annotationToFind, runAs, runMultiple, prefix));
                classList.remove(runAs);
            }
        }
        stack.remove(classList);
        return set;
    }

    /**
     * Find @link BulkImportField on a class.
     * 
     * @param class2
     * @param stack
     * @param annotationToFind
     * @param runAs
     * @param runAsField
     * @param prefix
     * @return
     */
    private LinkedHashSet<CellMetadata> handleClassAnnotations(Class<?> class2, Stack<List<Class<?>>> stack, Class<BulkImportField> annotationToFind,
            Class<?> runAs, Field runAsField, String prefix) {
        LinkedHashSet<CellMetadata> set = new LinkedHashSet<>();
        for (Field field : class2.getDeclaredFields()) {
            BulkImportField annotation = field.getAnnotation(annotationToFind);
            if (prefix == null) {
                prefix = "";
            }
            if (annotation != null) {
                String fieldPrefix = prefix;
                if (StringUtils.isNotBlank(annotation.label())) {
                    fieldPrefix = StringUtils.trim(annotation.label());
                    // fieldPrefix = fieldPrefix.trim();
                }

                Class<?> type = field.getType();
                if (ObjectUtils.equals(field, runAsField)) {
                    type = runAs;
                    logger.trace(" ** overriding type with " + type.getSimpleName());
                }

                if (Collection.class.isAssignableFrom(type))
                // handle Collection private List<ResourceCreator> ...
                {
                    ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                    Class<?> cls = (Class<?>) stringListType.getActualTypeArguments()[0];
                    set.addAll(findBulkAnnotationsOnClass(cls, stack, fieldPrefix));
                }
                // handle Singleton private Person owner ...
                else if (Persistable.class.isAssignableFrom(type)) {
                    set.addAll(findBulkAnnotationsOnClass(type, stack, fieldPrefix));
                }
                // handle more primative fields private String ...
                else {
                    logger.trace("adding {} ({})", field, stack);
                    if (!TdarConfiguration.getInstance().getCopyrightMandatory() && ObjectUtils.equals(annotation.label(), BulkImportField.COPYRIGHT_HOLDER)) {
                        continue;
                    }

                    if (TdarConfiguration.getInstance().getLicenseEnabled() == false
                            && (ObjectUtils.equals(field.getName(), "licenseType") || ObjectUtils.equals(field.getName(), "licenseText")))
                        continue;
                    set.add(new CellMetadata(field, annotation, class2, stack, prefix));

                    // set.add(field);
                }

            }
        }
        return set;
    }

    /**
     * Cast the value to the object type of the field and call the setter. Validate the propert in the process.
     * 
     * @param beanToProcess
     * @param name
     * @param value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void validateAndSetProperty(Object beanToProcess, String name, String value) {
        try {
            logger.trace("processing: " + beanToProcess + " - " + name + " --> " + value);
            Class propertyType = PropertyUtils.getPropertyType(beanToProcess, name);

            // handle types should we be testing column length?
            if (propertyType.isEnum()) {
                try {
                    BeanUtils.setProperty(beanToProcess, name, Enum.valueOf(propertyType, value));
                } catch (IllegalArgumentException e) {
                    logger.debug("cannot set property:", e);
                    throw new TdarRecoverableRuntimeException("reflectionService.not_valid_value", e, Arrays.asList(value, name));
                }
            } else {
                if (Integer.class.isAssignableFrom(propertyType)) {
                    try {
                        Double dbl = Double.valueOf(value);
                        if (dbl == Math.floor(dbl)) {
                            value = new Integer((int) Math.floor(dbl)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("reflectionService.expecting_integer", Arrays.asList(value, name));
                    }
                }
                if (Long.class.isAssignableFrom(propertyType)) {
                    try {
                        Double dbl = Double.valueOf(value);
                        if (dbl == Math.floor(dbl)) {
                            value = new Long((long) Math.floor(dbl)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("reflectionService.expecting_big_integer", Arrays.asList(value, name));
                    }
                }
                if (Float.class.isAssignableFrom(propertyType)) {
                    try {
                        Float.parseFloat(value);
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("reflectionService.expecting_floating_point", Arrays.asList(value, name));
                    }
                }
                BeanUtils.setProperty(beanToProcess, name, value);
            }
        } catch (Exception e1) {
            if (e1 instanceof TdarRecoverableRuntimeException) {
                throw (TdarRecoverableRuntimeException) e1;
            }
            logger.debug("error processing bulk upload: {}", e1);
            throw new TdarRecoverableRuntimeException("reflectionService.expecting_floating_generic", Arrays.asList(value, name));
        }
    }


    /**
     * Find all getters of beans that support the @link Obfuscatable interface and any child beans throughout the graph 
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Pair<Method, Class<? extends Obfuscatable>>> findAllObfuscatableGetters(Class<?> cls) {
        List<Method> declaredFields = new ArrayList<>();
        List<Pair<Method, Class<? extends Obfuscatable>>> result = new ArrayList<>();
        // iterate up the package hierarchy
        Class<?> actualClass = null; 
        while (cls.getPackage().getName().startsWith(ORG_TDAR)) {
            // find first implemented tDAR class (actual class);
            if (actualClass == null) {
                actualClass = cls;
            }
            for (Method method : cls.getDeclaredMethods()) {
                
                if (Modifier.isPublic(method.getModifiers()) && method.getName().startsWith(GET)) {
                    declaredFields.add(method);
                }
            }
            cls = cls.getSuperclass();
        }

        for (Method method : declaredFields) {
            Class<? extends Obfuscatable> type = null;
            // generic collections
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) || java.lang.reflect.Modifier.isTransient(method.getModifiers())
                    || java.lang.reflect.Modifier.isFinal(method.getModifiers()))
                continue;

            
            // logger.info("TYPE: {} {} ", method.getGenericReturnType(), method.getName());
            // logger.info("{} ==> {}", actualClass, method.getDeclaringClass());
            // logger.info(" {} {} {} ", dcl.getTypeParameters(), dcl.getGenericInterfaces(), dcl.getGenericSuperclass());
            boolean force = false;
            if (Collection.class.isAssignableFrom(method.getReturnType())) {
                Class<?> type2 = getType(method.getGenericReturnType());
                if (type2 == null) {
                    force = true;
                } else if (Obfuscatable.class.isAssignableFrom((Class<? extends Obfuscatable>) type2)) {
                    type = (Class<? extends Obfuscatable>) type2;
                    logger.trace("\t -> {}", type); // class java.lang.String.
                }
            }
            // singletons
            if (Obfuscatable.class.isAssignableFrom(method.getReturnType())) {
                type = (Class<? extends Obfuscatable>) method.getReturnType();
                logger.trace("\t -> {}", type); // class java.lang.String.
            }

            // things to add
            if (type != null || force) {
                if (force) {
                logger.debug("forcing method to be obfuscated because cannot figure out gneric type {} (good luck)", method);
                }
                result.add(new Pair<Method, Class<? extends Obfuscatable>>(method, type));
            }
        }
        return result;
    }

}
