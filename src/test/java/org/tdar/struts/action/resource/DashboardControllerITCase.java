/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.DashboardController;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class DashboardControllerITCase extends AbstractResourceControllerITCase {

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
    @Rollback
    public void testProjectLists() throws InstantiationException, IllegalAccessException {
        // used for setting up project
        Project projectWithDifferentSubmitterAndFullUser = new Project();
        projectWithDifferentSubmitterAndFullUser.setTitle("test");
        projectWithDifferentSubmitterAndFullUser.setStatus(Status.ACTIVE);
        projectWithDifferentSubmitterAndFullUser.markUpdated(getBasicUser());
        Person testPerson = createAndSaveNewPerson();

        genericService.save(projectWithDifferentSubmitterAndFullUser);

        addAuthorizedUser(projectWithDifferentSubmitterAndFullUser, testPerson, GeneralPermissions.MODIFY_RECORD);

        Project projectWithSameFullUserAndSubmitter = new Project();
        projectWithSameFullUserAndSubmitter.setTitle("test2");
        projectWithSameFullUserAndSubmitter.setStatus(Status.ACTIVE);
        projectWithSameFullUserAndSubmitter.markUpdated(testPerson);
        genericService.save(projectWithSameFullUserAndSubmitter);
        addAuthorizedUser(projectWithSameFullUserAndSubmitter, testPerson, GeneralPermissions.MODIFY_RECORD);
        addAuthorizedUser(projectWithSameFullUserAndSubmitter, getBasicUser(), GeneralPermissions.MODIFY_RECORD);

        DashboardController controller = generateNewInitializedController(DashboardController.class);
        controller.prepare();
        init(controller, testPerson);
        Set<Resource> fullUserProjects = controller.getEditableProjects();
        logger.info("{}", fullUserProjects);
        assertEquals(2, fullUserProjects.size());
        assertTrue(fullUserProjects.contains(projectWithDifferentSubmitterAndFullUser));
        assertTrue(fullUserProjects.contains(projectWithSameFullUserAndSubmitter));

        controller = generateNewInitializedController(DashboardController.class);
        controller.prepare();
        init(controller, getAdminUser());
        fullUserProjects = controller.getEditableProjects();
        assertTrue(3 < fullUserProjects.size());
    }

}
