package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.project.ProjectController;

public class ProjectControllerResourceAnnotationITCase extends AbstractResourceControllerITCase {

    @Test
    @Rollback
    public void testAddSingleAnnotation() throws Exception {
        loadAddSingle();
    }

    private ProjectController loadAddSingle() throws TdarActionException {
        ProjectController controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        controller.prepare();
        controller.add();
        Project p = controller.getProject();
        Assert.assertNotNull(p);
        p.setTitle("project with annotations");
        p.setDescription("description goes here");
        p.markUpdated(getUser());

        String expectedKey = "annotation key";
        String expectedValue = "annotation value";
        ResourceAnnotationType expectedType = ResourceAnnotationType.IDENTIFIER;

        // simulating save action from the project/add page
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey(expectedKey);
        key.setResourceAnnotationType(expectedType);
        ResourceAnnotation annotation = new ResourceAnnotation();
        annotation.setValue(expectedValue);
        annotation.setResourceAnnotationKey(key);
        controller.getResourceAnnotations().add(annotation);
        Long originalId = p.getId();
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        controller.save();
        Long newId = controller.getResource().getId();
        Assert.assertNotSame("resource id should be assigned after insert", originalId, newId);

        // simulating the view action - the resource should have one annotation
        genericService.detachFromSession(controller.getResource());
        controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        controller.setId(newId);
        controller.prepare();
        controller.edit();
        p = controller.getResource();
        Assert.assertEquals("expecting one and only one annotation", 1, p.getResourceAnnotations().size());
        ResourceAnnotation actualAnnotation = (ResourceAnnotation) p.getResourceAnnotations().toArray()[0];
        Assert.assertEquals("checking annotation key", expectedKey, actualAnnotation.getResourceAnnotationKey().getKey());
        Assert.assertEquals("checking annotation type", expectedType, actualAnnotation.getResourceAnnotationKey().getResourceAnnotationType());
        Assert.assertEquals("checking annotation value", expectedValue, actualAnnotation.getValue());
        return controller;
    }

    @Test
    @Rollback
    // try to delete the single annotation added by testAddSingleAnnotation
    public void testDeleteFromSingleAnnotation() throws Exception {
        ProjectController controller = loadAddSingle();
        // meta test ... does @rollback apply if we're calling a test from another test??
        Long id = controller.getResource().getId();
        controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        controller.setId(id);
        controller.prepare();
        controller.edit();

        Assert.assertEquals("expecting a single annotation in project after completing test", 1, controller.getResource().getResourceAnnotations().size());
        // simulate the edit action (really no different than the view action, from controller perspective)

        // simulate the save action - wiping out all of the
        controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        flush();
        logger.debug("{}", genericService.find(Project.class, id).isObfuscated());
        controller.setId(id);
        controller.prepare();
        controller.setResourceAnnotations(Collections.<ResourceAnnotation> emptyList());
        controller.setServletRequest(getServletPostRequest());
        controller.setAsync(false);
        controller.save();
        Project project = genericService.find(Project.class, id);
        Assert.assertEquals("annotations list should be empty", 0, project.getResourceAnnotations().size());

        // now back to the view action - we should have an empty list of resourceAnnotations
        controller = generateNewInitializedController(ProjectController.class);
        controller.setId(id);
        controller.prepare();
        controller.edit();
        Assert.assertEquals("annotations list should be empty", 0, controller.getResourceAnnotations().size());
    }

    private ResourceAnnotation createAnnotation(String key, String val) {
        ResourceAnnotationKey rak = new ResourceAnnotationKey();
        ResourceAnnotation ra = new ResourceAnnotation();
        rak.setKey(key);
        ra.setResourceAnnotationKey(rak);
        ra.setValue(val);
        return ra;
    }

    @SuppressWarnings("unused")
    private List<ResourceAnnotation> createAnnotations(Map<String, String> valMap) {
        List<ResourceAnnotation> list = new ArrayList<ResourceAnnotation>();
        for (Map.Entry<String, String> entry : valMap.entrySet()) {
            list.add(createAnnotation(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    @Test
    @Rollback
    public void testAdd3ThenDeleteMiddle() throws Exception {
        ProjectController controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        controller.prepare();
        controller.add();
        Project p = controller.getProject();
        Assert.assertNotNull(p);
        p.setTitle("project with annotations");
        p.setDescription("description goes here");
        p.markUpdated(getUser());

        List<ResourceAnnotation> list1 = new ArrayList<ResourceAnnotation>();
        List<ResourceAnnotation> list2 = new ArrayList<ResourceAnnotation>();
        // on the create page we will add three keys
        list1.add(createAnnotation("key1", "val1"));
        list1.add(createAnnotation("key2", "val2"));
        list1.add(createAnnotation("key3", "val3"));
        list1.add(createAnnotation("invalidKey1", ""));

        // on the edit page we will delete one key
        list2.add(createAnnotation("key1", "val1"));
        list2.add(null);
        list2.add(createAnnotation("key3", "val3"));

        controller.getResourceAnnotations().addAll(list1);
        controller.setAsync(false);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = controller.getResource().getId();

        // go to the view page

        // go back to the edit page
        genericService.detachFromSession(controller.getResource());
        controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        controller.setAsync(false);
        controller.setId(id);
        controller.prepare();
        Assert.assertEquals("the fourth annotation was incomplete and should not have saved", 3, controller.getProject().getResourceAnnotations().size());
        controller.setResourceAnnotations(list2);
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        Project project = genericService.find(Project.class, id);
        Assert.assertEquals("we should only have 2 annotations now", 2, project.getResourceAnnotations().size());

        // back to the view page
        controller = generateNewInitializedController(ProjectController.class, getBasicUser());
        controller.setId(id);
        controller.prepare();
        controller.edit();
        Assert.assertEquals("we should only have annotations now", 2, controller.getResourceAnnotations().size());
    }

}
