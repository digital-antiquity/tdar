/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringInputStream;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.parser.OwlApiHierarchyParser;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.resource.OntologyController;
import org.tdar.utils.TestConfiguration;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Adam Brin
 * 
 */
public class OntologyServiceITCase extends AbstractControllerITCase {

    @Autowired
    private OntologyService ontologyService;

    @Test
    @Rollback
    public void testDegenerateOntologyDuplicates() throws Exception {
        OntologyController controller = generateNewInitializedController(OntologyController.class);
        controller.prepare();
        controller.getOntology().setTitle("test");
        controller.getOntology().setDescription("test");
        controller.setFileInputMethod(AbstractInformationResourceController.FILE_INPUT_METHOD);
        String ontologyText = IOUtils.toString(new FileInputStream(new File(TestConstants.TEST_ONTOLOGY_DIR, "degenerateTabOntologyFile.txt")));
        controller.setFileTextInput(ontologyText);
        controller.setServletRequest(getServletPostRequest());
        assertEquals(TdarActionSupport.INPUT, controller.save());
        Throwable e = null;
        try {
            ontologyService.toOwlXml(-1L, ontologyText);
        } catch (Throwable ex) {
            ex.printStackTrace();
            e = ex;
        }
        assertTrue(e instanceof TdarRecoverableRuntimeException);
        assertTrue(e.getMessage().contains("unique"));
        setIgnoreActionErrors(true);
    }

    @Ignore
    @Test
    @Rollback
    public void testProperParsing() throws Exception {
        String inputString = FileUtils.readFileToString(new File(TestConstants.TEST_CODING_SHEET_DIR,"fauna-element-ontology.txt"));
        String owlXml = ontologyService.toOwlXml(1234l, inputString);
//        ontologyService.
        logger.info(owlXml);
        OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = owlOntologyManager.loadOntologyFromOntologyDocument(new StringInputStream(owlXml));  
        logger.info("{}", ontology);
        OwlApiHierarchyParser parser = new OwlApiHierarchyParser(ontology);
        parser.generate();
        fail("I need a test");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Test
    public void testTimeFormat() {
        String timeString = OntologyController.formatTime(1000 * 60 * 60 * 1 + 1000 * 60 * 2 + 1000 * 3 + 456);
        logger.debug("time: {}", timeString);
        assertEquals("expecting 1h 2m 3s 456ms", "01:02:03.456", timeString);
    }

}
