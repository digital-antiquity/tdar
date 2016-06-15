package org.tdar.struts.action;

import org.junit.Before;
import org.junit.Test;

public class UploadControllerITCase extends AbstractAdminControllerITCase {

    UploadController controller;

    @Before
    public void setup() {
        controller = generateNewInitializedController(UploadController.class);
    }

    @Test
    public void listUploadedFiles() throws Exception {
        controller.setInformationResourceId(4230L);
    }

}
