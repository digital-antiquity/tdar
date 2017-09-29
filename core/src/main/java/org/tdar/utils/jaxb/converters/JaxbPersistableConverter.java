package org.tdar.utils.jaxb.converters;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.service.AutowireHelper;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ReflectionService;

@Component
public class JaxbPersistableConverter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, Persistable> {

    private static final String NULL_REF = "NULL_REF";

    @Autowired
    GenericService genericService;

    @Autowired
    ReflectionService reflectionService;

    @Override
    public String marshal(Persistable d_) throws Exception {
        Persistable d = d_;
        if (d == null) {
            return NULL_REF;
        }
        String id = "-1";
        if (d.getId() != null) {
            id = d.getId().toString();
        }
        if (HibernateProxy.class.isAssignableFrom(d.getClass())) {
            d = (Persistable) ((HibernateProxy) d).getHibernateLazyInitializer().getImplementation();
        }
        return d.getClass().getSimpleName() + ":" + id;
    }

    @Override
    public Persistable unmarshal(String id) throws Exception {
        if (id.equals(NULL_REF) || id.equals("-1") || StringUtils.isBlank(id) || id.equals(":-1") || id.equals(":")) {
            return null;
        }
        String[] split = id.split(":");
        if (reflectionService == null) {
            AutowireHelper.autowire(this, genericService, reflectionService);
        }
        Class<? extends Persistable> cls = reflectionService.getMatchingClassForSimpleName(split[0]);
        return genericService.find(cls, Long.valueOf(split[1]));
    }
}
