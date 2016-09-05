package org.tdar.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for GetTopRecordsResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetTopRecordsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://archaeologydataservice.ac.uk/tag/schema}SearchResults"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTopRecordsResponse", propOrder = {
        "searchResults"
})
public class GetTopRecordsResponse {

    @XmlElement(name = "SearchResults", namespace = "http://archaeologydataservice.ac.uk/tag/schema", required = true)
    protected SearchResults searchResults;

    /**
     * Gets the value of the searchResults property.
     * 
     * @return
     *         possible object is {@link SearchResults }
     * 
     */
    public SearchResults getSearchResults() {
        return searchResults;
    }

    /**
     * Sets the value of the searchResults property.
     * 
     * @param value
     *            allowed object is {@link SearchResults }
     * 
     */
    public void setSearchResults(SearchResults value) {
        this.searchResults = value;
    }

}
