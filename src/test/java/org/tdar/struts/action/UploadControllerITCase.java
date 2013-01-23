package org.tdar.struts.action;

import org.junit.Before;
import org.junit.Test;
import org.tdar.struts.action.entity.InstitutionController;

public class UploadControllerITCase  extends AbstractAdminControllerITCase {

    UploadController controller;
    
    
    @Override
    protected TdarActionSupport getController() {
        return controller;
    }
    
    @Before
    public void setup() {
        controller = generateNewInitializedController(UploadController.class);
    }
    
    
    
    
    @Test
    public void listUploadedFiles() throws Exception{
        controller.setInformationResourceId(4230L);
        controller.listUploadedFiles();
    }

}
