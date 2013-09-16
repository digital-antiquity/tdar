package org.tdar.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for GetTopRecords complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetTopRecords">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{http://archaeologydataservice.ac.uk/tag/schema}Query"/>
 *         &lt;element name="numberOfRecords" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTopRecords", propOrder = {
        "sessionId",
        "query",
        "numberOfRecords"
})
public class GetTopRecords {

    @XmlElement(required = true)
    protected String sessionId;
    @XmlElement(name = "Query", namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true)
    protected Query query;
    protected int numberOfRecords;

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * @return
     *         possible object is {@link Query }
     * 
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *            allowed object is {@link Query }
     * 
     */
    public void setQuery(Query value) {
        this.query = value;
    }

    /**
     * Gets the value of the numberOfRecords property.
     * 
     */
    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    /**
     * Sets the value of the numberOfRecords property.
     * 
     */
    public void setNumberOfRecords(int value) {
        this.numberOfRecords = value;
    }

}
