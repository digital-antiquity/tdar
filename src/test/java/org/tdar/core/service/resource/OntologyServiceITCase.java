/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.action.resource.OntologyController;

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
