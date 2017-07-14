package org.tdar.struts.action.collection;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.CollectionRightsComparator;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.browse.BrowseCollectionController;
import org.tdar.struts.action.collection.admin.CollectionBatchAction;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts.action.project.ProjectController;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;
import org.tdar.struts.action.resource.ResourceDeleteAction;
import org.tdar.struts.action.resource.ResourceRightsController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.opensymphony.xwork2.Action;

public class CollectionBatchActionITCase extends AbstractResourceControllerITCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private EntityService entityService;

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    @Autowired
    private ResourceCollectionService resourceCollectionService;


    static int indexCount = 0;
    
    /**Associate all resources to the collection**/
    //create a shared collection
    	//create 2 project 
    	//create resources for at least one project
    	//create a resource with no project 
    
    //instatiate a collection batch action 
    
    //Change batch title, and see if the collection title inherits the change.
    //verify the resources for the project don't become disassociated 
    

    @SuppressWarnings("unused")
    @Test
    @Rollback(true)
    public void testBatchTitleEdit() throws Exception {
        String name 	   	 	   = "test collection";
        String description 		   = "test description";
        String existingProjectName = "Existing Project";
        String newProjectName 	   = "New Project";
        
        InformationResource normal = generateDocumentAndUseDefaultUser();
        InformationResource draft  = generateDocumentAndUseDefaultUser();
        Project existingProject    = createAndSaveNewProject(existingProjectName);
        Project newProject 		   = createAndSaveNewProject(newProjectName);
        
        final Long normalId = normal.getId();
        final Long draftId  = draft.getId();
        final Long existingProjectId = existingProject.getId();
        
        draft.setProject(existingProject);
        draft.setStatus(Status.DRAFT);

        normal.setProject(newProject);
        final Long newProjectId = newProject.getId();

        genericService.saveOrUpdate(draft);
        genericService.saveOrUpdate(normal);

        
        List<AuthorizedUser> users = new ArrayList<>(Arrays.asList(
                new AuthorizedUser(getAdminUser(),getBasicUser(), GeneralPermissions.ADMINISTER_SHARE),
                new AuthorizedUser(getAdminUser(),getAdminUser(), GeneralPermissions.MODIFY_RECORD)));
        
        List<Resource> resources = new ArrayList<Resource>(Arrays.asList(normal, draft, newProject, existingProject));

        SharedCollection collection = generateResourceCollection(name, description, false, users, resources, null);

        final Long collectionId = collection.getId();
        String slug   = collection.getSlug();
        
        collection = null;
        CollectionBatchAction controller = generateNewInitializedController(CollectionBatchAction.class, getAdminUser());
        

       for(Resource resource : resources){
            try {
            	controller.getIds().add(resource.getId());
            	controller.getTitles().add(resource.getTitle());
            	controller.getDescriptions().add(resource.getDescription());
            	controller.getResources().add(resource);
            	controller.getDates().add(-1);
            } catch (Exception e) {
            }
        }
        
        controller.setId(collectionId);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        
        resources = null;
        normal 	  = null;
        draft	  = null;
        
        controller.save();
        
        genericService.synchronize();
        
        draft = genericService.find(InformationResource.class, draftId);
        normal = genericService.find(InformationResource.class, normalId);
        
        assertEquals(draft.getProjectId(), existingProjectId);
        assertEquals(normal.getProjectId(), newProjectId);
    }
}
