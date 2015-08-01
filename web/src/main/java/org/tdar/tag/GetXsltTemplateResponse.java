package org.tdar.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for GetXsltTemplateResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetXsltTemplateResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetXsltTemplateResponse", propOrder = {
        "any"
})
public class GetXsltTemplateResponse {

    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the any property.
     * 
     * @return
     *         possible object is {@link Object }
     * 
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *            allowed object is {@link Object }
     * 
     */
    public void setAny(Object value) {
        this.any = value;
    }

}
