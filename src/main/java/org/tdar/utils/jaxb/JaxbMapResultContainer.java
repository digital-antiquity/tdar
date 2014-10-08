package org.tdar.utils.jaxb;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.utils.jaxb.converters.JaxbMapConverter;

@XmlRootElement(name="resultContainer")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbMapResultContainer implements Serializable {

    private static final long serialVersionUID = -8255546475402578411L;

    @XmlJavaTypeAdapter(JaxbMapConverter.class)
    private Map result;

    public JaxbMapResultContainer() {
    }

    public JaxbMapResultContainer(Map result) {
        this.setResult(result);
    }

    public Map getResult() {
        return result;
    }

    public void setResult(Map result) {
        this.result = result;
    }
}
