package org.tdar.tag;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meta">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="sessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="providerName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="totalRecords" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="recordsReturned" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="firstRecord" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="lastRecord" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="results">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="containsIntegratableData" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                   &lt;element name="integratableDatasetUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="result" type="{http://archaeologydataservice.ac.uk/tag/schema}resultType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "meta",
        "results"
})
@XmlRootElement(name = "SearchResults")
public class SearchResults {

    @XmlElement(required = true)
    protected SearchResults.Meta meta;
    @XmlElement(required = true)
    protected SearchResults.Results results;

    /**
     * Gets the value of the meta property.
     * 
     * @return
     *         possible object is {@link SearchResults.Meta }
     * 
     */
    public SearchResults.Meta getMeta() {
        return meta;
    }

    /**
     * Sets the value of the meta property.
     * 
     * @param value
     *            allowed object is {@link SearchResults.Meta }
     * 
     */
    public void setMeta(SearchResults.Meta value) {
        this.meta = value;
    }

    /**
     * Gets the value of the results property.
     * 
     * @return
     *         possible object is {@link SearchResults.Results }
     * 
     */
    public SearchResults.Results getResults() {
        return results;
    }

    /**
     * Sets the value of the results property.
     * 
     * @param value
     *            allowed object is {@link SearchResults.Results }
     * 
     */
    public void setResults(SearchResults.Results value) {
        this.results = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="sessionID" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="providerName" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="totalRecords" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="recordsReturned" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="firstRecord" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="lastRecord" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "sessionID",
            "providerName",
            "totalRecords",
            "recordsReturned",
            "firstRecord",
            "lastRecord"
    })
    public static class Meta {

        @XmlElement(required = true)
        protected String sessionID;
        @XmlElement(required = true)
        protected String providerName;
        protected int totalRecords;
        protected int recordsReturned;
        protected int firstRecord;
        protected int lastRecord;

        /**
         * Gets the value of the sessionID property.
         * 
         * @return
         *         possible object is {@link String }
         * 
         */
        public String getSessionID() {
            return sessionID;
        }

        /**
         * Sets the value of the sessionID property.
         * 
         * @param value
         *            allowed object is {@link String }
         * 
         */
        public void setSessionID(String value) {
            this.sessionID = value;
        }

        /**
         * Gets the value of the providerName property.
         * 
         * @return
         *         possible object is {@link String }
         * 
         */
        public String getProviderName() {
            return providerName;
        }

        /**
         * Sets the value of the providerName property.
         * 
         * @param value
         *            allowed object is {@link String }
         * 
         */
        public void setProviderName(String value) {
            this.providerName = value;
        }

        /**
         * Gets the value of the totalRecords property.
         * 
         */
        public int getTotalRecords() {
            return totalRecords;
        }

        /**
         * Sets the value of the totalRecords property.
         * 
         */
        public void setTotalRecords(int value) {
            this.totalRecords = value;
        }

        /**
         * Gets the value of the recordsReturned property.
         * 
         */
        public int getRecordsReturned() {
            return recordsReturned;
        }

        /**
         * Sets the value of the recordsReturned property.
         * 
         */
        public void setRecordsReturned(int value) {
            this.recordsReturned = value;
        }

        /**
         * Gets the value of the firstRecord property.
         * 
         */
        public int getFirstRecord() {
            return firstRecord;
        }

        /**
         * Sets the value of the firstRecord property.
         * 
         */
        public void setFirstRecord(int value) {
            this.firstRecord = value;
        }

        /**
         * Gets the value of the lastRecord property.
         * 
         */
        public int getLastRecord() {
            return lastRecord;
        }

        /**
         * Sets the value of the lastRecord property.
         * 
         */
        public void setLastRecord(int value) {
            this.lastRecord = value;
        }

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="containsIntegratableData" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *         &lt;element name="integratableDatasetUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="result" type="{http://archaeologydataservice.ac.uk/tag/schema}resultType" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "containsIntegratableData",
            "integratableDatasetUrl",
            "result"
    })
    public static class Results {

        protected boolean containsIntegratableData;
        @XmlElement(required = true)
        protected String integratableDatasetUrl;
        @XmlElement(required = true)
        protected List<ResultType> result;

        /**
         * Gets the value of the containsIntegratableData property.
         * 
         */
        public boolean isContainsIntegratableData() {
            return containsIntegratableData;
        }

        /**
         * Sets the value of the containsIntegratableData property.
         * 
         */
        public void setContainsIntegratableData(boolean value) {
            this.containsIntegratableData = value;
        }

        /**
         * Gets the value of the integratableDatasetUrl property.
         * 
         * @return
         *         possible object is {@link String }
         * 
         */
        public String getIntegratableDatasetUrl() {
            return integratableDatasetUrl;
        }

        /**
         * Sets the value of the integratableDatasetUrl property.
         * 
         * @param value
         *            allowed object is {@link String }
         * 
         */
        public void setIntegratableDatasetUrl(String value) {
            this.integratableDatasetUrl = value;
        }

        /**
         * Gets the value of the result property.
         * 
         * <p>
         * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present
         * inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the result property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * 
         * <pre>
         * getResult().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list {@link ResultType }
         * 
         * 
         */
        public List<ResultType> getResult() {
            if (result == null) {
                result = new ArrayList<ResultType>();
            }
            return this.result;
        }

    }

}
