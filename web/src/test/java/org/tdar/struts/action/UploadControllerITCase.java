package org.tdar.struts.action;

import org.junit.Before;
import org.junit.Test;
import org.tdar.struts.action.upload.UploadAction;

public class UploadControllerITCase extends AbstractAdminControllerITCase {

    UploadAction controller;

    @Before
    public void setup() {
        controller = generateNewInitializedController(UploadAction.class);
    }

    @Test
    public void listUploadedFiles() throws Exception {
        controller.setInformationResourceId(4230L);
    }

}
