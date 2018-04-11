package org.tdar.struts.action.upload;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.result.HasJsonDocumentResult;
import org.tdar.utils.json.JacksonView;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.web.service.WebPersonalFilestoreService;

@SuppressWarnings("serial")
@Namespace("/upload")
@Component
@Scope("prototype")
@ParentPackage("secured")
@Results({
        @Result(name = "exception", type = TdarActionSupport.HTTPHEADER, params = { "error", "500" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "500" })
})
@HttpForbiddenErrorResponseOnly
public class CreateFilestoreTicketAction extends AbstractAuthenticatableAction implements HasJsonDocumentResult {


    @Autowired
    private WebPersonalFilestoreService filestoreService;
    private PersonalFilestoreTicket personalFilestoreTicket;

    @Action(value = "grab-ticket", results = { @Result(name = SUCCESS, type = JSONRESULT)
    })
    public String grabTicket() {
        
        setPersonalFilestoreTicket(filestoreService.grabTicket(getAuthenticatedUser()));
        this.resultObject = getPersonalFilestoreTicket();
        this.jsonView = JsonLookupFilter.class;

        return SUCCESS;
    }

    private Object resultObject;
    private Class<? extends JacksonView> jsonView;

    public Object getResultObject() {
        return resultObject;
    }

    public Class<? extends JacksonView> getJsonView() {
        return jsonView;
    }

    public PersonalFilestoreTicket getPersonalFilestoreTicket() {
        return personalFilestoreTicket;
    }

    public void setPersonalFilestoreTicket(PersonalFilestoreTicket personalFilestoreTicket) {
        this.personalFilestoreTicket = personalFilestoreTicket;
    }

}
