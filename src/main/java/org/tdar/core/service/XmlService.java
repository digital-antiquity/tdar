package org.tdar.core.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.processes.PersonAnalysisProcess.LogPart;
import org.tdar.core.service.processes.PersonAnalysisProcess.PersonInfoLog;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.tdar.utils.jaxb.JaxbValidationEvent;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/*
 * class to help with marshalling and unmarshalling of resources
 */
@Service
public class XmlService implements Serializable {

    private static final long serialVersionUID = -7304234425123193412L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Class<?>[] jaxbClasses;

    @Autowired
    JaxbPersistableConverter persistableConverter;

    @Transactional(readOnly = true)
    public String convertToXML(Object object) throws Exception {
        StringWriter sw = new StringWriter();
        convertToXML(object, sw);
        return sw.toString();
    }

    // FIXME: I should cache this file and use it!!!
    public File generateSchema() throws IOException, JAXBException, NoSuchBeanDefinitionException, ClassNotFoundException {
        final File tempFile = File.createTempFile("tdar-schema", ".xsd");
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

    @Autowired
    private UrlService urlService;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Writer convertToXML(Object object, Writer writer) throws Exception {
        if (jaxbClasses == null) {
            jaxbClasses = ReflectionService.scanForAnnotation(XmlElement.class, XmlRootElement.class);
        }
        JAXBContext jc = JAXBContext.newInstance(jaxbClasses);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, urlService.getPairedSchemaUrl());
        // marshaller.setProperty(Marshaller.JAXB_, urlService.getSchemaUrl());
        logger.info("converting: {}", object);
        marshaller.marshal(object, writer);
        return writer;
    }

    @Transactional
    public void convertToJson(Object object, Writer writer) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(writer, object);
    }

    @Transactional
    public String convertToJson(Object object) throws IOException {
        StringWriter writer = new StringWriter();
        convertToJson(object, writer);
        return writer.toString();
    }

    @Transactional(readOnly = true)
    public Document convertToXML(Object object, Document document) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(object, document);

        return document;
    }

    public Object parseXml(Reader reader) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Resource.class, Institution.class, Person.class);
        final List<String> lines = IOUtils.readLines(reader);
        IOUtils.closeQuietly(reader);
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(generateSchema());

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setAdapter(persistableConverter);

        final List<JaxbValidationEvent> errors = new ArrayList<JaxbValidationEvent>();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                JaxbValidationEvent err = new JaxbValidationEvent(event, lines.get(event.getLocator().getLineNumber() - 1));
                errors.add(err);
                logger.warn("an XML parsing exception occured: {}", err);
                return true;
            }
        });

        // separate out so that we can throw the exception
        Object toReturn = unmarshaller.unmarshal(new StringReader(StringUtils.join(lines, "\r\n")));// , new DefaultHandler());

        if (errors.size() > 0) {
            throw new JaxbParsingException("could not parse xml: {} ", errors);
        }

        return toReturn;
    }

    public void generateFOAF(Creator creator, PersonInfoLog log) throws IOException {
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
        for (LogPart part : log.getCollaboratorLogPart()) {
            com.hp.hpl.jena.rdf.model.Resource res = model.createResource();
            if (part.getSimpleClassName().equals("Institution")) {
                res.addProperty(RDF.type, FOAF.Organization);
            } else {
                res.addProperty(RDF.type, FOAF.Person);
            }
            res.addLiteral(FOAF.name, part.getName());
            res.addProperty(RDFS.seeAlso, String.format("%s/browse/creators/%s/rdf", baseUrl, part.getId()));
            rdf.addProperty(ResourceFactory.createProperty(baseUrl + "/rdf/count"), part.getCount().toString());
            rdf.addProperty(FOAF.knows, res);
        }
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + "/rdf/creatorMedian"), log.getCreatorMedian().toString());
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + "/rdf/creatorMean"), log.getCreatorMean().toString());
        for (LogPart part : log.getKeywordLogPart()) {
            com.hp.hpl.jena.rdf.model.Resource res = model.createResource();
            res.addProperty(RDF.type, part.getSimpleClassName());
            res.addLiteral(FOAF.name, part.getName());
            // res.addProperty(RDFS.seeAlso,String.format("%s/browse/creators/%s/rdf", baseUrl, part.getId()));
            res.addProperty(ResourceFactory.createProperty(baseUrl + "/rdf/count"), part.getCount().toString());
            rdf.addProperty(FOAF.topic_interest, res);
        }
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + "/rdf/keywordMean"), log.getKeywordMean().toString());
        rdf.addProperty(ResourceFactory.createProperty(baseUrl + "/rdf/keywordMedian"), log.getKeywordMedian().toString());
        File dir = new File(TdarConfiguration.getInstance().getCreatorFOAFDir());
        dir.mkdir();
        FileWriter writer = new FileWriter(new File(dir, creator.getId() + ".xml"));
        model.write(writer, "RDF/XML-ABBREV");
        IOUtils.closeQuietly(writer);

    }

    private com.hp.hpl.jena.rdf.model.Resource addInstitution(Model model, String baseUrl, Institution institution) {
        com.hp.hpl.jena.rdf.model.Resource institution_ = model.createResource();
        institution_.addProperty(RDF.type, FOAF.Organization);
        institution_.addLiteral(FOAF.name, institution.getName());
        institution_.addProperty(RDFS.seeAlso, String.format("%s/browse/creators/%s/rdf", baseUrl, institution.getId()));
        return institution_;
    }

    private com.hp.hpl.jena.rdf.model.Resource addPerson(Model model, String baseUrl, Person person) {
        com.hp.hpl.jena.rdf.model.Resource person_ = model.createResource(FOAF.NS);
        person_.addProperty(RDF.type, FOAF.Person);
        person_.addProperty(FOAF.firstName, person.getFirstName());
        person_.addProperty(FOAF.family_name, person.getLastName());
        person_.addProperty(RDFS.seeAlso, String.format("%s/browse/creators/%s/rdf", baseUrl, person.getId()));
        Institution institution = person.getInstitution();
        if (Persistable.Base.isNotNullOrTransient(institution)) {
            person_.addProperty(FOAF.member, addInstitution(model, baseUrl, institution));
        }
        return person_;
    }
}
