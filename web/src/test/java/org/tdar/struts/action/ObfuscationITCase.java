/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.ObfuscationService;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.document.DocumentViewAction;

/**
 * @author Adam Brin
 * 
 */
public class ObfuscationITCase extends AbstractIntegrationControllerTestCase {

    @Autowired
    ObfuscationService obfuscationService;


    @Test
    public void testAOPInterceptor() throws TdarActionException {
        DocumentController controller = generateNewInitializedController(DocumentController.class, getAdminUser());
        controller.setId(Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        controller.prepare();
        controller.getProject();
        controller.getId();
        DocumentViewAction rva = generateNewInitializedController(DocumentViewAction.class, getBasicUser());
        rva.setId(Long.parseLong(TestConstants.TEST_DOCUMENT_ID));
        rva.prepare();
        rva.view();
        ((Document) rva.getResource()).getProject();
    }
}
