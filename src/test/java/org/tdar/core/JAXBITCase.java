package org.tdar.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.search.AbstractSearchControllerITCase;
import org.tdar.utils.jaxb.JaxbParsingException;
import org.xml.sax.SAXException;

public class JAXBITCase extends AbstractSearchControllerITCase {

    @Autowired
    XmlService xmlService;

    @Test
    public void test() throws Exception {
        Document document = genericService.find(Document.class, 4232l);
        String xml = xmlService.convertToXML(document);
        logger.info(xml);
    }

    @Ignore
    @Test
    public void testJson() throws Exception {
        Document document = genericService.find(Document.class, 4232l);
        StringWriter sw = (StringWriter)xmlService.convertToJSON(document, new StringWriter());
        logger.info(sw.toString());
        Project project = genericService.find(Project.class, 3805l);
        sw = (StringWriter)xmlService.convertToJSON(project, new StringWriter());
        logger.info(sw.toString());
    }

    @Test
    public void testProject() throws Exception {
        Project project = genericService.find(Project.class, 3805l);
        String xml = xmlService.convertToXML(project);
        logger.info(xml);
    }

    @Test
    public void loadTest() throws JAXBException, IOException, SAXException, ParserConfigurationException {

        File file = new File(TestConstants.TEST_XML_DIR + "/bad-enum-document.xml");
        Object obj = null;
        try {
            obj = xmlService.parseXml(file);
        } catch (JaxbParsingException exception) {
            logger.debug("parsing exception", exception);
            assertEquals(1, exception.getEvents().size());
        }
        logger.debug("{}", obj);
        assertNull(obj);
    }

    @Test
    public void testValidateOAIStatic() throws ConfigurationException, SAXException, IOException {
        testValidXMLResponse(new FileInputStream(new File(TestConstants.TEST_XML_DIR, "oaidc_get_records.xml")),
                "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
    }

    @Test
    public void testValidateSchema() throws ConfigurationException, SAXException, IOException {
        File schemaFile = new File("target/out.xsd");
        try {
            File generateSchema = xmlService.generateSchema();
            FileUtils.copyFile(generateSchema, schemaFile);
            testValidXMLSchemaResponse(FileUtils.readFileToString(generateSchema));
        } catch (Exception e) {
            assertFalse("I should not exist. fix me, please?", true);
        }
    }

}
