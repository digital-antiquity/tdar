/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.OAIException;
import org.tdar.core.exception.SearchPaginationException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.XmlService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.InstitutionQueryBuilder;
import org.tdar.search.query.builder.PersonQueryBuilder;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.RangeQueryPart;
import org.tdar.struts.data.DateRange;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.data.oai.OAIMetadataFormat;
import org.tdar.struts.data.oai.OAIParameter;
import org.tdar.struts.data.oai.OAIRecordProxy;
import org.tdar.struts.data.oai.OAIRecordType;
import org.tdar.struts.data.oai.OAIResumptionToken;
import org.tdar.struts.data.oai.OAIVerb;
import org.tdar.struts.data.oai.OaiErrorCode;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opensymphony.xwork2.interceptor.ParameterNameAware;

import edu.asu.lib.jaxb.JaxbDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;
import freemarker.ext.dom.NodeModel;

/**
 * @author Adam Brin
 * @author Conal Tuohy
 * 
 */
@Namespace("/oai-pmh")
@ParentPackage("default")
@Component
@Scope("prototype")
public class OAIController extends AbstractLookupController<Indexable> implements ParameterNameAware {

    private static final long serialVersionUID = 7948823253518070317L;
    @Autowired
    ObfuscationService obfuscationService;

    // results
    public static final String SUCCESS_IDENTIFY = "success-identify";
    public static final String SUCCESS_LIST_IDENTIFIERS = "success-list-identifiers";
    public static final String SUCCESS_LIST_METADATA_FORMATS = "success-list-metadata-formats";
    public static final String SUCCESS_GET_RECORD = "success-get-record";
    public static final String SUCCESS_LIST_RECORDS = "success-list-records";
    @SuppressWarnings("unused")
    public static final String SUCCESS_LIST_SETS = "success-list-sets";

    // OAI-PMH URL parameters
    private String verb;
    private String identifier;
    private String metadataPrefix;
    private String set;
    private String from;
    private String until;
    private OAIResumptionToken resumptionToken;
    // OAI-PMH response data
    private OAIResumptionToken newResumptionToken;
    private OAIException unacceptableParameterException = null;
    private OAIException invalidResumptionToken = null;
    private OAIException invalidDate = null;
    private OAIException invalidIdentifier = null;
    private OaiErrorCode errorCode;
    private List<OAIRecordProxy> records;
    private OAIRecordProxy record;
    private OAIMetadataFormat[] metadataFormats;

    // values created in response to setting an identifier
    private OAIRecordType identifierType;
    private long identifierRecordNumber;

    // configuration values
    private String repositoryNamespaceIdentifier = TdarConfiguration.getInstance().getRepositoryNamespaceIdentifier();
    private String repositoryName = TdarConfiguration.getInstance().getRepositoryName();
    private String adminEmail = TdarConfiguration.getInstance().getSystemAdminEmail();
    private String description = TdarConfiguration.getInstance().getSystemDescription();
    private boolean enableEntities = TdarConfiguration.getInstance().getEnableEntityOai();
    private OaiDcProvider oaiObject;
    private QueryBuilder resourceQueryBuilder;
    private PersonQueryBuilder personQueryBuilder;
    private InstitutionQueryBuilder institutionQueryBuilder;
    private String mode = "OAI";

    @Autowired
    private XmlService xmlService;

    // http://.../oai-pmh/oai?
    @Action(value = "oai", results = {
            @Result(name = SUCCESS_GET_RECORD, location = "getRecord.ftl", type = "freemarker", params = {
                    "contentType", "text/xml" }),
            @Result(name = SUCCESS_IDENTIFY, location = "identify.ftl", type = "freemarker", params = { "contentType", "text/xml" }),
            @Result(name = SUCCESS_LIST_IDENTIFIERS, location = "listIdentifiers.ftl", type = "freemarker", params = { "contentType", "text/xml" }),
            @Result(name = SUCCESS_LIST_RECORDS, location = "listRecords.ftl", type = "freemarker", params = { "contentType", "text/xml" }),
            @Result(name = SUCCESS_LIST_METADATA_FORMATS, location = "listMetadataFormats.ftl", type = "freemarker", params = { "contentType", "text/xml" }),
            @Result(name = ERROR, location = "error.ftl", type = "freemarker", params = { "contentType", "text/xml" })
    })
    public String oai() {
        OAIVerb verbToHandle = getVerbEnum();
        try {
            if (unacceptableParameterException != null) {
                // an unknown parameter was passed from the harvester
                throw unacceptableParameterException;
            }

            if (invalidResumptionToken != null) {
                // a bogus resumptionToken was passed from the harvester
                throw invalidResumptionToken;
            }

            if (invalidDate != null) {
                // a bogus from or until parameter was passed from the harvester
                throw invalidDate;
            }

            if (invalidIdentifier != null) {
                // a bogus identifier identifier parameter was passed from the harvester
                throw invalidIdentifier;
            }

            // Sets are currently not supported
            if (set != null) {
                throw new OAIException(getText("oaiController.sets_not_supported"), OaiErrorCode.NO_SET_HIERARCHY);
            }

            if (verbToHandle == null) {
                getLogger().warn("NO VERB PROVIDED");
                throw new OAIException(getText("oaiController.bad_verb"), OaiErrorCode.BAD_VERB);
            } else {
                switch (verbToHandle) {
                    case IDENTIFY:
                        return identifyVerb();
                    case LIST_IDENTIFIERS:
                        return listIdentifiersVerb();
                    case LIST_RECORDS:
                        return listRecordsVerb();
                    case LIST_SETS:
                        return listSetsVerb();
                    case LIST_METADATA_FORMATS:
                        return listMetadataFormatsVerb();
                    case GET_RECORD:
                        return getRecordVerb();
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            if (e instanceof OAIException) {
                setErrorCode(((OAIException) e).getCode());
            } else {
                setErrorCode(OaiErrorCode.BAD_ARGUMENT);
            }
            errorCode.setMessage(e.getMessage());
            if (e instanceof SearchPaginationException) {
                getLogger().trace(e.getMessage(), e);
            } else {
                getLogger().debug(e.getMessage(), e);
            }
        }
        return ERROR;
    }

    /**
     * @return the Enumeration corresponding to the client-specified verb
     */
    public OAIVerb getVerbEnum() {
        return OAIVerb.fromString(getVerb());
    }

    public OAIMetadataFormat getMetadataPrefixEnum() throws OAIException {
        return OAIMetadataFormat.fromString(getMetadataPrefix());
    }

    private String identifyVerb() throws OAIException {
        String message = getText("oaiController.not_allowed_with_identity");
        assertParameterIsNull(metadataPrefix, "metadataPrefix", message);
        assertParameterIsNull(identifier, "identifier", message);
        assertParameterIsNull(set, "set", message);
        assertParameterIsNull(resumptionToken, "resumptionToken", message);
        assertParameterIsNull(from, "from", message);
        assertParameterIsNull(until, "until", message);
        return SUCCESS_IDENTIFY;
    }

    private void assertParameterIsNull(Object parameter, String parameterName, String message) throws OAIException {
        if (parameter != null) {
            throw new OAIException(getText("oaiController.bad_arguement", parameterName, message), OaiErrorCode.BAD_ARGUMENT);
        }
    }

    /*
     * Create a list of the identifiers and (optionally also include metadata records)
     * The method generates three distinct queries; one for people, one for institutions, and one for resources,
     * and populates the results list with (a page from) the result of each of those queries.
     * This means that the cursor encoded in the resumptionTokens applies equally to all of the queries.
     * This means that each response sent to a harvester will typically contain a mixture of the different types of records.
     * Typically the last page of results which a harvester receives will consist just of Resource records, as the other entities
     * will have a lower cardinality and will run out more quickly.
     */
    private void listIdentifiersOrRecords(boolean includeRecords) throws ParserConfigurationException, JAXBException, OAIException, ParseException {

        // Check parameters

        // The only valid parameters for ListRecords are verb, from, until, set, resumptionToken, and metadataPrefix: identifier is always invalid
        assertParameterIsNull(identifier, "identifier", getText("oaiController.not_allowed_with_list"));
        // the resumptionToken parameter is exclusive - if present, then no other parameters may be present apart from verb
        if (resumptionToken != null) {
            String message = "Not allowed with resumptionToken";
            assertParameterIsNull(from, "from", message);
            assertParameterIsNull(until, "until", message);
            assertParameterIsNull(metadataPrefix, "metadataPrefix", message);
        }

        // Sort results by dateUpdated ascending and filter
        // by dates, either supplied in OAI-PMH 'from' and 'to' parameters,
        // or encoded as parts of the 'resumptionToken'
        // Optionally filter results by date range
        // In Lucene, dates are stored as "yyyyMMddHHmmssSSS" in UTC time zone
        // see http://docs.jboss.org/hibernate/search/3.4/reference/en-US/html_single/#d0e3510
        // but in OAI-PMH, date parameters are ISO8601 dates, so we must remove the punctuation.
        String effectiveMetadataPrefix = metadataPrefix;
        Date effectiveFrom = new DateTime("1900").toDate();
        Date effectiveUntil = new DateTime("3000").toDate();
        if (from != null) {
            effectiveFrom = new DateTime(from).toDate();
        }
        if (until != null) {
            effectiveUntil = new DateTime(until).toDate();
        }
        // start record number (cursor)
        int cursor = 0;
        if (resumptionToken != null) {
            // ... then this is the second or subsequent page of results.
            // In this case there are no separate "from" and "until" parameters passed by the client;
            // instead all the parameters come packed into a resumption token.
            cursor = resumptionToken.getCursor();
            effectiveFrom = resumptionToken.getFromDate();
            effectiveUntil = resumptionToken.getUntilDate();
            effectiveMetadataPrefix = resumptionToken.getMetadataPrefix();
        }
        setSortField(SortOption.DATE_UPDATED);
        OAIMetadataFormat metadataFormat = OAIMetadataFormat.fromString(effectiveMetadataPrefix);

        // now actually build the queries and execute them
        resourceQueryBuilder = new ResourceQueryBuilder();
        resourceQueryBuilder.append(new FieldQueryPart<>("status", Status.ACTIVE));

        personQueryBuilder = new PersonQueryBuilder();
        personQueryBuilder.append(new FieldQueryPart<>("status", Status.ACTIVE));
        institutionQueryBuilder = new InstitutionQueryBuilder();
        institutionQueryBuilder.append(new FieldQueryPart<>("status", Status.ACTIVE));

        records = new ArrayList<>();
        int totalPersons = 0;
        int totalInstitutions = 0;
        int totalResources = 0;

        // only publish Persons and Institutions if this feature is specifically enabled in TDAR configuration,
        // and only with oai_dc and tdar metadata formats, not MODS
        if (enableEntities && !metadataFormat.equals(OAIMetadataFormat.MODS)) {
            // list people
            totalPersons = populateResult(includeRecords, OAIRecordType.PERSON, personQueryBuilder, cursor, metadataFormat, effectiveFrom, effectiveUntil);
            totalInstitutions = populateResult(includeRecords, OAIRecordType.INSTITUTION, institutionQueryBuilder, cursor, metadataFormat, effectiveFrom,
                    effectiveUntil);
        }

        // list the resources
        totalResources = populateResult(includeRecords, OAIRecordType.RESOURCE, resourceQueryBuilder, cursor, metadataFormat, effectiveFrom, effectiveUntil);

        // if any of the queries returned more than a page of search results, create a resumptionToken to allow
        // the client to continue harvesting from that point
        int recordsPerPage = getRecordsPerPage();
        if ((totalResources > recordsPerPage) || (totalPersons > recordsPerPage) || (totalInstitutions > recordsPerPage)) {
            // ... then this is a partial response, and should be terminated with a ResumptionToken
            // which may be empty if this is the last page of results
            newResumptionToken = new OAIResumptionToken();
            // advance the cursor by one page
            cursor = getNextPageStartRecord();
            // check if there would be any resources, persons or institutions in that hypothetical next page
            if ((totalResources > cursor) || (totalPersons > cursor) || (totalInstitutions > cursor)) {
                // ... populate the resumptionToken so the harvester can continue harvesting from the next page
                newResumptionToken.setCursor(cursor);
                newResumptionToken.setFromDate(effectiveFrom);
                newResumptionToken.setUntilDate(effectiveUntil);
                newResumptionToken.setMetadataPrefix(effectiveMetadataPrefix);
            }
        }

        // if there were no records found, then throw an exception
        if ((totalResources + totalPersons + totalInstitutions) == 0) {
            throw new OAIException(getText("oaiController.no_matches"), OaiErrorCode.NO_RECORDS_MATCH);
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private int populateResult(boolean includeRecords, OAIRecordType recordType, QueryBuilder queryBuilder, int cursor, OAIMetadataFormat metadataFormat,
            Date effectiveFrom, Date effectiveUntil)
            throws ParseException, ParserConfigurationException,
            JAXBException, OAIException {
        setStartRecord(cursor);
        queryBuilder.append(new RangeQueryPart(QueryFieldNames.DATE_UPDATED, new DateRange(effectiveFrom, effectiveUntil)));
        int total = 0;
        try {
            switchProjectionModel(queryBuilder);
            super.handleSearch(queryBuilder);
            total = getTotalRecords();
            List<Long> ids = new ArrayList<>();
            for (Indexable i : getResults()) {
                if ((i instanceof Viewable) && !((Viewable) i).isViewable()) {
                    continue;
                }
                OaiDcProvider resource = (OaiDcProvider) i;
                ids.add(resource.getId());
                // create OAI metadata for the record
                OAIRecordProxy proxy = new OAIRecordProxy(repositoryNamespaceIdentifier, recordType, resource.getId(), resource.getDateUpdated());
                if (includeRecords) {
                    proxy.setMetadata(createNodeModel(resource, metadataFormat));
                }
                records.add(proxy);
            }
            getLogger().info("ALL IDS: {}", ids);
        } catch (SearchPaginationException spe) {
            getLogger().debug("an pagination exception happened .. {} ", spe.getMessage());
        } catch (TdarRecoverableRuntimeException e) {
            // this is expected as the cursor follows the "max" results for person/inst/resource so, whatever the max is
            // means that the others will throw this error.
            getLogger().debug("an exception happened .. {} ", e);
        }
        return total;
    }

    private void switchProjectionModel(QueryBuilder queryBuilder) {
        setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        if (queryBuilder instanceof ResourceQueryBuilder) {
            setProjectionModel(ProjectionModel.RESOURCE_PROXY);
        }
    }

    private Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        return document;
    }

    private NodeModel createNodeModel(OaiDcProvider object, OAIMetadataFormat requestedFormat) throws ParserConfigurationException, JAXBException,
            OAIException {

        // create a W3C DOM to populate with the Institution data
        Document document = createDocument();

        // Prior to publication of the metadata, obscure anything confidential

        // First "walk" the object tree to load everything into memory, since obfuscation will detach the object
        // from the Hibernate session, which will prevent loading lazily-initialized properties.
        // xmlService.convertToXML((Persistable) object);
        if (object instanceof Obfuscatable) {
            obfuscationService.obfuscate((Obfuscatable) object, getAuthenticatedUser());
        }

        // if the desired metadata format is "tdar", then simply marshal the Person
        // into the W3C DOM, otherwise, create a stub record.
        JaxbDocument jaxbDocument = null;
        switch (requestedFormat) {
            case TDAR:
                xmlService.convertToXML(object, document);
                break;
            case DC: // for strict compliance with OAI-PMH spec, we have to be able to disseminate oai_dc, but it's purely pro forma and of no practical use
                if (object instanceof Creator) {
                    populateStubDcRecord(document, object.getTitle(), object.getClass().getSimpleName(), object.getId());
                } else {
                    jaxbDocument = DcTransformer.transformAny((Resource) object);
                    JaxbDocumentWriter.write(jaxbDocument, document, true);
                    getLogger().trace("case:document: {} ", jaxbDocument);
                }
                break;
            case MODS:
                if (object instanceof Creator) {
                    throw new OAIException(getText("oaiController.cannot_disseminate"), OaiErrorCode.CANNOT_DISSEMINATE_FORMAT);
                }
                jaxbDocument = ModsTransformer.transformAny((Resource) object);
                JaxbDocumentWriter.write(jaxbDocument, document, true);
                getLogger().trace("case:document: {} ", jaxbDocument);
                break;
        }

        // marshal the W3C DOM XML into a FreeMarker NodeModel for rendering by FTL
        return NodeModel.wrap(document);
    }

    // For strict compliance with the OAI-PMH specification, it's necessary to be able to disseminate oai_dc for every record
    // In the case of Person and Institution records, the oai_dc metadata format is quite inappropriate and not very useful,
    // so this method exists to create a "stub" record for the sake of compliance. In real life these records generally won't be harvested.
    // The method populates an empty DOM document with an oai_dc record containing a title, a type, and an identifier.
    private void populateStubDcRecord(Document document, String title, String type, long identifier) {
        String dc = "http://purl.org/dc/elements/1.1/";
        Element rootElement = document.createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "dc");
        Element titleElement = document.createElementNS(dc, "title");
        Element identifierElement = document.createElementNS(dc, "identifier");
        Element typeElement = document.createElementNS(dc, "type");
        document.appendChild(rootElement);
        rootElement.setAttributeNS(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                "xsi:schemaLocation",
                OAIMetadataFormat.DC.getNamespace() + " " + OAIMetadataFormat.DC.getSchemaLocation()
                );
        rootElement.appendChild(titleElement);
        rootElement.appendChild(typeElement);
        rootElement.appendChild(identifierElement);
        titleElement.setTextContent(title);
        identifierElement.setTextContent(Long.toString(identifier));
        typeElement.setTextContent(type);
    }

    private String listIdentifiersVerb() throws ParserConfigurationException, JAXBException, OAIException, ParseException {
        listIdentifiersOrRecords(false);
        return SUCCESS_LIST_IDENTIFIERS;
    }

    private String listRecordsVerb() throws ParserConfigurationException, JAXBException, OAIException, ParseException {
        listIdentifiersOrRecords(true);
        return SUCCESS_LIST_RECORDS;
    }

    private String listMetadataFormatsVerb() throws OAIException {
        String message = "Not allowed with ListMetadataFormats verb";
        assertParameterIsNull(metadataPrefix, "metadataPrefix", message);
        assertParameterIsNull(set, "set", message);
        assertParameterIsNull(resumptionToken, "resumptionToken", message);
        assertParameterIsNull(from, "from", message);
        assertParameterIsNull(until, "until", message);
        if (identifier == null) {
            // no identifier was specified, then return a list of all the metadata formats that can be disseminated
            setMetadataFormats(OAIMetadataFormat.values());
        } else {
            // an identifier was specified, so return a list of the metadata formats which that record can be disseminated in
            setMetadataFormats(getIdentifiedRecordType().getMetadataFormats());
        }
        if (!TdarConfiguration.getInstance().enableTdarFormatInOAI()) {
            setMetadataFormats((OAIMetadataFormat[]) ArrayUtils.removeElement(getMetadataFormats(), OAIMetadataFormat.TDAR));
        }
        return SUCCESS_LIST_METADATA_FORMATS;
    }

    private void setMetadataFormats(OAIMetadataFormat[] values) {
        this.metadataFormats = values;
    }

    public OAIMetadataFormat[] getMetadataFormats() {
        return metadataFormats;
    }

    private String listSetsVerb() throws OAIException {
        throw new OAIException(getText("oaiController.sets_not_supported"), OaiErrorCode.NO_SET_HIERARCHY);
        // return SUCCESS_LIST_SETS;
    }

    private String getRecordVerb() throws JAXBException, OAIException, ParserConfigurationException {
        // validate parameters
        String message = getText("oaiController.not_allowed_with_get");

        assertParameterIsNull(resumptionToken, "resumptionToken", message);
        assertParameterIsNull(from, "from", message);
        assertParameterIsNull(until, "until", message);

        if (identifier == null) {
            throw new OAIException(getText("oaiController.missing_identifer"), OaiErrorCode.BAD_ARGUMENT);
        }

        // check the requested metadata format
        OAIMetadataFormat requestedFormat = getMetadataPrefixEnum();

        if (requestedFormat == null) {
            throw new OAIException(getText("oaiController.invalid_metadata_param"),
                    OaiErrorCode.BAD_ARGUMENT);
        }

        // check that this kind of record can be disseminated in the requested format
        getIdentifiedRecordType().checkCanDisseminateFormat(requestedFormat);

        // Convert the resource metadata into a FreeMarker NodelModel for rendering,
        // and wrap it up with the OAI record metadata for FreeMarker
        OAIRecordProxy record = null;
        NodeModel ftlNode = null;

        Date updatedDate = getOaiObject().getDateUpdated();
        if (updatedDate == null) {
            updatedDate = getOaiObject().getDateCreated();
        }

        record = new OAIRecordProxy(repositoryNamespaceIdentifier, getIdentifiedRecordType(), getOaiObject().getId(), updatedDate);
        ftlNode = createNodeModel(getOaiObject(), requestedFormat);
        record.setMetadata(ftlNode);

        // return the record to FreeMarker
        setRecord(record);
        return SUCCESS_GET_RECORD;
    }

    /**
     * @return the verb
     */
    public String getVerb() {
        return verb;
    }

    /**
     * @param verb
     *            the verb to set
     */
    public void setVerb(String verb) {
        this.verb = verb;
    }

    /**
     * @return the identifier
     * @see <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#UniqueIdentifier">Open Archives Initiative - Protocol for Metadata Harvesting
     *      v2.0 § 2.4 Unique Identifier</a>
     * @see <a href="http://www.openarchives.org/OAI/2.0/guidelines-oai-identifier.htm">Specification and XML Schema for the OAI Identifier Format</a>
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     * @throws OAIException
     * @see <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#UniqueIdentifier">Open Archives Initiative - Protocol for Metadata Harvesting
     *      v2.0 § 2.4 Unique Identifier</a>
     * @see <a href="http://www.openarchives.org/OAI/2.0/guidelines-oai-identifier.htm">Specification and XML Schema for the OAI Identifier Format</a>
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        try {
            // Parse the OAI identifier into its constituent parts
            // Identifier format is "oai:{repository-namespace-id}:{type}:{numeric-identifier}"
            // e.g. "oai:tdar.org:Resource:100"
            String[] token = identifier.split(":", 4);
            // First token must = "oai"
            // Second token must = the repository namespace identifier
            if (!token[0].equals("oai") || !token[1].equals(repositoryNamespaceIdentifier)) {
                throw new OAIException(getText("oaiController.identifier_not_part"), OaiErrorCode.ID_DOES_NOT_EXIST);
            }
            // the third token is the type of the record, i.e. "Resource", "Person" or "Institution"
            setIdentifierType(token[2]);
            // the final token is the record number
            setIdentifierRecordNumber(Long.valueOf(token[3]));
            // finally load the identified Person, Institution or Resource
            Class<?> oaiObjectClass = Resource.class;
            switch (getIdentifiedRecordType()) {
                case PERSON:
                    oaiObjectClass = Person.class;
                    break;
                case INSTITUTION:
                    oaiObjectClass = Institution.class;
                    break;
                default:
                    break;
            }
            setOaiObject((OaiDcProvider) getGenericService().find(oaiObjectClass, getIdentifierRecordNumber()));
            if (getOaiObject() == null) {
                throw new OAIException(
                        getText("oaiController.no_identifier", oaiObjectClass.getSimpleName()),
                        OaiErrorCode.ID_DOES_NOT_EXIST);
            }

        } catch (OAIException e) {
            // save this exception to throw later when the main oai() method is called
            invalidIdentifier = e;
        }
    }

    /**
     * @param type
     *            the type of the record identified by the identifier, i.e. "Resource", "Person" or "Institution"
     * @throws OAIException
     */
    private void setIdentifierType(String type) throws OAIException {
        identifierType = OAIRecordType.fromString(type);
    }

    /**
     * @return the type of the record identified by the identifier, i.e. "Resource", "Person" or "Institution"
     */
    private OAIRecordType getIdentifiedRecordType() {
        return identifierType;
    }

    /**
     * @param recordNumber
     *            the numeric portion of the identifier
     */
    private void setIdentifierRecordNumber(long recordNumber) {
        identifierRecordNumber = recordNumber;
    }

    /**
     * @return the numeric portion of the identifier
     */
    private long getIdentifierRecordNumber() {
        return identifierRecordNumber;
    }

    /**
     * @return the metadataPrefix
     */
    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    /**
     * @param metadataPrefix
     *            the metadataPrefix to set
     */
    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public OaiErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(OaiErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getRepositoryNamespaceIdentifier() {
        return repositoryNamespaceIdentifier;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getSet() {
        return set;
    }

    /*
     * @param resumptionToken the String content of the OAI-PMH resumptionToken which was sent in the client's request
     */
    public void setResumptionToken(String resumptionToken) {
        try {
            this.resumptionToken = new OAIResumptionToken(resumptionToken);
        } catch (OAIException oaie) {
            invalidResumptionToken = oaie;
        }
    }

    /*
     * @return the OAI-PMH resumptionToken which was sent in the client's request
     */
    public String getResumptionToken() {
        if (resumptionToken != null) {
            return resumptionToken.getToken();
        }
        return null;
    }

    /*
     * @param newResumptionToken the OAI-PMH resumptionToken which is to be returned to the client
     */
    public void setNewResumptionToken(OAIResumptionToken newResumptionToken) {
        this.newResumptionToken = newResumptionToken;
    }

    /*
     * @return the OAI-PMH resumptionToken which is to be returned to the client
     */
    public OAIResumptionToken getNewResumptionToken() {
        return newResumptionToken;
    }

    private void checkDate(String date) {
        String shortDate = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
        String longDate = shortDate + "";
        if (!date.matches(shortDate) && !date.matches(longDate)) {
            invalidDate = new OAIException("Invalid date specified: " + date, OaiErrorCode.BAD_ARGUMENT);
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
        checkDate(from);
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
        checkDate(until);
    }

    /**
     * @return the records
     */
    public List<OAIRecordProxy> getRecords() {
        return records;
    }

    /**
     * @param records
     *            the records to set
     */
    public void setRecords(List<OAIRecordProxy> records) {
        this.records = records;
    }

    /**
     * @return the record
     */
    public OAIRecordProxy getRecord() {
        return record;
    }

    /**
     * @param record
     *            the record to set
     */
    public void setRecord(OAIRecordProxy record) {
        this.record = record;
    }

    @Override
    public boolean acceptableParameterName(String parameterName) {
        // Called by Struts framework to ask if the parameter name is acceptable
        getLogger().trace("OAI parameter:" + parameterName);
        try {
            OAIParameter.fromString(parameterName);
            return true;
        } catch (OAIException oaie) {
            // Unknown parameter! But don't throw this exception now because Struts won't like it - stash it for the oai() method to process
            unacceptableParameterException = oaie;
            return false;
        }
    }

    private OaiDcProvider getOaiObject() {
        return oaiObject;
    }

    private void setOaiObject(OaiDcProvider oaiObject) {
        this.oaiObject = oaiObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.search.query.SearchResultHandler#setMode(java.lang.String)
     */
    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.search.query.SearchResultHandler#getMode()
     */
    @Override
    public String getMode() {
        return mode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        return null;
    }
}
