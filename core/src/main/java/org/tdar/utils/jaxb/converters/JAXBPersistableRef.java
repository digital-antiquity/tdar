package org.tdar.utils.jaxb.converters;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.Persistable;

@XmlRootElement
public class JAXBPersistableRef implements Persistable {

    private static final long serialVersionUID = -7534044335917732141L;
    private String type;
    private Long id;

    public JAXBPersistableRef() {
    }

    public JAXBPersistableRef(Long id, Class<? extends Persistable> cls) {
        this.id = id;
        if (cls != null) {
            this.type = cls.getSimpleName();
        }
    }

    @XmlAttribute
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList("id");
    }
}
