package org.tdar.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxies;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.processes.relatedInfoLog.RelatedInfoLog;
import org.tdar.core.service.processes.relatedInfoLog.RelatedInfoLogPart;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbResultContainer;
import org.tdar.utils.jaxb.JaxbValidationEvent;
import org.tdar.utils.jaxb.XMLFilestoreLogger;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.jaxb.converters.JaxbResourceCollectionRefConverter;
import org.tdar.utils.json.LatitudeLongitudeBoxWrapper;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/*
 * class to help with marshalling and unmarshalling of resources
 */
@Service
public class SerializationService {

    private static final String RDF_KEYWORD_MEDIAN = "/rdf/keywordMedian";
    private static final String RDF_KEYWORD_MEAN = "/rdf/keywordMean";
    private static final String RDF_XML_ABBREV = "RDF/XML-ABBREV";
    private static final String FOAF_XML = ".foaf.xml";
    private static final String RDF_CREATOR_MEDIAN = "/rdf/creatorMedian";
    private static final String RDF_CREATOR_MEAN = "/rdf/creatorMean";
    private static final String RDF_COUNT = "/rdf/count";
    private static final String INSTITUTION = "Institution";
    private static final String XSD = ".xsd";
    private static final String TDAR_SCHEMA = "tdar-schema";
    private static final String S_BROWSE_CREATORS_S_RDF = "%s/browse/creators/%s/rdf";
    @SuppressWarnings("unchecked")
    private static final Class<Class<?>>[] rootClasses = new Class[] { Resource.class, Creator.class, JaxbResultContainer.class, ResourceCollection.class,
            FileProxies.class, FileProxy.class };

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JaxbPersistableConverter persistableConverter;

    @Autowired
    private JaxbResourceCollectionRefConverter collectionRefConverter;

    XMLFilestoreLogger xmlFilestoreLogger;

    public SerializationService() throws ClassNotFoundException {
        xmlFilestoreLogger = new XMLFilestoreLogger();
    }

    /**
     * Convert the existing object to an XML representation using JAXB
     * 
     * @param object
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public String convertToXML(Object object) throws Exception {
        return xmlFilestoreLogger.convertToXML(object);
    }

    /**
     * Generate the XSD schema for tDAR
     * 
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public File generateSchema() throws IOException, JAXBException {
        final File tempFile = File.createTempFile(TDAR_SCHEMA, XSD, TdarConfiguration.getInstance().getTempDirectory());
        JAXBContext jc = JAXBContext.newInstance(rootClasses);

        // WRITE OUT SCHEMA
        jc.generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(tempFile);
            }
        });

        return tempFile;
    }

    /**
     * Convert an Object to XML via JAXB, but use the writer instead of a String (For writing directly to a file or Stream)
     * 
     * @param object
     * @param writer
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Writer convertToXML(Object object, Writer writer) throws Exception {
        return xmlFilestoreLogger.convertToXML(object, writer);
    }

        /**
     * Convert an object to JSON using JAXB using writer
     * 
     * @param object
     * @param writer
     * @throws JsonProcessingException
     * @throws IOException
     */
    @Transactional(readOnly=true)
    public void convertToJson(Object object, Writer writer, Class<?> view, String callback) throws IOException {
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        ObjectWriter objectWriter = JacksonUtils.initializeObjectWriter(mapper, view);
        object = wrapObjectIfNeeded(object, callback);
        objectWriter.writeValue(writer, object);
    }

    @Transactional(readOnly = true)
    public <C> C readObjectFromJson(String json, Class<C> cls) throws IOException {
        ObjectMapper mapper = JacksonUtils.initializeObjectMapper();
        return mapper.readValue(json, cls);
    }

    /**
     * Convert an object to JSON using JAXB to string
     * 
     * @param object
     * @return
     * @throws IOException
     */
    @Transactional
    public String convertToJson(Object object) throws IOException {
        logger.trace("converting object to json:{}", object);
        StringWriter writer = new StringWriter();
        convertToJson(object, writer, null, null);
        return writer.toString();
    }

    /*
     * Takes an object, a @JsonView class (optional); and callback-name (optional); and constructs a JSON or JSONP object passing it back to the controller.
     * Most commonly used to produce a stream.
     */
    @Transactional(readOnly=true)
    public String convertFilteredJsonForStream(Object object, Class<?> view, String callback) {
        Object wrapper = wrapObjectIfNeeded(object, callback);
        String result = null;
        try {
            result = convertToFilteredJson(wrapper, view);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            wrapper = wrapObjectIfNeeded(error, callback);
            try {
                result = convertToJson(wrapper);
            } catch (IOException e1) {
            }
        } finally {
            if (result == null) {
                result = "{error:'unknown'}";
            }
        }

        return result;

    }

    private Object wrapObjectIfNeeded(Object object, String callback) {
        Object wrapper = object;
        if (StringUtils.isNotBlank(callback)) {
            wrapper = new JSONPObject(callback, object);
        }
        return wrapper;
    }

    @Transactional(readOnly=true)
    public String convertToFilteredJson(Object object, Class<?> view) throws IOException {
        StringWriter writer = new StringWriter();
        convertToJson(object, writer, view, null);
        return writer.toString();
    }

    /**
     * Convert an object to XML using JAXB, but populate a W3C XML Document
     * 
     * @param object
     * @param document
     * @return
     * @throws JAXBException
     */
    @Transactional(readOnly = true)
    public Document convertToXML(Object object, Document document) throws JAXBException {
        return xmlFilestoreLogger.convertToXML(object, document);
    }

    /**
     * Parse an XML reader stream to a java bean
     * 
     * @param reader
     * @return
     * @throws Exception
     */
    public Object parseXml(Reader reader) throws Exception {
        return parseXml(null, reader);
    }

    public Object parseXml(Class<?> c, Reader reader) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(rootClasses);
        if (c != null) {
            jc = JAXBContext.newInstance(c);
        }
        final List<String> lines = IOUtils.readLines(reader);
        IOUtils.closeQuietly(reader);
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(generateSchema());

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setAdapter(JaxbResourceCollectionRefConverter.class, collectionRefConverter);
        unmarshaller.setAdapter(JaxbPersistableConverter.class, persistableConverter);

        final List<String> errors = new ArrayList<>();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                JaxbValidationEvent err = new JaxbValidationEvent(event, lines.get(event.getLocator().getLineNumber() - 1));
                errors.add(err.toString());
                logger.warn("an XML parsing exception occurred: {}", err);
                return true;
            }
        });

        // separate out so that we can throw the exception
        Object toReturn = unmarshaller.unmarshal(new StringReader(StringUtils.join(lines, "\r\n")));// , new DefaultHandler());

        if (errors.size() > 0) {
            throw new JaxbParsingException(MessageHelper.getMessage("serializationService.could_not_parse"), errors);
        }

        return toReturn;
    }

    /**
     * Generate he FOAF RDF/XML
     * 
     * @param creator
     * @param log
     * @throws IOException
     */
    @Transactional(readOnly=true)
    public void generateFOAF(Creator<?> creator, RelatedInfoLog log) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        String baseUrl = TdarConfiguration.getInstance().getBaseUrl();
        com.hp.hpl.jena.rdf.model.Resource rdf = null;
        switch (creator.getCreatorType()) {
            case INSTITUTION:
                rdf = addInstitution(model, baseUrl, (Institution) creator);
                break;
            case PERSON:
                rdf = addPerson(model, baseUrl, (Person) creator);
                break;
        }
        if (rdf == null) {
            throw new TdarRecoverableRuntimeException("serializationService.cannot_determine_creator");
        }

        for (RelatedInfoLogPart part : log.getCollaboratorLogPart()) {
            com.hp.hpl.jena.rdf.model.Resource res = model.createResource();
            if (part.getSimpleClassName().equals(INSTITUTION)) {
                res.addProperty(RDF.type, FOAF.Organization);
            } else {
                res.addProperty(RDF.type, FOAF.Person);
            }
            res.addLiteral(FOAF.name, part.getName());
            res.addProperty(RDFS.seeAlso, String.format(S_BROWSE_CREATORS_S_RDF, baseUrl, part.getId()));
            res.addProperty(ResourceFactory.createProperty(baseUrl + RDF_COUNT), part.getCount().toString());
            rdf.addProperty(FOAF.knows, res);
        }
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + RDF_CREATOR_MEDIAN), log.getCreatorMedian().toString());
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + RDF_CREATOR_MEAN), log.getCreatorMean().toString());
        for (RelatedInfoLogPart part : log.getKeywordLogPart()) {
            com.hp.hpl.jena.rdf.model.Resource res = model.createResource();
            res.addProperty(RDF.type, part.getSimpleClassName());
            res.addLiteral(FOAF.name, part.getName());
            // res.addProperty(RDFS.seeAlso,String.format("%s/browse/creators/%s/rdf", baseUrl, part.getId()));
            res.addProperty(ResourceFactory.createProperty(baseUrl + RDF_COUNT), part.getCount().toString());
            rdf.addProperty(FOAF.topic_interest, res);
        }
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + RDF_KEYWORD_MEAN), log.getKeywordMean().toString());
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + RDF_KEYWORD_MEDIAN), log.getKeywordMedian().toString());

        File file = new File(TdarConfiguration.getInstance().getTempDirectory(), creator.getId() + FOAF_XML);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
        model.write(writer, RDF_XML_ABBREV);
        IOUtils.closeQuietly(writer);
        FileStoreFile fsf = new FileStoreFile(FilestoreObjectType.CREATOR, VersionType.METADATA, creator.getId(), file.getName());
        TdarConfiguration.getInstance().getFilestore().store(FilestoreObjectType.CREATOR, file, fsf);

    }

    /**
     * Add an institution to an RDF Model
     * 
     * @param model
     * @param baseUrl
     * @param institution
     * @return
     */
    private com.hp.hpl.jena.rdf.model.Resource addInstitution(Model model, String baseUrl, Institution institution) {
        com.hp.hpl.jena.rdf.model.Resource institution_ = model.createResource();
        institution_.addProperty(RDF.type, FOAF.Organization);
        institution_.addLiteral(FOAF.name, institution.getName());
        institution_.addProperty(RDFS.seeAlso, String.format(S_BROWSE_CREATORS_S_RDF, baseUrl, institution.getId()));
        return institution_;
    }

    /**
     * Add a person to an RDF Model
     * 
     * @param model
     * @param baseUrl
     * @param person
     * @return
     */
    private com.hp.hpl.jena.rdf.model.Resource addPerson(Model model, String baseUrl, Person person) {
        com.hp.hpl.jena.rdf.model.Resource person_ = model.createResource(FOAF.NS);
        person_.addProperty(RDF.type, FOAF.Person);
        person_.addProperty(FOAF.firstName, person.getFirstName());
        person_.addProperty(FOAF.family_name, person.getLastName());
        person_.addProperty(RDFS.seeAlso, String.format(S_BROWSE_CREATORS_S_RDF, baseUrl, person.getId()));
        Institution institution = person.getInstitution();
        if (PersistableUtils.isNotNullOrTransient(institution)) {
            person_.addProperty(FOAF.member, addInstitution(model, baseUrl, institution));
        }
        return person_;
    }

    /**
     * Stores the CreatorXML Log in the filestore
     * 
     * @return
     * @throws Exception
     */
    @Transactional(readOnly=true)
    public void generateRelatedLog(Persistable creator, RelatedInfoLog log) throws Exception {
        File file = new File(TdarConfiguration.getInstance().getTempDirectory(), creator.getId() + ".xml");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
        convertToXML(log, writer);
        IOUtils.closeQuietly(writer);
        FilestoreObjectType type = FilestoreObjectType.CREATOR;
        if (creator instanceof ResourceCollection) {
            type = FilestoreObjectType.COLLECTION;
        }

        FileStoreFile fsf = new FileStoreFile(type, VersionType.METADATA, creator.getId(), file.getName());
        TdarConfiguration.getInstance().getFilestore().store(type, file, fsf);

    }

    @Transactional(readOnly=true)
    public <C> void convertToXMLFragment(Class<C> cls, C object, Writer writer) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(cls);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        logger.trace("converting: {}", object);
        marshaller.marshal(object, writer);

    }

    @Transactional(readOnly=true)
    public String createGeoJsonFromResourceList(Map<String, Object> result, String resultKey, String rssUrl, Class<?> filter, String callback) throws IOException {
        List<Object> rslts = (List<Object>) result.get(resultKey);
        List<LatitudeLongitudeBoxWrapper> wrappers = new ArrayList<>();
        for (Object obj : rslts) {
            if (obj instanceof Resource) {
                wrappers.add(new LatitudeLongitudeBoxWrapper((Resource)obj, filter));
            }
        }
        result.put(resultKey, wrappers);
        StringWriter writer = new StringWriter();
        logger.debug("filter: {}", filter);
        convertToJson(result, writer, filter, callback);
        return writer.toString();
    }
}
