package org.tdar.experimental;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Resource;

public class HibernateAnnotationTest {
    protected final static transient Logger logger = LoggerFactory.getLogger(Resource.class);

    private class ClassToAnnotationMap extends HashMap<Class<?>, Annotation> {
    };

    @Test
    public void testGetFields() {
        Class<Resource> type = Resource.class;
        List<Field> fields = new ArrayList<Field>(Arrays.asList(type.getDeclaredFields()));
        logger.debug("here we go, fieldcount:{}", type.getDeclaredFields().length);
        for (Field field : fields) {
            Set<Class<? extends Annotation>> set = new HashSet<Class<? extends Annotation>>();
            // logger.debug("Field:{}", field);
            Annotation[] annotations = field.getAnnotations();
            for (Annotation ann : annotations) {
                set.add(ann.annotationType());
            }
            if (set.contains(ManyToMany.class) && set.contains(JoinTable.class)) {
                logger.debug("This field has a many to many relationship w/ a jointable:{}", field);
            }
        }
    }

    @Test
    // collect all the fields that annotate many to many relationship (@ManyToMany *and* @JoinTable)
    public void testGetManyToManyFields() {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : Resource.class.getDeclaredFields()) {
            Map<Class<?>, Annotation> map = getMap(field);
            if (map.containsKey(ManyToMany.class) && map.containsKey(JoinTable.class)) {
                logger.debug("Field:{} m2m:{}  jointable:{}", new Object[] { field, map.get(ManyToMany.class), map.get(JoinTable.class) });
                fields.add(field);
            }
        }
    }

    private Map<Class<?>, Annotation> getMap(Field field) {
        Map<Class<?>, Annotation> map = new ClassToAnnotationMap();

        for (Annotation ann : field.getAnnotations()) {
            map.put(ann.annotationType(), ann);
        }
        return map;
    }

}
