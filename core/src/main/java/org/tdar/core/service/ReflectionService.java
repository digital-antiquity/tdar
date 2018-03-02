package org.tdar.core.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.utils.Pair;

public interface ReflectionService {

    Set<Field> findFieldsReferencingClass(Class<?> classToInspect, Class<?> classToFind);

    Set<Field> findAssignableFieldsRefererencingClass(Class<?> classToInspect, Class<?> ancestorToFind);

    <T> T callFieldGetter(Object obj, Field field);

    <T> void callFieldSetter(Object obj, Field field, T fieldValue);

    Class<? extends Persistable> getMatchingClassForSimpleName(String name) throws ClassNotFoundException;

    List<Pair<Field, Class<? extends Persistable>>> findAllPersistableFields(Class<?> cls_);

    List<Pair<Method, Class<? extends Obfuscatable>>> findAllObfuscatableGetters(Class<?> cls_);

    Method findMatchingSetter(Method method);

}
