package org.tdar.struts.action.collection.share;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts.action.ManageAction;

@ParentPackage("secured")
@Namespace("/collection/share")
@Component
@Scope("prototype")
public class CollectionAdhocShareEditAction extends ManageAction {


    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public void validate() {
        // TODO: figure out how to handle unauthorized request
        //
        super.validate();
    }

    @Action(value="edit", results={
            @Result(name="success", location="edit.ftl")
    })
    public  String execute() {
        return "success";
    }


}
