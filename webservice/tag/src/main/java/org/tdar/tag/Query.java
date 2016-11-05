package org.tdar.tag;

import java.math.BigDecimal;
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
 *       &lt;all>
 *         &lt;element name="what" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="subjectTerm" type="{http://archaeologydataservice.ac.uk/tag/schema}subjectType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="when" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="minDate" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="maxDate" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="where" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="minLatitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                   &lt;element name="minLongitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                   &lt;element name="maxLatitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                   &lt;element name="maxLongitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="freetext" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

        })
@XmlRootElement(name = "Query")
public class Query {

    @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema")
    protected Query.What what;
    @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema")
    protected Query.When when;
    @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema")
    protected Query.Where where;
    @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema")
    protected String freetext;

    /**
     * Gets the value of the what property.
     * 
     * @return
     *         possible object is {@link Query.What }
     * 
     */
    public Query.What getWhat() {
        return what;
    }

    /**
     * Sets the value of the what property.
     * 
     * @param value
     *            allowed object is {@link Query.What }
     * 
     */
    public void setWhat(Query.What value) {
        this.what = value;
    }

    /**
     * Gets the value of the when property.
     * 
     * @return
     *         possible object is {@link Query.When }
     * 
     */
    public Query.When getWhen() {
        return when;
    }

    /**
     * Sets the value of the when property.
     * 
     * @param value
     *            allowed object is {@link Query.When }
     * 
     */
    public void setWhen(Query.When value) {
        this.when = value;
    }

    /**
     * Gets the value of the where property.
     * 
     * @return
     *         possible object is {@link Query.Where }
     * 
     */
    public Query.Where getWhere() {
        return where;
    }

    /**
     * Sets the value of the where property.
     * 
     * @param value
     *            allowed object is {@link Query.Where }
     * 
     */
    public void setWhere(Query.Where value) {
        this.where = value;
    }

    /**
     * Gets the value of the freetext property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getFreetext() {
        return freetext;
    }

    /**
     * Sets the value of the freetext property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setFreetext(String value) {
        this.freetext = value;
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
     *         &lt;element name="subjectTerm" type="{http://archaeologydataservice.ac.uk/tag/schema}subjectType" maxOccurs="unbounded"/>
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
            "subjectTerm"
    })
    public static class What {

        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true)
        protected List<SubjectType> subjectTerm;

        /**
         * Gets the value of the subjectTerm property.
         * 
         * <p>
         * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present
         * inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the subjectTerm property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * 
         * <pre>
         * getSubjectTerm().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list {@link SubjectType }
         * 
         * 
         */
        public List<SubjectType> getSubjectTerm() {
            if (subjectTerm == null) {
                subjectTerm = new ArrayList<SubjectType>();
            }
            return this.subjectTerm;
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
     *         &lt;element name="minDate" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="maxDate" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
            "minDate",
            "maxDate"
    })
    public static class When {

        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema")
        protected int minDate;
        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema")
        protected int maxDate;

        /**
         * Gets the value of the minDate property.
         * 
         */
        public int getMinDate() {
            return minDate;
        }

        /**
         * Sets the value of the minDate property.
         * 
         */
        public void setMinDate(int value) {
            this.minDate = value;
        }

        /**
         * Gets the value of the maxDate property.
         * 
         */
        public int getMaxDate() {
            return maxDate;
        }

        /**
         * Sets the value of the maxDate property.
         * 
         */
        public void setMaxDate(int value) {
            this.maxDate = value;
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
     *         &lt;element name="minLatitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *         &lt;element name="minLongitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *         &lt;element name="maxLatitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
     *         &lt;element name="maxLongitude" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
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
            "minLatitude",
            "minLongitude",
            "maxLatitude",
            "maxLongitude"
    })
    public static class Where {

        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true, defaultValue = "0")
        protected BigDecimal minLatitude;
        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true, defaultValue = "0")
        protected BigDecimal minLongitude;
        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true, defaultValue = "0")
        protected BigDecimal maxLatitude;
        @XmlElement(namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true, defaultValue = "0")
        protected BigDecimal maxLongitude;

        /**
         * Gets the value of the minLatitude property.
         * 
         * @return
         *         possible object is {@link BigDecimal }
         * 
         */
        public BigDecimal getMinLatitude() {
            return minLatitude;
        }

        /**
         * Sets the value of the minLatitude property.
         * 
         * @param value
         *            allowed object is {@link BigDecimal }
         * 
         */
        public void setMinLatitude(BigDecimal value) {
            this.minLatitude = value;
        }

        /**
         * Gets the value of the minLongitude property.
         * 
         * @return
         *         possible object is {@link BigDecimal }
         * 
         */
        public BigDecimal getMinLongitude() {
            return minLongitude;
        }

        /**
         * Sets the value of the minLongitude property.
         * 
         * @param value
         *            allowed object is {@link BigDecimal }
         * 
         */
        public void setMinLongitude(BigDecimal value) {
            this.minLongitude = value;
        }

        /**
         * Gets the value of the maxLatitude property.
         * 
         * @return
         *         possible object is {@link BigDecimal }
         * 
         */
        public BigDecimal getMaxLatitude() {
            return maxLatitude;
        }

        /**
         * Sets the value of the maxLatitude property.
         * 
         * @param value
         *            allowed object is {@link BigDecimal }
         * 
         */
        public void setMaxLatitude(BigDecimal value) {
            this.maxLatitude = value;
        }

        /**
         * Gets the value of the maxLongitude property.
         * 
         * @return
         *         possible object is {@link BigDecimal }
         * 
         */
        public BigDecimal getMaxLongitude() {
            return maxLongitude;
        }

        /**
         * Sets the value of the maxLongitude property.
         * 
         * @param value
         *            allowed object is {@link BigDecimal }
         * 
         */
        public void setMaxLongitude(BigDecimal value) {
            this.maxLongitude = value;
        }

    }

}
