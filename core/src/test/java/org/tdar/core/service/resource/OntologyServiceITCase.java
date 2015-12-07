/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
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
    @Rollback(false)
    public void testReplaceOntology() throws IOException {
        Ontology ont = new Ontology();
        ont.setTitle("test");
        ont.setDescription("test");
        ont.setDate(1234);
        ont.markUpdated(getBasicUser());
        genericService.saveOrUpdate(ont);
        String ontologyTextInput = FileUtils.readFileToString(new File(TestConstants.TEST_ONTOLOGY_DIR, "synonym_initial.txt"));
        String owlXml = ontologyService.toOwlXml(ont.getId(), ontologyTextInput);
        File file = File.createTempFile("test-owl", ".owl");
        FileUtils.write(file , owlXml);
        ont = addFileToResource(ont, file);
        
        CodingSheet sheet = new CodingSheet();
        sheet.setTitle("test element coding sheet");
        sheet.setDescription(sheet.getTitle());
        sheet.setDate(1234);
        sheet.markUpdated(getBasicUser());
        genericService.saveOrUpdate(sheet);
        CodingRule rule = new CodingRule(sheet,"Epiotic");
        sheet.getCodingRules().add(rule);
        genericService.saveOrUpdate(rule);
        genericService.saveOrUpdate(sheet);
        rule.setCode("E");
        rule.setOntologyNode(ont.getNodeByIri("Epiotic"));
        genericService.saveOrUpdate(rule);
        
        OntologyNode dent = ont.getNodeByIri("Dentary");
        OntologyNode mand = ont.getNodeByIri("Mandible");
        Long mandId = mand.getId();
        logger.debug("Dentary {}", dent);
        logger.debug("Mandible {}", mand);
        assertNull(dent);
        genericService.synchronize();
        logger.debug("nodes:{}", ont.getOntologyNodes());
        ontologyTextInput = FileUtils.readFileToString(new File(TestConstants.TEST_ONTOLOGY_DIR, "synonym_as_parent.txt"));
        owlXml = ontologyService.toOwlXml(ont.getId(), ontologyTextInput);
        File newFile = File.createTempFile("test-owl-new", ".owl");
        FileUtils.write(newFile , owlXml);
        ont = replaceFileOnResource(ont, newFile, ont.getFirstInformationResourceFile());
        logger.debug("nodes:{}", ont.getOntologyNodes());
        ont.clearTransientMaps();
        dent = ont.getNodeByIri("Dentary");
        mand = ont.getNodeByIri("Mandible");
        logger.debug("Dentary: {}", dent);
        logger.debug("Mandible: {}", mand);
        assertNotNull(dent);
        assertNotEquals(dent.getId(), mand.getId());
        assertEquals(mandId, mand.getId());

    /**
     *         logger.debug("nodes:{}", ont.getOntologyNodes());
        ontologyTextInput = FileUtils.readFileToString(new File(TestConstants.TEST_ONTOLOGY_DIR, "synonym_as_parent.txt"));
        owlXml = ontologyService.toOwlXml(ont.getId(), ontologyTextInput);
        File newFile = File.createTempFile("test-owl-new", ".owl");
        FileUtils.write(newFile , owlXml);
        InformationResourceFile irf = ont.getFirstInformationResourceFile();
        ont.getInformationResourceFiles().add(irf);
        InformationResourceFileVersion version = new InformationResourceFileVersion(VersionType.UPLOADED_ARCHIVAL, newFile.getName(), 2, ont.getId(), irf.getId());
        version.setDateCreated(new Date());
        genericService.saveOrUpdate(version);
        genericService.saveOrUpdate(irf);
        ontologyService.shred(ont);
//        ont = replaceFileOnResource(ont, newFile, ont.getFirstInformationResourceFile());
        logger.debug("nodes:{}", ont.getOntologyNodes());
        

     */
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
