/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.resource.OntologyController;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.junit.Assert.*;

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

    @Test
    @Rollback
    public void testProperParsing() throws Exception {
        OntologyController controller = generateNewInitializedController(OntologyController.class);
        controller.prepare();
        controller.getOntology().setTitle("test");
        controller.getOntology().setDescription("test");
        controller.setFileInputMethod(AbstractInformationResourceController.FILE_INPUT_METHOD);
        String ontologyText = IOUtils.toString(new FileInputStream(new File(TestConstants.TEST_CODING_SHEET_DIR, "fauna-element-ontology.txt")));
        controller.setFileTextInput(ontologyText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Ontology ontology = controller.getOntology();
        List<OntologyNode> nodes = ontology.getOntologyNodes();
        Collections.sort(nodes,new Comparator<OntologyNode>() {
            @Override
            public int compare(OntologyNode o1, OntologyNode o2) {
                return ObjectUtils.compare(o1.getImportOrder(), o2.getImportOrder());

            }
        });
        for (OntologyNode node : ontology.getOntologyNodes()) {
            logger.info("{} : {} ({}); {} [{} - {}]", node.getImportOrder(), node.getDisplayName(), node.getId(), node.getIri(), node.getIntervalStart(), node.getIntervalEnd());
        }
        OntologyNode node0 = nodes.get(0);
        assertEquals("Articulated Skeleton", node0.getDisplayName());
        assertEquals("description", node0.getDescription());
        OntologyNode node1 = nodes.get(1);
        assertEquals("Articulated Skeleton Complete", node1.getDisplayName());
        assertEquals(2, node1.getNumberOfParents());
        assertEquals(node0.getIndex() + "."+ node1.getIntervalStart(), node1.getIndex());
        assertEquals("Articulated Skeleton Nearly Complete", nodes.get(2).getDisplayName());
        assertEquals("another description", nodes.get(2).getDescription());
        assertTrue(nodes.get(2).getSynonyms().contains("ASNC"));
        assertEquals("Articulated Skeleton Partial", nodes.get(3).getDisplayName());
        assertEquals("Articulated Skeleton Anterior Portion", nodes.get(4).getDisplayName());
        assertEquals("Not Recorded", nodes.get(nodes.size() - 1).getDisplayName());
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

    @Test
    public void testValidTextToOwlXml() {
        String ontologyTextInput = "Parent\n\tFirst Child\n\t\tFirst Child's Child1\n\t\tFirst Child's Second Child\n\t\tFirst Child's Child\n"
                + "\tSecond Child\n" + "\tThird Child\n\t\tThird Child's Child\n\t\tThird Child's Child2\n"
                + "\tFourth Child\n\t\tFourth Child's Child\n\t\tFourth Child's Nondegenerate Child\n"
                + "Second Root Parent\n\tSecond Root Parent's Degenerate Child ";
        String owlXml = ontologyService.toOwlXml(237L, ontologyTextInput);
        // FIXME: make assertions on the generated OWL XML.
        assertNotNull(owlXml);
    }

    @Test
    public void testDegenerateTextToOwlXml() {
        String ontologyTextInput = "Parent\n\tFirst Child\n\t\tFirst Child's Child\n\t\tFirst Child's Second Child\n\t\tFirst Child's Child\n"
                + "\tSecond Child\n" + "\tThird Child\n\t\tThird Child's Child\n\t\tThird Child's Child\n"
                + "\tFourth Child\n\t\t\t\tFourth Child's Degenerate Child\n\t\tFourth Child's Nondegenerate Child\n"
                + "Second Root Parent\n\t\tSecond Root Parent's Degenerate Child ";

        try {
            logger.info(ontologyService.toOwlXml(238L, ontologyTextInput));
            fail("Should raise an java.lang.IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException successException) {
        }
    }

}
