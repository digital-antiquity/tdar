package org.tdar.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for resultType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="identifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="summary" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="publisher" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultType", propOrder = {
        "title",
        "identifier",
        "summary",
        "url",
        "publisher"
})
public class ResultType {

    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    protected String identifier;
    @XmlElement(required = true)
    protected String summary;
    @XmlElement(required = true)
    protected String url;
    @XmlElement(required = true)
    protected String publisher;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the summary property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the value of the summary property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSummary(String value) {
        this.summary = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the publisher property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Sets the value of the publisher property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPublisher(String value) {
        this.publisher = value;
    }

}
