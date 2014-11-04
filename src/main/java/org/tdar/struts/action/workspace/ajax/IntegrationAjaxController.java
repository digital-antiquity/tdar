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
@ParentPackage("default")
@Namespace("/workspace/ajax")
@Component
@Scope("prototype")
public class IntegrationAjaxController  extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = 0x01;
    private IntegrationSearchFilter filter = new IntegrationSearchFilter();
    private InputStream inputStream;

    @Override
    public void prepare() {
//        inputStream = getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/get-table-details.json");
        getLogger().debug("inputstream is null: {}",  inputStream == null);
    }

    @Action(value = "find-datasets", results={
            @Result(name="success", type="jsonresult")
    })
    public String findDatasets() {
        return "success";
    }

    @Action(value = "table-details", results={
        @Result(name="success", type="stream",
                params = {
                        "contentType", "application/json",
                        "inputName", "inputStream"
                }        )
    })
    public String dataTableDetails() {
        return "success";
    }

    public IntegrationSearchFilter getFilter() {
        return filter;
    }

    public Object getJsonResult() {
        return new ArrayList<String>() {{
            add("Dataset 1");
            add("Dataset 2");
            add("Dataset 3");
        }};
    }

    public InputStream getInputStream() {
        return getClass().getClassLoader().getResourceAsStream("integration-ajax-samples/get-table-details.json");
    }


}
