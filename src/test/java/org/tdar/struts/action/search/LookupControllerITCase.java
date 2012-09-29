package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.ProjectController;


public class LookupControllerITCase extends AbstractIntegrationTestCase {

    @Autowired
    private LookupController controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(99);
    }
    
    
    @Test
    public void testResourceLookupByType() {
        searchIndexService.indexAll(Resource.class);
        // get back all documents
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT));
        controller.lookupResource();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() {
        searchIndexService.indexAll(Resource.class);
        controller.setProjectId(3073L);
        controller.lookupResource();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testKeywordLookup() {
        searchIndexService.indexAll(GeographicKeyword.class, CultureKeyword.class);
        controller.setKeywordType("culturekeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testAnnotationLookup() {
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("ISSN");
        key.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key);
        ResourceAnnotationKey key2 = new ResourceAnnotationKey();
        key2.setKey("ISBN");
        key2.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key2);

        searchIndexService.indexAll(ResourceAnnotationKey.class);
        controller.setTerm("IS");
        controller.lookupAnnotationKey();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        controller.setTerm("ZZ");
        controller.lookupAnnotationKey();
        resources = controller.getResults();
        assertEquals("ZZ should return no results", 0, resources.size());
    }

    @Test
    @Rollback(value=true)
    public void testDeletedResourceFilteredForNonAdmins() throws Exception {
        initControllerForDashboard();

        searchIndexService.indexAll(Resource.class);
        controller.setUseSubmitterContext(true);
        Project proj  = createProjectViaProjectController("project to be deleted");

        searchIndexService.index(proj);

        controller.lookupResource();
        assertTrue(controller.getResults().contains(proj));
        
        //now delete the resource and makes sure it doesn't show up for the common rabble
        logger.debug("result contents before delete: {}", controller.getResults());
        proj.setStatus(Status.DELETED);
        genericService.saveOrUpdate(proj);
        initController();
        controller.setUseSubmitterContext(true);
        
        searchIndexService.index(proj);
        controller.lookupResource();
        logger.debug("result contents after delete: {}", controller.getResults());
        assertFalse(controller.getResults().contains(proj));

        //now pretend that it's an admin visiting the dashboard.  Even though they can view/edit everything, deleted items 
        //won't show up in their dashboard unless they are the submitter or have explicitly been given access rights, so we update the project's submitter
        proj.setSubmitter(getAdminUser());
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);
        controller = generateNewController(LookupController.class);
        init(controller, getAdminUser());
        controller.setUseSubmitterContext(false); 
        controller.setIncludedStatuses(new ArrayList<Status>(Arrays.asList(Status.values())));
        controller.setRecordsPerPage(10000000);
        controller.lookupResource();
        assertTrue(controller.getResults().contains(proj));
    }
    
    //setup the controller similar to how struts will do it when receiving an ajax call from the dashboard
    private void initControllerForDashboard() {
        controller.getResourceTypes().add(null);
        controller.getIncludedStatuses().add(null);
        controller.setSortField(SortOption.RELEVANCE);
        controller.setTerm(null);
        controller.setProjectId((String)null);
        controller.setCollectionId(null);
    }    
    

    //more accurately model how struts will create a project by having the controller do it
    private Project createProjectViaProjectController(String title) throws TdarActionException {
        ProjectController projectController = generateNewInitializedController(ProjectController.class);
        projectController.prepare();
        Project project = projectController.getProject();
        project.setTitle(title);
        project.setDescription(title);
        projectController.save();
        Assert.assertNotNull(project.getId());
        assertTrue(project.getId() != -1L);
        return project;
    }

    
    // TODO: need filtered test (e.g. only ontologies in a certain project)

    public void initControllerFields() {
        searchIndexService.indexAll();
        List<String> types = new ArrayList<String>();
        types.add("DOCUMENT");
        types.add("ONTOLOGY");
        types.add("CODING_SHEET");
        types.add("IMAGE");
        types.add("DATASET");
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT,
                ResourceType.ONTOLOGY, ResourceType.IMAGE, ResourceType.DATASET,
                ResourceType.CODING_SHEET));
    }

    @Test
    public void testResourceLookup() {
        initControllerFields();
        controller.setTitle("HARP");
        controller.lookupResource();

        JSONArray jsonArray = new JSONArray();
        for (Indexable persistable : controller.getResults()) {
            if (persistable instanceof JsonModel) {
                jsonArray.add(((JsonModel) persistable).toJSON());
            }
        }
        String json = jsonArray.toString();
        logger.debug("resourceLookup results:{}", json);
        // assertTrue(json.contains("iTotalRecords"));
        assertTrue(json.contains("HARP"));
    }

    @Test
    @Rollback(value=true)
    public void testAdminDashboardAnyStatus() throws Exception{
        //have a regular user create a document in each status (except deleted)that should be visible when an admin looks for document with "any" status
        Document activeDoc = createAndSaveNewInformationResource(Document.class, getUser(), "testActiveDoc");
        activeDoc.setStatus(Status.ACTIVE); //probably unnecessary
        Document draftDoc = createAndSaveNewInformationResource(Document.class, getUser(), "testDraftDoc");
        draftDoc.setStatus(Status.DRAFT);
        Document flaggedDoc = createAndSaveNewInformationResource(Document.class, getUser(), "testFlaggedaDoc");
        flaggedDoc.setStatus(Status.FLAGGED);
        List<Document> docs = Arrays.asList(activeDoc, draftDoc, flaggedDoc);
        entityService.saveOrUpdateAll(docs);
        searchIndexService.indexAll(Resource.class);
        
        //login as an admin
        controller = generateNewController(LookupController.class);
        init(controller, getAdminUser());
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        for(Document doc : docs) {
            controller.setTitle(doc.getTitle());
            controller.setIncludedStatuses(Collections.<Status>emptyList());
            controller.lookupResource();
            assertTrue(String.format("looking for '%s' when filtering by %s", doc, controller.getIncludedStatuses()), controller.getResults().contains(doc));
        }
        
    }
    
    
}
