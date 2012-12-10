package org.tdar.struts.action.resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.ResourceCreatorProxy;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.cc.properties" })
public class PaymentResourceControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    private DocumentController controller;

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    public void initControllerFields() {
        controller.prepare();
        controller.setProjectId(TestConstants.PARENT_PROJECT_ID);
    }

    private ResourceCreatorProxy getNewResourceCreator(String last, String first, String email, Long id, ResourceCreatorRole role) {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        Person p = rcp.getPerson();
        rcp.getPerson().setLastName(last);
        rcp.getPerson().setFirstName(first);
        rcp.getPerson().setEmail(email);
        // id may be null
        rcp.getPerson().setId(id);
        Institution inst = new Institution();
        inst.setName("University of TEST");
        p.setInstitution(inst);
        rcp.setRole(role);
        return rcp;
    }

    public void setController(DocumentController controller) {
        this.controller = controller;
    }

    @Test
    @Rollback()
    public void testSaveWithoutValidAccount() throws Exception {
        Assert.assertTrue(getTdarConfiguration().isPayPerIngestEnabled());
        initControllerFields();

        getLogger().trace("controller:" + controller);
        getLogger().trace("controller.resource:" + controller.getResource());
        controller.getAuthorshipProxies().add(getNewResourceCreator("newLast", "newFirst", "new@email.com", null, ResourceCreatorRole.AUTHOR));

        Document d = controller.getDocument();
        d.setTitle("doc title");
        d.setDescription("desc");
        d.markUpdated(getUser());
        controller.setServletRequest(getServletPostRequest());
        String result = controller.save();
        Long newId = controller.getResource().getId();

        Assert.assertNull(entityService.findByEmail("new@email.com"));
        // now reload the document and see if the institution was saved.
        Assert.assertEquals("resource id should be -1 after unpaid resource addition", newId, Long.valueOf(-1L));
        Assert.assertEquals("controller should not be successful", result, TdarActionSupport.INPUT);
    }

}
