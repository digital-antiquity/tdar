/*
 * Copyright 2014 The Digital Archaeological Record.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tdar.struts.action.workspace;

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
public class IntegrationController {

    @Actions({

            //list all saved executions for the current user
            @Action("list"),

            //generate new, transient integration object for editing
            @Action(value="add", results={
                    @Result(name="success", location="edit.ftl")
            }),

            //load existing integration object for editing
            @Action("edit/{id}"),

            //delete integration
            //TODO:  this action accepts both GET and POST
            @Action(value = "delete/{id}", results = {
                    @Result(name = "postonly", location = "delete-confirm.ftl"),
                    @Result(name = "success", type = "redirect", location="list")
            }),

            //find ontologies matching specified filter terms
            @Action(value = "ajax/find-ontologies", results = {
                    @Result(name="success", type="jsonresult")
            }),

            //find datasets matching specified filter terms
            @Action(value = "ajax/find-datasets", results = {
                    @Result(name="success", type="jsonresult")
            }),

            //return full json for specified data table id's
            @Action(value = "ajax/data-table", results = {
                    @Result(name="success", type="jsonresult")
            }),

            //return full json for specified ontology id's
            @Action(value = "ajax/ontology", results = {
                    @Result(name="success", type="jsonresult")
            }),

            //return ontology-node participation information for the specified list of dataTable id's
            //(similar to data returned to the view in the old '/workspace/filter' action)
            @Action(value = "ajax/node-value-stats", results = {
                    @Result(name="success", type="jsonresult")
            })

            //Actions from old WorkspaceController that we intend to keep:
            //    @Action(value = "display-filtered-results")
            //    @Action(value = "download")






    })
    public String execute() {
        return "success";
    }



    
}
