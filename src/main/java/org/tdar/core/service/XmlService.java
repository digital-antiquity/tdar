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
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.processes.CreatorAnalysisProcess.CreatorInfoLog;
import org.tdar.core.service.processes.CreatorAnalysisProcess.LogPart;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FileStoreFile.DirectoryType;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbValidationEvent;
import org.tdar.utils.jaxb.XMLFilestoreLogger;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
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
public class XmlService {

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

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Class<?>[] jaxbClasses;

    @Autowired
    private UrlService urlService;

    @Autowired
    private JaxbPersistableConverter persistableConverter;

    @Autowired
    private ObfuscationService obfuscationService;

    XMLFilestoreLogger xmlFilestoreLogger;
    
    public XmlService() throws ClassNotFoundException {
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
        JAXBContext jc = JAXBContext.newInstance(Resource.class, Institution.class, Person.class);

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
    @Transactional
    public void convertToJson(Object object, Writer writer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
        ObjectWriter objectWriter = null;
        if (logger.isTraceEnabled()) {
            objectWriter = mapper.writerWithDefaultPrettyPrinter();
        } else {
            objectWriter = mapper.writer();
        }
        objectWriter.writeValue(writer, object);
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
        StringWriter writer = new StringWriter();
        convertToJson(object, writer);
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
        JAXBContext jc = JAXBContext.newInstance(Resource.class, Institution.class, Person.class);
        final List<String> lines = IOUtils.readLines(reader);
        IOUtils.closeQuietly(reader);
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(generateSchema());

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setAdapter(persistableConverter);

        final List<JaxbValidationEvent> errors = new ArrayList<>();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                JaxbValidationEvent err = new JaxbValidationEvent(event, lines.get(event.getLocator().getLineNumber() - 1));
                errors.add(err);
                logger.warn("an XML parsing exception occurred: {}", err);
                return true;
            }
        });

        // separate out so that we can throw the exception
        Object toReturn = unmarshaller.unmarshal(new StringReader(StringUtils.join(lines, "\r\n")));// , new DefaultHandler());

        if (errors.size() > 0) {
            throw new JaxbParsingException(MessageHelper.getMessage("xmlService.could_not_parse"), errors);
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
    public void generateFOAF(Creator creator, CreatorInfoLog log) throws IOException {
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
            throw new TdarRecoverableRuntimeException("xmlService.cannot_determine_creator");
        }

        for (LogPart part : log.getCollaboratorLogPart()) {
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
        for (LogPart part : log.getKeywordLogPart()) {
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
        FileStoreFile fsf = new FileStoreFile(DirectoryType.SUPPORT, creator.getId(), file.getName());
        TdarConfiguration.getInstance().getFilestore().store(ObjectType.CREATOR, file, fsf);

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
        if (Persistable.Base.isNotNullOrTransient(institution)) {
            person_.addProperty(FOAF.member, addInstitution(model, baseUrl, institution));
        }
        return person_;
    }

    /**
     * Stores the CreatorXML Log in the filestore
     * 
     * @param model
     * @param baseUrl
     * @param person
     * @return
     * @throws Exception
     */
    public void generateCreatorLog(Creator creator, CreatorInfoLog log) throws Exception {
        File file = new File(TdarConfiguration.getInstance().getTempDirectory(), creator.getId() + ".xml");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
        convertToXML(log, writer);
        IOUtils.closeQuietly(writer);
        FileStoreFile fsf = new FileStoreFile(DirectoryType.SUPPORT, creator.getId(), file.getName());
        TdarConfiguration.getInstance().getFilestore().store(ObjectType.CREATOR, file, fsf);

    }
}
