package org.tdar.utils.jaxb.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ReflectionService;

public class JaxbPersistableConverter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, Persistable> {

    private static final String NULL_REF = "NULL_REF";

    @Autowired
    GenericService genericService;

    @Autowired
    ReflectionService reflectionService;

    @Override
    public String marshal(Persistable d) throws Exception {
        if (d == null) {
            return NULL_REF;
        }
        String id = "-1";
        if (d.getId() != null) {
            id = d.getId().toString();
        }
        return d.getClass().getSimpleName() + ":" + id;
    }

    @Override
    public Persistable unmarshal(String id) throws Exception {
        String[] split = id.split(":");
        Class<Persistable> cls = reflectionService.getMatchingClassForSimpleName(split[0]);
        return (Persistable) genericService.find(cls, Long.valueOf(split[1]));
    }
}
