package org.tdar.core;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.search.AbstractSearchControllerITCase;
import org.xml.sax.SAXException;

public class JAXBITCase extends AbstractSearchControllerITCase {

    @Test
    public void test() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Project.class);
        Document project = genericService.find(Document.class, 4232l);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(project, System.out);
    }

    @Test
    public void loadTest() throws JAXBException, IOException, SAXException, ParserConfigurationException {

        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        JAXBContext jc = JAXBContext.newInstance(Resource.class, Institution.class, Person.class);

        // WRITE OUT SCHEMA
        jc.generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(new File("target/out.xsd"));
            }
        });

        // READ IN SCHEMA
        // Schema schema = sf.newSchema(new File("target/out.xsd"));
        File file = new File(TestConstants.TEST_XML_DIR + "/bad-enum-document.xml");

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        // unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // TODO Auto-generated method stub
                logger.info(event.getMessage());
                logger.info(event.getClass().getName());
                logger.info(Integer.toString(event.getSeverity()));
                return true;
            }
        });

        unmarshaller.unmarshal(file);// , new DefaultHandler());

        // Load customer from XML
        // Unmarshaller unmarshaller = jc.createUnmarshaller();
        // jc.generateSchema(new SchemaOutputResolver() {
        //
        // @Override
        // public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        // return new StreamResult(new File("target/out.xsd"));
        // }
        // });
        //
        // SAXParserFactory spf = SAXParserFactory.newInstance();
        // spf.setNamespaceAware(true);
        // spf.setValidating(true);
        // // Schema schema = sf.newSchema(new File("target/out.xsd"));
        // // unmarshaller.setSchema(schema);
        //
        // // UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
        //
        // // SAXParser sp = spf.newSAXParser();
        // // XMLReader xr = sp.getXMLReader();
        // // JAXBContentAndErrorHandler contentErrorHandler = new JAXBContentAndErrorHandler(unmarshallerHandler);
        // // xr.setErrorHandler(contentErrorHandler);
        // // xr.setContentHandler(contentErrorHandler);
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

        // Persist customer to database
    }
}
