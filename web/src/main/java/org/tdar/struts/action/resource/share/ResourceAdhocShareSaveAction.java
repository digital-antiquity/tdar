package org.tdar.struts.action.resource.share;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

/**
 * Created by jimdevos on 3/6/17.
 */

public class ResourceAdhocShareSaveAction extends AbstractResourceAdhocShareAction {


    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        getAdhocShare().setCollectionId(getPersistable().getInternalResourceCollection().getId());

    }

    @WriteableSession
    @Override
    @Action(value="save", results={
            @Result(name="success", type="redirect", location="/collection/${id}"),
            @Result(name="input", location="edit.ftl")
    })
    @PostOnly
    @Validations(
            emails = { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "adhocShare.email", key = "adhocShareCreateAction.email_required") }
    )
    public String execute() {
        // FIXME: what is the appropriate way to create an invite to edit a resource?  Do I pass a list of one resource to the "resources" parameter, or simply specify the internal collection of that resource?
        getResourceCollectionService().createShareFromAdhoc(getAdhocShare(), null, getPersistable().getInternalResourceCollection(), null, getAuthenticatedUser());
        return "success";
    }




}
