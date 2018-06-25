package org.tdar.core;

import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.SerializationServiceImpl;
import org.xml.sax.SAXException;

public class SchemaValidationTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testValidateOAIStatic() throws ConfigurationException, SAXException, IOException, ClassNotFoundException {
        testValidXMLResponse(new FileInputStream(TestConstants.getFile(TestConstants.TEST_XML_DIR, "oaidc_get_records.xml")));
    }

    @Test
    public void testValidateSchema() throws ConfigurationException, SAXException, IOException {
        File schemaFile = new File("target/out.xsd");
        try {
            SerializationService serializationService = new SerializationServiceImpl();
            File generateSchema = serializationService.generateSchema();
            FileUtils.copyFile(generateSchema, schemaFile);
            logger.debug("{}", generateSchema);
            testValidXMLSchemaResponse(FileUtils.readFileToString(generateSchema));
        } catch (Exception e) {
            logger.warn("exception", e);
            assertFalse("I should not exist. fix me, please?", true);
        }
    }

    /**
     * Validate a response against an external schema
     * 
     * @param schemaLocation
     *            the URL of the schema to use to validate the document
     * @throws ConfigurationException
     * @throws SAXException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public void testValidXMLResponse(InputStream code) throws ConfigurationException, SAXException, FileNotFoundException, ClassNotFoundException {
        testValidXML(code);
    }

    /**
     * Validate that a response is a valid XML schema
     * 
     * @throws ConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void testValidXMLSchemaResponse(String code) throws ConfigurationException, SAXException, IOException, ClassNotFoundException {
        SerializationService serializationService = new SerializationServiceImpl();
        JaxbSchemaValidator setupValidator = new JaxbSchemaValidator(serializationService);

    }

    private void testValidXML(InputStream code) throws FileNotFoundException, SAXException, ClassNotFoundException {
        SerializationService serializationService = new SerializationServiceImpl();
        JaxbSchemaValidator v = new JaxbSchemaValidator(serializationService);

        InputStream rereadableStream = null;
        try {
            rereadableStream = new ByteArrayInputStream(IOUtils.toByteArray(code));
        } catch (Exception e) {
            logger.error("", e);
        }
        if (rereadableStream == null) {
            rereadableStream = code;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(rereadableStream));
        StreamSource is = new StreamSource(reader);
        List<?> errorList = v.getInstanceErrors(is);

        if (!errorList.isEmpty()) {
            StringBuffer errors = new StringBuffer();
            for (Object error : errorList) {
                errors.append(error.toString());
                errors.append(System.getProperty("line.separator"));
                logger.error(error.toString());
            }
            String content = "";
            try {
                rereadableStream.reset();
                content = IOUtils.toString(rereadableStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Assert.fail("Instance invalid: " + errors.toString() + " in:\n" + content);
        }
    }
}
