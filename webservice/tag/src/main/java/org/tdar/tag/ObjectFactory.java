package org.tdar.tag;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.tdar.tag.Query.What;
import org.tdar.tag.Query.When;
import org.tdar.tag.Query.Where;
import org.tdar.tag.SearchResults.Meta;
import org.tdar.tag.SearchResults.Results;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.tdar.tag package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. The Java representation of XML content can
 * consist of schema derived interfaces and classes representing the binding of schema type definitions, element declarations and model groups. Factory methods
 * for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetXsltTemplate_QNAME = new QName("http://archaeologydataservice.ac.uk/tag/schema", "GetXsltTemplate");
    private final static QName _GetVersion_QNAME = new QName("http://archaeologydataservice.ac.uk/tag/schema", "GetVersion");
    private final static QName _GetVersionResponse_QNAME = new QName("http://archaeologydataservice.ac.uk/tag/schema", "GetVersionResponse");
    private final static QName _GetXsltTemplateResponse_QNAME = new QName("http://archaeologydataservice.ac.uk/tag/schema", "GetXsltTemplateResponse");
    private final static QName _GetTopRecords_QNAME = new QName("http://archaeologydataservice.ac.uk/tag/schema", "GetTopRecords");
    private final static QName _GetTopRecordsResponse_QNAME = new QName("http://archaeologydataservice.ac.uk/tag/schema", "GetTopRecordsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tdar.tag
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetVersion }
     * 
     */
    public GetVersion createGetVersion() {
        return new GetVersion();
    }

    /**
     * Create an instance of {@link Query.When }
     * 
     */
    public Query.When createQueryWhen() {
        return new Query.When();
    }

    /**
     * Create an instance of {@link GetXsltTemplate }
     * 
     */
    public GetXsltTemplate createGetXsltTemplate() {
        return new GetXsltTemplate();
    }

    /**
     * Create an instance of {@link ResultType }
     * 
     */
    public ResultType createResultType() {
        return new ResultType();
    }

    /**
     * Create an instance of {@link GetXsltTemplateResponse }
     * 
     */
    public GetXsltTemplateResponse createGetXsltTemplateResponse() {
        return new GetXsltTemplateResponse();
    }

    /**
     * Create an instance of {@link Query.What }
     * 
     */
    public Query.What createQueryWhat() {
        return new Query.What();
    }

    /**
     * Create an instance of {@link GetVersionResponse }
     * 
     */
    public GetVersionResponse createGetVersionResponse() {
        return new GetVersionResponse();
    }

    /**
     * Create an instance of {@link SearchResults.Meta }
     * 
     */
    public SearchResults.Meta createSearchResultsMeta() {
        return new SearchResults.Meta();
    }

    /**
     * Create an instance of {@link GetTopRecords }
     * 
     */
    public GetTopRecords createGetTopRecords() {
        return new GetTopRecords();
    }

    /**
     * Create an instance of {@link GetTopRecordsResponse }
     * 
     */
    public GetTopRecordsResponse createGetTopRecordsResponse() {
        return new GetTopRecordsResponse();
    }

    /**
     * Create an instance of {@link SearchResults.Results }
     * 
     */
    public SearchResults.Results createSearchResultsResults() {
        return new SearchResults.Results();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link Query.Where }
     * 
     */
    public Query.Where createQueryWhere() {
        return new Query.Where();
    }

    /**
     * Create an instance of {@link SearchResults }
     * 
     */
    public SearchResults createSearchResults() {
        return new SearchResults();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetXsltTemplate }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://archaeologydataservice.ac.uk/tag/schema", name = "GetXsltTemplate")
    public JAXBElement<GetXsltTemplate> createGetXsltTemplate(GetXsltTemplate value) {
        return new JAXBElement<GetXsltTemplate>(_GetXsltTemplate_QNAME, GetXsltTemplate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVersion }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://archaeologydataservice.ac.uk/tag/schema", name = "GetVersion")
    public JAXBElement<GetVersion> createGetVersion(GetVersion value) {
        return new JAXBElement<GetVersion>(_GetVersion_QNAME, GetVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVersionResponse }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://archaeologydataservice.ac.uk/tag/schema", name = "GetVersionResponse")
    public JAXBElement<GetVersionResponse> createGetVersionResponse(GetVersionResponse value) {
        return new JAXBElement<GetVersionResponse>(_GetVersionResponse_QNAME, GetVersionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetXsltTemplateResponse }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://archaeologydataservice.ac.uk/tag/schema", name = "GetXsltTemplateResponse")
    public JAXBElement<GetXsltTemplateResponse> createGetXsltTemplateResponse(GetXsltTemplateResponse value) {
        return new JAXBElement<GetXsltTemplateResponse>(_GetXsltTemplateResponse_QNAME, GetXsltTemplateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTopRecords }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://archaeologydataservice.ac.uk/tag/schema", name = "GetTopRecords")
    public JAXBElement<GetTopRecords> createGetTopRecords(GetTopRecords value) {
        return new JAXBElement<GetTopRecords>(_GetTopRecords_QNAME, GetTopRecords.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTopRecordsResponse }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://archaeologydataservice.ac.uk/tag/schema", name = "GetTopRecordsResponse")
    public JAXBElement<GetTopRecordsResponse> createGetTopRecordsResponse(GetTopRecordsResponse value) {
        return new JAXBElement<GetTopRecordsResponse>(_GetTopRecordsResponse_QNAME, GetTopRecordsResponse.class, null, value);
    }

}
