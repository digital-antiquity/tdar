/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.DashboardController;

/**
 * @author Adam Brin
 * 
 */
public class DashboardControllerITCase extends AbstractControllerITCase {


    @Test
    @Rollback
    public void testProjectLists() throws InstantiationException, IllegalAccessException, SolrServerException, IOException {
        // used for setting up project
        Project projectWithDifferentSubmitterAndFullUser = new Project();
        projectWithDifferentSubmitterAndFullUser.setTitle("project with different submitter and user");
        projectWithDifferentSubmitterAndFullUser.setDescription("test");
        projectWithDifferentSubmitterAndFullUser.setStatus(Status.ACTIVE);
        projectWithDifferentSubmitterAndFullUser.markUpdated(getBasicUser());
        TdarUser testPerson = createAndSaveNewUser();

        genericService.save(projectWithDifferentSubmitterAndFullUser);

        addAuthorizedUser(projectWithDifferentSubmitterAndFullUser, testPerson, GeneralPermissions.MODIFY_RECORD);
        // evictCache();

        logger.debug("{internal: {}", projectWithDifferentSubmitterAndFullUser.getAuthorizedUsers());
        Project projectWithSameFullUserAndSubmitter = new Project();
        projectWithSameFullUserAndSubmitter.setTitle("project with same submitter");
        projectWithSameFullUserAndSubmitter.setDescription("test2");
        projectWithSameFullUserAndSubmitter.setStatus(Status.ACTIVE);
        projectWithSameFullUserAndSubmitter.markUpdated(testPerson);
        genericService.save(projectWithSameFullUserAndSubmitter);
        addAuthorizedUser(projectWithSameFullUserAndSubmitter, testPerson, GeneralPermissions.MODIFY_RECORD);
        addAuthorizedUser(projectWithSameFullUserAndSubmitter, getBasicUser(), GeneralPermissions.MODIFY_RECORD);
        DashboardController controller = generateNewInitializedController(DashboardController.class);
        controller.prepare();
        init(controller, testPerson);
        controller.execute();
        Set<Resource> fullUserProjects = controller.getEditableProjects();
        logger.info("{}", fullUserProjects);
        assertEquals(2, fullUserProjects.size());
        assertTrue(fullUserProjects.contains(projectWithDifferentSubmitterAndFullUser));
        assertTrue(fullUserProjects.contains(projectWithSameFullUserAndSubmitter));

        controller = generateNewInitializedController(DashboardController.class);
        controller.prepare();
        init(controller, getAdminUser());
        controller.execute();
        fullUserProjects = controller.getEditableProjects();
        logger.debug("projects: {}", fullUserProjects.size());
        assertTrue(3 < fullUserProjects.size());
    }

}
