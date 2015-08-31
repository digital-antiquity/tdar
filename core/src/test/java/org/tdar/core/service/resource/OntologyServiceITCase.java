/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

/**
 * @author Adam Brin
 * 
 */
public class OntologyServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private OntologyService ontologyService;

    @Test
    public void testDuplicateNodeToSynonym() throws IOException {
        String ontologyTextInput = FileUtils.readFileToString(new File(TestConstants.TEST_ONTOLOGY_DIR, "parentSynonymDuplicate.txt"));
        Exception exception = null;
        try {
            ontologyService.toOwlXml(239L, ontologyTextInput);
            fail("Should raise an java.lang.IndexOutOfBoundsException");
        } catch (TdarRecoverableRuntimeException successException) {
            exception = successException;
        }
        assertNotNull("expecting an exception", exception);
        assertTrue(exception.getMessage().contains("Falconiformes"));
    }

    @Test
    public void testValidTextToOwlXml() throws IOException {
        String ontologyTextInput = FileUtils.readFileToString(new File(TestConstants.TEST_ONTOLOGY_DIR, "simpleValid.txt"));
        String owlXml = ontologyService.toOwlXml(237L, ontologyTextInput);
        // FIXME: make assertions on the generated OWL XML.
        assertNotNull(owlXml);
    }

    @Test
    public void testDegenerateTextToOwlXml() throws IOException {
        String ontologyTextInput = FileUtils.readFileToString(new File(TestConstants.TEST_ONTOLOGY_DIR, "degenerate.txt"));
        Exception exception = null;
        try {
            logger.info(ontologyService.toOwlXml(238L, ontologyTextInput));
            fail("Should raise an java.lang.IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException successException) {
            exception = successException;
        }
        assertNotNull("expecting an exception", exception);
    }

}
