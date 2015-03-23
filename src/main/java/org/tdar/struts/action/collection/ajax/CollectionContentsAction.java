package org.tdar.struts.action.collection.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Struts action that provides a paginiated viewinto a list of ResourceCollection resources.
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection/ajax")
public class CollectionContentsAction extends ActionSupport{

    @Action("list")
    public String execute() {
        return SUCCESS;
    }
}
