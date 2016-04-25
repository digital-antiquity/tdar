package org.tdar.core.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.OntologyNode;
/**
 * $Id$
 * 
 * Exercises the OWL API hierarchy parser.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class OwlApiHierarchyParserTest {

    // FIXME: no point in these being anywhere but in src/test/resources/
    public final static String EXAMPLE_FAUNA = TestConstants.TEST_ONTOLOGY_DIR + "example-fauna.owl";

    public final static String EXAMPLE_HIERARCHY = TestConstants.TEST_ONTOLOGY_DIR + "example-hierarchy.owl";

    private static final transient Logger logger = LoggerFactory.getLogger(OwlApiHierarchyParserTest.class);

    public OWLOntology getOwlOntology() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        try {
            return manager.loadOntology(IRI.create(new File(EXAMPLE_FAUNA)));
        } catch (OWLOntologyCreationException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Couldn't load ontology from file: " + EXAMPLE_FAUNA, exception);
        }
    }

    @Test
    public void testOwlHierarchyMap() {
        OWLOntology owlOntology = getOwlOntology();
        OwlApiHierarchyParser parser = new OwlApiHierarchyParser(owlOntology);
        Map<OWLClass, Set<OWLClass>> owlHierarchyMap = parser.getOwlHierarchyMap();
        for (Map.Entry<OWLClass, Set<OWLClass>> entry : owlHierarchyMap.entrySet()) {
            OWLClass owlClass = entry.getKey();
            Set<OWLClass> children = entry.getValue();
            assertNotNull(owlClass);
            assertNotNull(children);
            for (OWLClass child : children) {
                assertNotNull(child);
                assertTrue(owlHierarchyMap.containsKey(child));
            }
        }
    }

    @Test
    public void testOntologyNodeGeneration() {
        OWLOntology owlOntology = getOwlOntology();
        OwlApiHierarchyParser parser = new OwlApiHierarchyParser(owlOntology);
        Map<OWLClass, Set<OWLClass>> hierarchyMap = parser.getOwlHierarchyMap();
        List<OntologyNode> nodes = parser.generate();
        Map<OWLClass, OntologyNode> classNodeMap = parser.getOwlClassNodeMap();
        assertFalse(nodes.isEmpty());
        Collections.sort(nodes, new Comparator<OntologyNode>() {
            @Override
            public int compare(OntologyNode a, OntologyNode b) {
                return a.getIri().compareTo(b.getIri());
            }
        });

        // ensure that all node interval starts are <= node ends
        for (OntologyNode ontologyNode : nodes) {
            logger.debug(String.format("Label: %s, [%d, %d] (%s)", ontologyNode.getIri(), ontologyNode.getIntervalStart(), ontologyNode.getIntervalEnd(),
                    ontologyNode.getIndex()));

            assertTrue("ontology node start should always be less than end", ontologyNode.getIntervalStart() <= ontologyNode.getIntervalEnd());
        }
        // ensure root node covers bounds for all nodes
        for (OWLClass rootClass : parser.getRootClasses()) {
            OntologyNode rootNode = classNodeMap.get(rootClass);
            assertNotNull(rootNode);
            for (OntologyNode ontologyNode : nodes) {
                if (rootNode.equals(ontologyNode)) {
                    continue;
                }
                assertTrue(rootNode.getIntervalStart() < ontologyNode.getIntervalStart());
                assertTrue(rootNode.getIntervalEnd() > ontologyNode.getIntervalEnd());
            }
        }
        // test bounds for all children
        for (Map.Entry<OWLClass, Set<OWLClass>> entry : hierarchyMap.entrySet()) {
            OWLClass parentClass = entry.getKey();
            OntologyNode parent = classNodeMap.get(parentClass);
            for (OWLClass childClass : entry.getValue()) {
                OntologyNode child = classNodeMap.get(childClass);
                assertTrue("parent start should always be less than child start", parent.getIntervalStart() < child.getIntervalStart());
                assertTrue("parent end should always be greater than child end", parent.getIntervalEnd() > child.getIntervalEnd());
            }
        }
    }
}
