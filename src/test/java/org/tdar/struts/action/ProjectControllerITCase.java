package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.utils.entity.ResourceCreatorProxy;

public class ProjectControllerITCase extends AbstractAdminControllerITCase {

    @Test
    @Rollback
    public void testProjectResourceCreator() {
        Person test1 = new Person("test", "person", "");
        Person test5 = new Person("test", "person", null);
        Person test2 = new Person("test", "person", "test@test.com");
        Person test3 = new Person("test", "person", "");
        Person test6 = new Person("Test", "Person", "");
        Institution inst = new Institution("da");
        test3.setInstitution(inst);
        Person test4 = new Person("test", "person", "test@test.com");
        test4.setInstitution(inst);
        ProjectController controller = generateNewInitializedController(ProjectController.class);
        controller.prepare();
        List<ResourceCreatorProxy> creditProxies = new ArrayList<ResourceCreatorProxy>();
        creditProxies.add(new ResourceCreatorProxy(test1,ResourceCreatorRole.CONTACT)); // simple person, no institution
        creditProxies.add(new ResourceCreatorProxy(test3,ResourceCreatorRole.CONTACT)); // person1 with institution
        creditProxies.add(new ResourceCreatorProxy(test4,ResourceCreatorRole.CONTACT)); // person1 but with email & institution
        creditProxies.add(new ResourceCreatorProxy(test5,ResourceCreatorRole.CONTACT)); // person1 but with a null email instead of blank
        creditProxies.add(new ResourceCreatorProxy(test2,ResourceCreatorRole.CONTACT)); // person1 with email, no institution
        creditProxies.add(new ResourceCreatorProxy(test6,ResourceCreatorRole.CONTACT)); // person1 with case differences
        controller.setCreditProxies(creditProxies);
        controller.getProject().setTitle("test");
        controller.getProject().setDescription("test");
        controller.save();
        Project project = controller.getProject();
        project = genericService.merge(project);
        List<Creator> people = new ArrayList<Creator>();
        for (ResourceCreator creator : project.getResourceCreators()) {
            logger.info(creator + " " + creator.getCreator().getId());
            people.add(creator.getCreator());
        }
        assertEquals("First and Second person should be the same (difference institution)", people.get(0), people.get(1));
        assertEquals("First and Sixth person should be the same (difference case)", people.get(0), people.get(5));
        assertEquals("First and Fourth person should be the same (email null)", people.get(0), people.get(1));
        assertFalse("First and Third should not be the same as there's an email", people.get(0).equals(people.get(2)));
        assertEquals("Third and Fifth should be the same as there's the same email but one has institution", people.get(2), people.get(4));
    }

    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Test
    @Rollback
    public void testProjectLists() throws InstantiationException, IllegalAccessException {
        @SuppressWarnings("unused") // used for setting up project
        Document doc = createAndSaveNewInformationResource(Document.class);
        Project projectWithDifferentSubmitterAndFullUser = new Project();
        projectWithDifferentSubmitterAndFullUser.setTitle("test");
        projectWithDifferentSubmitterAndFullUser.setStatus(Status.ACTIVE);
        projectWithDifferentSubmitterAndFullUser.markUpdated(getTestPerson());
        logger.info(getUser());
        projectWithDifferentSubmitterAndFullUser.setFullUsers(new HashSet<FullUser>(Arrays.asList(new FullUser(projectWithDifferentSubmitterAndFullUser,
                getUser()), new FullUser(projectWithDifferentSubmitterAndFullUser, getUser()))));
        Project projectWithSameFullUserAndSubmitter = new Project();
        projectWithSameFullUserAndSubmitter.setTitle("test2");
        projectWithSameFullUserAndSubmitter.setStatus(Status.ACTIVE);
        projectWithSameFullUserAndSubmitter.markUpdated(getUser());
        projectWithSameFullUserAndSubmitter.setFullUsers(new HashSet<FullUser>(Arrays.asList(new FullUser(projectWithDifferentSubmitterAndFullUser, getUser()),
                new FullUser(projectWithDifferentSubmitterAndFullUser, getUser()))));

        genericService.save(projectWithDifferentSubmitterAndFullUser);
        genericService.save(projectWithDifferentSubmitterAndFullUser.getFullUsers());
        genericService.save(projectWithSameFullUserAndSubmitter);
        genericService.save(projectWithSameFullUserAndSubmitter.getFullUsers());

        ProjectController controller = generateNewInitializedController(ProjectController.class);
        controller.prepare();
        List<Project> fullUserProjects = controller.getFullUserProjects();
        List<Project> allSubmittedProjects = controller.getAllSubmittedProjects();

        for (Project proj : allSubmittedProjects) {
            assertFalse(fullUserProjects.contains(proj));
        }

        HashSet<Project> uniqueProjects = new HashSet<Project>(fullUserProjects);
        assertEquals("checking uniqueness", uniqueProjects.size(), fullUserProjects.size());

        for (Project proj : fullUserProjects) {
            assertFalse(allSubmittedProjects.contains(proj));
        }
    }

}
