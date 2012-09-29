package org.tdar.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.AbstractSearchControllerITCase;
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
    public void loadTest() throws JAXBException, IOException, SAXException {
        // Load customer from XML
        JAXBContext jc = JAXBContext.newInstance(Resource.class, Institution.class, Person.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        String docString = FileUtils.readFileToString(new File(TestConstants.TEST_XML_DIR + "/documentImport.xml"));

        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//        Schema schema = sf.newSchema(new File("out.xsd"));
//        unmarshaller.setSchema(schema);
        Document doc = (Document) unmarshaller.unmarshal(new StringReader(docString));
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(doc, System.out);

        jc.generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(new File("out.xsd"));
            }
        });
        // Persist customer to database
    }
}
