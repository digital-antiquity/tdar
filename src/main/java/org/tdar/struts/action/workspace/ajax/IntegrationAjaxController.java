package org.tdar.struts.action.workspace.ajax;

import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts.action.AuthenticationAware;

import java.util.ArrayList;

/**
 * Created by jimdevos on 10/28/14.
 */
@ParentPackage("secured")
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
            @Result(name="success", type="jsonresult")
    })
    public String findDatasets() {
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
}
