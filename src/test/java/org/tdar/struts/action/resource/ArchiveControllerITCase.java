package org.tdar.struts.action.resource;


import org.junit.Before;
import org.junit.Test;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;


import static org.junit.Assert.*;

/**
 * 
 * @author Martin Paulo
 */
public class ArchiveControllerITCase extends AbstractDataIntegrationTestCase {
    
    private ArchiveController controller;

    @Before
    public void setUp() {
        controller = generateNewInitializedController(ArchiveController.class);
    }

    @Test
    public void isSingleFileUploadOnly() {
       assertFalse(controller.isMultipleFileUploadEnabled());
       assertFalse(controller.supportsMultipleFileUpload());
    }
}
