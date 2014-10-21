package org.tdar.struts.action.workspace;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
public class IntegrationController extends ActionSupport {

    private static final long serialVersionUID = 0x01;

//    @Actions({
//
//            //list all saved executions for the current user
//            @Action("list"),
//
//            //generate new, transient integration object for editing
//            @Action(value="add", results={
//                    @Result(name="success", location="edit.ftl")
//            }),
//
//            //load existing integration object for editing
//            @Action("edit/{id}"),
//
//            //delete integration
//            //TODO:  this action accepts both GET and POST
//            @Action(value = "delete/{id}", results = {
//                    @Result(name = "postonly", location = "delete-confirm.ftl"),
//                    @Result(name = "success", type = "redirect", location="list")
//            }),
//
//            //find ontologies matching specified filter terms
//            @Action(value = "ajax/find-ontologies", results = {
//                    @Result(name="success", type="jsonresult")
//            }),
//
//            //find datasets matching specified filter terms
//            @Action(value = "ajax/find-datasets", results = {
//                    @Result(name="success", type="jsonresult")
//            }),
//
//            //return full json for specified data table id's
//            @Action(value = "ajax/data-table", results = {
//                    @Result(name="success", type="jsonresult")
//            }),
//
//            //return full json for specified ontology id's
//            @Action(value = "ajax/ontology", results = {
//                    @Result(name="success", type="jsonresult")
//            }),
//
//            //return ontology-node participation information for the specified list of dataTable id's
//            //(similar to data returned to the view in the old '/workspace/filter' action)
//            @Action(value = "ajax/node-value-stats", results = {
//                    @Result(name="success", type="jsonresult")
//            })
//
//            //Actions from old WorkspaceController that we intend to keep:
//            //    @Action(value = "display-filtered-results")
//            //    @Action(value = "download")
//
//
//
//
//
//
//    })

    @Action(value="add", results={
            @Result(name="success", location="edit.ftl")
    })
    public String thisIsMyMethod() {
        return "success";
    }





    /**
     *
     *
     *
     *
     *
     *       No.
     *
     *
     *
     *
     *
     */
    public String getThemeDir() {
        return "includes/themes/tdar/";
    }

    
}























