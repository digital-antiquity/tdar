package org.tdar.struts.action.workspace;

import java.io.IOException;
import java.util.ArrayList;
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
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.external.AuthorizationService;
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
/**
 * Don't get used to this class, kid.  It's only here as a placeholder to define the overall workflow.
 * Expect that the actual action methods will be recklessly scattered across a myriad of obtusely-named action classes. YOU HAVE
 * BEEN WARNED.
 */
public class IntegrationController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -2356381511354062946L;

    private Object categoryListJsonObject;

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private XmlService xmlService;
    @Autowired
    private GenericService genericService;

    // @Actions({
    //
    // //list all saved executions for the current user
    // @Action("list"),
    //
    // //generate new, transient integration object for editing
    // @Action(value="add", results={
    // @Result(name="success", location="edit.ftl")
    // }),
    //
    // //load existing integration object for editing
    // @Action("edit/{id}"),
    //
    // //delete integration
    // //TODO: this action accepts both GET and POST
    // @Action(value = "delete/{id}", results = {
    // @Result(name = "postonly", location = "delete-confirm.ftl"),
    // @Result(name = "success", type = "redirect", location="list")
    // }),
    //
    // //find ontologies matching specified filter terms
    // @Action(value = "ajax/find-ontologies", results = {
    // @Result(name="success", type="jsonresult")
    // }),
    //
    // //find datasets matching specified filter terms
    // @Action(value = "ajax/find-datasets", results = {
    // @Result(name="success", type="jsonresult")
    // }),
    //
    // //return full json for specified data table id's
    // @Action(value = "ajax/data-table", results = {
    // @Result(name="success", type="jsonresult")
    // }),
    //
    // //return full json for specified ontology id's
    // @Action(value = "ajax/ontology", results = {
    // @Result(name="success", type="jsonresult")
    // }),
    //
    // //return ontology-node participation information for the specified list of dataTable id's
    // //(similar to data returned to the view in the old '/workspace/filter' action)
    // @Action(value = "ajax/node-value-stats", results = {
    // @Result(name="success", type="jsonresult")
    // })
    //
    // //Actions from old WorkspaceController that we intend to keep:
    // // @Action(value = "display-filtered-results")
    // // @Action(value = "download")
    //
    //
    //
    //
    //
    //
    // })

    @Override
    public void prepare() {
        prepareCategories();
    }

    @Actions({
            @Action(value = "add", results = {
                    @Result(name = "success", location = "edit.ftl")
            }),
            @Action(value = "add-angular", results = {
                    @Result(name = "success", location = "edit-angular.ftl")
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
            json = xmlService.convertToJson(categoryListJsonObject);
        } catch (IOException e) {
            addActionError(e.getMessage());
        }
        return json;
    }

}
