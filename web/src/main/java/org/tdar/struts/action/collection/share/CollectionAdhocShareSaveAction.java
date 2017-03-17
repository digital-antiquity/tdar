package org.tdar.struts.action.collection.share;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

/**
 *  We extend APVA because it performes load and checkValidRequest(),  we don't need anything else it offers (in
 *  fact adds things we *dont* want, but we'll worry about that later).
 */
// fixme: refactor APVA by  pulling  out  load() and checkValidRequest() into a parent class (or an interceptor).
public class CollectionAdhocShareSaveAction extends AbstractCollectionAdhocShareAction {

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        getAdhocShare().setCollectionId(getId());

    }

    @WriteableSession
    @Override
    @Action(value="save", results={
            @Result(name="success", type="redirect", location="/collection/${id}"),
            @Result(name="input", location="edit.ftl")
    })
    @PostOnly
    @Validations(
            emails = { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "adhocShare.email", key = "adhocShareCreateAction.email_required")},
            requiredFields = {@RequiredFieldValidator(fieldName = "adhocShare.email", key = "adhocShareCreateAction.email_required")}

    )
    public String execute() {
        getResourceCollectionService().createShareFromAdhoc(getAdhocShare(), null, getResourceCollection(), null, getAuthenticatedUser());
        addActionMessage(getText("adhocShareCreateAction.invitation_sent", getAdhocShare().getEmail()));
        return "success";
    }


}
