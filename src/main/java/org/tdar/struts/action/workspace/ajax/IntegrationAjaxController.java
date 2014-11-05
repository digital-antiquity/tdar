package org.tdar.struts.action.workspace.ajax;

import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts.action.AuthenticationAware;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by jimdevos on 10/28/14.
 */
@ParentPackage("default")  //FIXME: change to secured package
@Namespace("/workspace/ajax")
@Component
@Scope("prototype")
public class IntegrationAjaxController  extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = 0x01;
    private IntegrationSearchFilter filter = new IntegrationSearchFilter();

    @Override
    public void prepare() {
    }

    @Action(value = "find-datasets", results={
            @Result(name="success", type="stream",
                    params = {
                            "contentType", "application/json",
                            "inputName", "datasetSearchJsonInputStream"
                    }        )
    })
    public String findDatasets() {
        return "success";
    }


    @Action(value = "find-ontologies", results={
            @Result(name="success", type="stream",
                    params = {
                    "contentType", "application/json",
                    "inputName", "ontologySearchJsonInputStream"
            }        )
    })
    public String findOntologies() {
        return "success";
    }

    @Action(value = "table-details", results={
        @Result(name="success", type="stream",
                params = {
                        "contentType", "application/json",
                        "inputName", "tableDetailsJsonInputStream"
                }        )
    })
    public String dataTableDetails() {
        return "success";
    }

    @Action(value = "ontology-details", results={
            @Result(name="success", type="stream",
                    params = {
                            "contentType", "application/json",
                            "inputName", "ontologyDetailsJsonInputStream"
                    }        )
    })
    public String ontologyDetails() {
        return "success";
    }



    public IntegrationSearchFilter getFilter() {
        return filter;
    }


    //FIXME: replace placeholder data
    public Object getJsonResult() {
        return new ArrayList<String>() {{
            add("Dataset 1");
            add("Dataset 2");
            add("Dataset 3");
        }};
    }

    //FIXME: replace placeholder data
    public InputStream getTableDetailsJsonInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/get-table-details.json");
    }

    //FIXME: replace placeholder data
    public InputStream getOntologyDetailsJsonInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/get-ontology-details.json");
    }

    //FIXME: replace placeholder data
    public InputStream getDatasetSearchJsonInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/search-datasets.json");
    }

    //FIXME: replace placeholder data
    public InputStream getOntologySearchJsonInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/search-ontologies.json");
    }



}
