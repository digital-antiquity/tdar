package org.tdar.core.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.Pair;

/**
 * Service to help with Reflection
 * 
 * @author Adam Brin
 */
@Service
public class ReflectionServiceImpl implements ReflectionService {

    private static final String GET = "get";
    private static final String ORG_TDAR = "org.tdar.";
    private transient Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, Class<? extends Persistable>> persistableLookup;

    @Autowired
    private GenericDao genericDao;

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
            if (ReflectionHelper.getFieldReturnType(field).equals(classToFind)) {
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
            if (ReflectionHelper.getFieldReturnType(field).isAssignableFrom(ancestorToFind)) {
                matchingFields.add(field);
            }
        }
        logger.debug("Fields in {} that refer to {}:{}", new Object[] { classToInspect.getSimpleName(), ancestorToFind.getSimpleName(), matchingFields });
        return matchingFields;
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
        Method method = ReflectionUtils.findMethod(field.getDeclaringClass(), ReflectionHelper.generateGetterName(field));
        if (method == null) {
            return null;
        }
        if (method.getReturnType() != Void.TYPE) {
            try {
                return (T) method.invoke(obj);
            } catch (Exception e) {
                logger.debug("cannot call field getter for field: {}", field, e);
            }
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
        String setterName = ReflectionHelper.generateSetterName(field);
        String valClass = "null";
        if (fieldValue != null) {
            valClass = fieldValue.getClass().getSimpleName();
        }
        logger.trace("Calling {}.{}({})", new Object[] { field.getDeclaringClass().getSimpleName(), setterName, valClass });
        // here we assume that field's type is assignable from the fieldValue
        Method setter = ReflectionUtils.findMethod(field.getDeclaringClass(), setterName, field.getType());
        try {
            setter.invoke(obj, fieldValue);
        } catch (Exception e) {
            logger.debug("cannot call field setter {} on : {} {}  {}", field, obj, fieldValue, e);
        }

    }

    /**
     * Find Classes within the org/tdar/core/bean tree that support @link Persistable and resolve the SimpleName with the specified String
     * 
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     * @throws ClassNotFoundException
     */
    public Class<? extends Persistable> getMatchingClassForSimpleName(String name) throws ClassNotFoundException {
        logger.trace("scanning for: {}", name);
        scanForPersistables();
//        logger.trace("scanning for {} in: {}", name, persistableLookup);
        return persistableLookup.get(name);
    }

    /**
     * Scan the class tree to find all objects that implement @link Persistable
     * 
     * @throws NoSuchBeanDefinitionException
     * @throws ClassNotFoundException
     */
    private void scanForPersistables() throws ClassNotFoundException {
        if (persistableLookup != null) {
            return;
        }

        Set<Class<? extends Persistable>> findCandidateComponents = ReflectionHelper.findClassesThatImplement(Persistable.class);
        persistableLookup = new HashMap<>();
        for (Class<? extends Persistable> cls : findCandidateComponents) {
            logger.trace("{} - {} ", cls.getSimpleName(), cls);
            if (StringUtils.isBlank(cls.getSimpleName()) || StringUtils.equalsIgnoreCase("base", cls.getSimpleName())) {
                continue;
            }
            if (persistableLookup.containsKey(cls.getSimpleName())) {
                throw new TdarRecoverableRuntimeException("reflectionService.jaxb_mapping", Arrays.asList(cls.getSimpleName()));
            }
            persistableLookup.put(cls.getSimpleName(), cls);
        }

    }

    @SuppressWarnings("unused")
    private static final ClassLoader classLoader = Document.class.getClassLoader();


    /**
     * Find all beans that implment the @link Persistable interface
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Pair<Field, Class<? extends Persistable>>> findAllPersistableFields(Class<?> cls_) {
        List<Field> declaredFields = new ArrayList<>();
        List<Pair<Field, Class<? extends Persistable>>> result = new ArrayList<>();
        // iterate up the package hierarchy
        Class<?> cls = cls_;
        while (cls.getPackage().getName().startsWith(ORG_TDAR)) {
            CollectionUtils.addAll(declaredFields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }

        for (Field field : declaredFields) {
            Class<? extends Persistable> type = null;
            // generic collections
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || java.lang.reflect.Modifier.isTransient(field.getModifiers())
                    || java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }

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
     * Cast the value to the object type of the field and call the setter. Validate the propert in the process.
     * 
     * @param beanToProcess
     * @param name
     * @param value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void validateAndSetProperty(Object beanToProcess, String name, String value) {
        List<String> errorValueList = Arrays.asList(name, value);
        try {
            logger.trace("processing: {} - {} --> {}", beanToProcess, name, value);
            Class propertyType = PropertyUtils.getPropertyType(beanToProcess, name);

            // handle types should we be testing column length?
            if (propertyType.isEnum()) {
                try {
                    BeanUtils.setProperty(beanToProcess, name, Enum.valueOf(propertyType, value));
                } catch (IllegalArgumentException e) {
                    logger.debug("cannot set property:", e);
                    throw new TdarRecoverableRuntimeException("reflectionService.not_valid_value", e, errorValueList);
                }
            } else {
                String value_ = value;
                if (Integer.class.isAssignableFrom(propertyType)) {
                    try {
                        Double dbl = Double.valueOf(value);
                        if (dbl == Math.floor(dbl)) {
                            value_ = new Integer((int) Math.floor(dbl)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("reflectionService.expecting_integer", errorValueList);
                    }
                }
                if (Long.class.isAssignableFrom(propertyType)) {
                    try {
                        Double dbl = Double.valueOf(value);
                        if (dbl == Math.floor(dbl)) {
                            value_ = new Long((long) Math.floor(dbl)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("reflectionService.expecting_big_integer", errorValueList);
                    }
                }
                if (Float.class.isAssignableFrom(propertyType)) {
                    try {
                        Float.parseFloat(value);
                        Float flt = Float.valueOf(value);
                        if (flt == Math.floor(flt)) {
                            value_ = new Long((long) Math.floor(flt)).toString();
                        }
                    } catch (NumberFormatException nfe) {
                        throw new TdarRecoverableRuntimeException("reflectionService.expecting_floating_point", errorValueList);
                    }
                }
                BeanUtils.setProperty(beanToProcess, name, value_);
            }
        } catch (Exception e1) {
            if (e1 instanceof TdarRecoverableRuntimeException) {
                throw (TdarRecoverableRuntimeException) e1;
            }
            logger.debug("error processing bulk upload: {}", e1);
            throw new TdarRecoverableRuntimeException("reflectionService.expecting_generic", errorValueList);
        }
    }

    /**
     * Find all getters of beans that support the @link Obfuscatable interface and any child beans throughout the graph
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Pair<Method, Class<? extends Obfuscatable>>> findAllObfuscatableGetters(Class<?> cls_) {
        List<Method> declaredFields = new ArrayList<>();
        List<Pair<Method, Class<? extends Obfuscatable>>> result = new ArrayList<>();
        // iterate up the package hierarchy
        Class<?> actualClass = null;
        Class<?> cls = cls_;
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
                    || java.lang.reflect.Modifier.isFinal(method.getModifiers())) {
                continue;
            }

            // logger.info("TYPE: {} {} ", method.getGenericReturnType(), method.getName());
            // logger.info("{} ==> {}", actualClass, method.getDeclaringClass());
            // logger.info(" {} {} {} ", dcl.getTypeParameters(), dcl.getGenericInterfaces(), dcl.getGenericSuperclass());
            boolean force = false;
            if (Collection.class.isAssignableFrom(method.getReturnType())) {
                Class<?> type2 = ReflectionHelper.getType(method.getGenericReturnType());
                if (type2 == null) {
                    force = true;
                } else if (Obfuscatable.class.isAssignableFrom(type2)) {
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
            if ((type != null) || force) {
                if (force) {
                    logger.trace("forcing method to be obfuscated because cannot figure out gneric type {} (good luck)", method);
                }
                result.add(new Pair<Method, Class<? extends Obfuscatable>>(method, type));
            }
        }
        return result;
    }

    public static List<Field> findAnnotatedFieldsOfClass(Class<?> cls_, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        // iterate up the package hierarchy
        Class<?> actualClass = null;
        Class<?> cls = cls_;
        while (cls.getPackage().getName().startsWith(ORG_TDAR)) {
            // find first implemented tDAR class (actual class);
            if (actualClass == null) {
                actualClass = cls;
            }
            for (Field field : cls.getDeclaredFields()) {
                Object annotation = field.getAnnotation(annotationClass);
                if (annotation != null) {
                    result.add(field);
                }
            }
            cls = cls.getSuperclass();
        }
        return result;
    }

    public void walkObject(Persistable p) {
        logger.debug("{} {}", p.getClass().getCanonicalName(), p);
        Set<String> seen = new HashSet<>();
        walkObject(p, 0, seen);
    }

    private String makeKey(Persistable p) {
        return String.format("%s-%s", p.getClass().getSimpleName(), p.getId());
    }

    private void walkObject(Persistable p, int indent, Set<String> seen) {
        List<Pair<Field, Class<? extends Persistable>>> findAllPersistableFields = findAllPersistableFields(p.getClass());
        String key = makeKey(p);
        if (seen.contains(key)) {
            logger.debug("{}[{}] {}", StringUtils.repeat("| ", indent + 1), "seen", p);
            return;
        }
        seen.add(key);
        for (Pair<Field, Class<? extends Persistable>> pair : findAllPersistableFields) {
            Object content = callFieldGetter(p, pair.getFirst());
            if (content == null) {
                logger.trace("{}{}", StringUtils.repeat("| ", indent + 1), pair.getFirst());
                continue;
            }
            logger.trace("{}, {}", content, pair.getFirst());
            if (Collection.class.isAssignableFrom(content.getClass())) {
                @SuppressWarnings("unchecked")
                Collection<Persistable> originalList = (Collection<Persistable>) content;
                Collection<Persistable> contents = new ArrayList<Persistable>(originalList);
                // using a separate collection to avoid concurrent modification of bi-directional double-lists
                if (CollectionUtils.isNotEmpty(contents)) {
                    logger.debug("{}{}", StringUtils.repeat("| ", indent + 1), pair.getFirst().getName());
                }
                Iterator<Persistable> iterator = contents.iterator();
                while (iterator.hasNext()) {
                    Persistable p_ = iterator.next();
                    boolean sessionContains = genericDao.sessionContains(p_);
                    logger.debug("{}[{}] {}", StringUtils.repeat("| ", indent + 2), sessionContains, p_);
                    if (sessionContains) {
                        walkObject(p_, indent + 2, seen);
                    }
                }
            } else {
                boolean sessionContains = genericDao.sessionContains(content);
                logger.debug("{}[{}] {} {}", StringUtils.repeat("| ", indent + 1), sessionContains, pair.getFirst().getName(), content);
                if (sessionContains) {
                    walkObject((Persistable) content, indent + 1, seen);
                }
            }
        }
    }

    public Method findMatchingSetter(Method method) {
        String name = "set" + method.getName().substring(3);
        return ReflectionUtils.findMethod(method.getDeclaringClass(), name, method.getReturnType());
    }

    public Field getFieldForGetterOrSetter(Method method) {
        String name = ReflectionHelper.cleanupMethodName(method);
        Field field = ReflectionUtils.findField(method.getDeclaringClass(), name);
        return field;
    }

}
