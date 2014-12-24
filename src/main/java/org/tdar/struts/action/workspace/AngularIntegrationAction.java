package org.tdar.struts.action.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.struts.action.AuthenticationAware;

import com.opensymphony.xwork2.Preparable;

/**
 *
 * @author jim
 */
@ParentPackage("secured")
@Namespace("/workspace")
@Component
@Scope("prototype")
public class AngularIntegrationAction extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -2356381511354062946L;

    private Object categoryListJsonObject;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient ProjectService projectService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;


    private List<Resource> fullUserProjects = new ArrayList<>();
    private Collection<ResourceCollection> allResourceCollections = new ArrayList<>();

    @Override
    public void prepare() {
        prepareCategories();
        prepareProjectStuff();
        prepareCollections();
    }

    @Actions({
            @Action(value = "add", results = {
                    @Result(name = "success", location = "edit.ftl")
            }),
            @Action(value = "integrate", results = {
                    @Result(name = "success", location = "ng-integrate.ftl")
            })
    })
    public String execute() {
        return "success";
    }

    private void prepareCategories() {
        // We really just need a flattened list of categories (client will build the hierarchy).
        // FIXME: make more efficient by ensuring we never lazy-init the parent associations (n+1 select)
        List<CategoryVariable> cats = genericService.findAll(CategoryVariable.class);
        Map<Long, String> parentNames = new HashMap<>();
        List<Map<String, Object>> subcats = new ArrayList<>();

        // pass 1: build the parentNames (we assume depth of 2 with no childless parents)
        for (CategoryVariable cat : cats) {
            if (cat.getParent() == null) {
                parentNames.put(cat.getId(), cat.getName());
            }
        }

        // pass 2: list of subcat maps
        for (CategoryVariable cat : cats) {
            if (cat.getParent() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cat.getId());
                map.put("name", cat.getName());
                map.put("parent_name", parentNames.get(cat.getParent().getId()));
                subcats.add(map);
            }
        }

        categoryListJsonObject = subcats;
    }

    public String getCategoriesJson() {
        String json = "[]";
        try {
            json = serializationService.convertToJson(categoryListJsonObject);
        } catch (IOException e) {
            addActionError(e.getMessage());
        }
        return json;
    }



     String getJson(Object obj) {
         String json = "[]";
         try {
             json = serializationService.convertToJson(obj);
         } catch (IOException e) {
             addActionError(e.getMessage());
         }
         return json;
     }

    public List<Resource> getFullUserProjects() {
        return fullUserProjects;
    }

    public String getFullUserProjectsJson() {
        return getJson(fullUserProjects);
    }

    public String getAllResourceCollectionsJson() {
        return getJson(allResourceCollections);
    }

    private void prepareProjectStuff() {
        // FIXME: isAdmin should not be an argument here; ProjectService can derive isAdmin by tapping authservice.
        fullUserProjects = new ArrayList<>(projectService.findSparseTitleIdProjectListByPerson(getAuthenticatedUser(), false));
        Collections.sort(fullUserProjects);
    }
    private void prepareCollections() {
        // For now, you just get flattened list of collections. Because.
        allResourceCollections.addAll(resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser()));
    }
    
    public String getNgApplicationName() {
        return "integrationApp";
    }
}
