package org.tdar.transform;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "meta")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetaTag implements Serializable {

    private static final long serialVersionUID = 3541542426505974287L;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String content;

    public MetaTag() {
    }

    public MetaTag(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
