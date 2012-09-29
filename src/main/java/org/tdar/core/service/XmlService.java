package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.w3c.dom.Document;

/*
 * class to help with marshalling and unmarshalling of resources
 */
@Service
public class XmlService implements Serializable {

    private static final long serialVersionUID = -7304234425123193412L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Class<?>[] jaxbClasses;

    @Transactional(readOnly = true)
    public String convertToXML(Object object) throws Exception {
        StringWriter sw = new StringWriter();
        convertToXML(object, sw);
        return sw.toString();
    }

    // FIXME: I should cache this file and use it!!!
    public File generateSchema() throws IOException, JAXBException {
        final File tempFile = File.createTempFile("tdar-schema", "xsd");
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
    UrlService urlService;

    @Transactional(readOnly = true)
    public Writer convertToXML(Object object, Writer writer) throws Exception {
        if (jaxbClasses == null) {
            jaxbClasses = ReflectionService.scanForAnnotation(XmlElement.class, XmlRootElement.class);
        }
        JAXBContext jc = JAXBContext.newInstance(jaxbClasses);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, urlService.getSchemaUrl());
        // marshaller.setProperty(Marshaller.JAXB_, urlService.getSchemaUrl());
        marshaller.marshal(object, writer);
        return writer;
    }

    @Transactional(readOnly = true)
    public Document convertToXML(Object object, Document document) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(object, document);

        return document;
    }

    public Object parseXml(File documentFile) throws JAXBException, JaxbParsingException {
        JAXBContext jc = JAXBContext.newInstance(Resource.class, Institution.class, Person.class);

        // SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        Unmarshaller unmarshaller = jc.createUnmarshaller();

        // FIXME: use schema??
        // unmarshaller.setSchema(sf.newSchema(schemaFile));

        final List<ValidationEvent> errors = new ArrayList<ValidationEvent>();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                errors.add(event);
                Object obj = new Object[] {event.getClass().getSimpleName(), event.getMessage(), event.getSeverity() };
                logger.warn("an XML parsing exception occured {} , severity: {} , msg: {}", obj);
                return true;
            }
        });

        // separate out so that we can throw the exception
        Object toReturn = unmarshaller.unmarshal(documentFile);// , new DefaultHandler());

        if (errors.size() > 0) {
            throw new JaxbParsingException("could not parse file: " + documentFile.getName(), errors);
        }

        return toReturn;
        // SAXParserFactory spf = SAXParserFactory.newInstance();
        // spf.setNamespaceAware(true);
        // spf.setValidating(true);

        // javax.xml.validation.Schema schema = sf.newSchema(new File("target/out.xsd"));
        // unmarshaller.setSchema(schema);
        //
        // UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
        //
        // SAXParser sp = spf.newSAXParser();
        // XMLReader xr = sp.getXMLReader();
        // JAXBContentAndErrorHandler contentErrorHandler = new JAXBContentAndErrorHandler(unmarshallerHandler);
        // xr.setErrorHandler(contentErrorHandler);
        // xr.setContentHandler(contentErrorHandler);
        // JAXBSource source = new JAXBSource(jc, docString);
        // Validator validator = schema.newValidator();
        // validator.setErrorHandler(new ErrorHandler() {
        //
        // public void warning(SAXParseException exception) throws SAXException {
        // System.out.println("\nWARNING");
        // exception.printStackTrace();
        // }
        //
        // public void error(SAXParseException exception) throws SAXException {
        // System.out.println("\nERROR");
        // exception.printStackTrace();
        // }
        //
        // public void fatalError(SAXParseException exception) throws SAXException {
        // System.out.println("\nFATAL ERROR");
        // exception.printStackTrace();
        // }
        // });
        // validator.validate(source);

        // InputSource xml = new InputSource(new FileReader(docString));
        // xr.parse(xml);

        // SAXParserFactory spf = SAXParserFactory.newInstance();
        // spf.setNamespaceAware(true);
        // SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        // Schema schema = sf.newSchema(new File("out.xsd"));
        // spf.setSchema(schema);
        // // unmarshaller.setSchema(schema);
        // UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
        // JAXBContentAndErrorHandler contentErrorHandler = new JAXBContentAndErrorHandler(unmarshallerHandler);
        // Document doc = (Document) unmarshaller.unmarshal(new StringReader(docString));
        // Marshaller marshaller = jc.createMarshaller();
        // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // marshaller.marshal(doc, System.out);

    }
}
