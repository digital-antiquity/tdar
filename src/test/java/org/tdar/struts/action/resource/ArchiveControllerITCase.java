package org.tdar.struts.action.resource;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

/**
 * 
 * @author Martin Paulo
 */
public class ArchiveControllerITCase extends AbstractDataIntegrationTestCase {

    @Test
    public void isSingleFileUploadOnly() {
        ArchiveController controller = generateNewInitializedController(ArchiveController.class);
        assertFalse(controller.isMultipleFileUploadEnabled());
    }
}
