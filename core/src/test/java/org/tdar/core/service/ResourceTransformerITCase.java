package org.tdar.core.service;

import java.io.StringWriter;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.transform.EMLDocumentTransformer;

import edu.asu.lib.eml.EMLDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;

public class ResourceTransformerITCase extends AbstractIntegrationTestCase {

    @Test
    public void serializeEML() throws JAXBException {
        Document resource = genericService.find(Document.class, Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        EMLDocument doc = EMLDocumentTransformer.transformAny(resource);
        StringWriter writer = new StringWriter();
        JaxbDocumentWriter.write(doc, writer, true);
        logger.debug(writer.toString());

    }
}
