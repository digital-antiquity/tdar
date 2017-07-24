/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.ontology.OntologyNodeWrapper;
import org.tdar.utils.PersistableUtils;

/**
 * @author Adam Brin
 * 
 */
public class OntologyServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private OntologyService ontologyService;
    @Autowired
    private OntologyNodeDao ontologyNodeDao;

    @Test
    public void testDuplicateNodeToSynonym() throws IOException {
        String ontologyTextInput = FileUtils.readFileToString(TestConstants.getFile(TestConstants.TEST_ONTOLOGY_DIR, "parentSynonymDuplicate.txt"));
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
        String ontologyTextInput = FileUtils.readFileToString(TestConstants.getFile(TestConstants.TEST_ONTOLOGY_DIR, "simpleValid.txt"));
        String owlXml = ontologyService.toOwlXml(237L, ontologyTextInput);
        // FIXME: make assertions on the generated OWL XML.
        assertNotNull(owlXml);
    }

    @Test
    @Rollback(true)
    public void testBadHierarchyParsing() throws IOException {
        Ontology ont = createOntology();
        File file = createOwlFile(ont, "not_flat.txt");
        ont = addFileToResource(ont, file);
        List<OntologyNode> rootElements = ontologyService.getRootElements(ont.getOntologyNodes());
        logger.debug("root elements: {}", rootElements);
        assertEquals(1, rootElements.size());
        String name = rootElements.iterator().next().getDisplayName();
        assertEquals("Paint", name);
    }

    @Test
    @Rollback(true)
    public void testJsonSerialization() throws IOException {
        Ontology ont = createOntology();
        File file = createOwlFile(ont, "not_flat.txt");
        ont = addFileToResource(ont, file);
        OntologyNodeWrapper wrapper = ontologyService.prepareOntologyJson(ont);
        logger.debug(wrapper.getDisplayName());
        assertEquals("Paint", wrapper.getDisplayName());
        assertEquals(9, wrapper.getChildren().size());

    }

    @Test
    @Rollback(true)
    public void testFindDatasetUsingOntology() {
        OntologyNode node = genericService.find(OntologyNode.class, 68840L);
        List<Dataset> using = ontologyNodeDao.findDatasetsUsingNode(node);
        List<Long> ids = PersistableUtils.extractIds(using);
        assertTrue(ids.contains(42990L));
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testSplitOntologyNodeSynonymIntoNode() throws IOException {
        Ontology ont = createOntology();
        File file = createOwlFile(ont, "synonym_initial.txt");
        ont = addFileToResource(ont, file);

        mapTrivialCodingSheet(ont);

        OntologyNode dent = ont.getNodeByIri("Dentary");
        OntologyNode mand = ont.getNodeByIri("Mandible");
        Long mandId = mand.getId();
        logger.debug("Dentary {}", dent);
        logger.debug("Mandible {}", mand);
        assertNull(dent);
        genericService.synchronize();
        logger.debug("nodes:{}", ont.getOntologyNodes());
        File newFile = createOwlFile(ont, "synonym_as_parent.txt");
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

    }

    private File createOwlFile(Ontology ont, String name) throws IOException {
        String ontologyTextInput = FileUtils.readFileToString(TestConstants.getFile(TestConstants.TEST_ONTOLOGY_DIR, name));
        String owlXml = ontologyService.toOwlXml(ont.getId(), ontologyTextInput);
        File file = File.createTempFile("test-owl", ".owl");
        FileUtils.write(file, owlXml);
        return file;
    }

    private void mapTrivialCodingSheet(Ontology ont) {
        CodingSheet sheet = new CodingSheet();
        sheet.setTitle("test element coding sheet");
        sheet.setDescription(sheet.getTitle());
        sheet.setDate(1234);
        sheet.markUpdated(getBasicUser());
        genericService.saveOrUpdate(sheet);
        CodingRule rule = new CodingRule(sheet, "Epiotic");
        sheet.getCodingRules().add(rule);
        genericService.saveOrUpdate(rule);
        genericService.saveOrUpdate(sheet);
        rule.setCode("E");
        rule.setOntologyNode(ont.getNodeByIri("Epiotic"));
        genericService.saveOrUpdate(rule);
    }

    @SuppressWarnings({ "deprecation", "unused" })
    @Test
    @Rollback(true)
    public void testJoinOntologyNodeSynonymIntoNode() throws IOException {
        // assert that the merged node gets dropped in mappings of IDs
        Ontology ont = createOntology();
        File file = createOwlFile(ont, "synonym_as_parent.txt");
        ont = addFileToResource(ont, file);

        mapTrivialCodingSheet(ont);

        OntologyNode dent = ont.getNodeByIri("Dentary");
        OntologyNode mand = ont.getNodeByIri("Mandible");
        Long mandId = mand.getId();
        Long dentId = dent.getId();
        logger.debug("Dentary {}", dent);
        logger.debug("Mandible {}", mand);
        assertNotNull(dent);
        genericService.synchronize();
        logger.debug("nodes:{}", ont.getOntologyNodes());
        File newFile = createOwlFile(ont, "synonym_initial.txt");
        ont = replaceFileOnResource(ont, newFile, ont.getFirstInformationResourceFile());
        logger.debug("nodes:{}", ont.getOntologyNodes());
        ont.clearTransientMaps();
        dent = ont.getNodeByIri("Dentary");
        mand = ont.getNodeByIri("Mandible");
        logger.debug("Dentary: {}", dent);
        logger.debug("Mandible: {}", mand);
        assertNull(dent);
        assertEquals(mandId, mand.getId());

    }

    private Ontology createOntology() {
        Ontology ont = new Ontology();
        ont.setTitle("test");
        ont.setDescription("test");
        ont.setDate(1234);
        ont.markUpdated(getBasicUser());
        genericService.saveOrUpdate(ont);
        return ont;
    }

    @Test
    public void testDegenerateTextToOwlXml() throws IOException {
        String ontologyTextInput = FileUtils.readFileToString(TestConstants.getFile(TestConstants.TEST_ONTOLOGY_DIR, "degenerate.txt"));
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
