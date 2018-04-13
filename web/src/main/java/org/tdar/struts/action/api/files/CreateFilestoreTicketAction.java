package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.result.HasJsonDocumentResult;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.web.service.WebPersonalFilestoreService;

@SuppressWarnings("serial")
@Component
@Scope("prototype")
@ParentPackage("secured")
public class CreateFilestoreTicketAction extends AbstractJsonApiAction implements HasJsonDocumentResult {

    @Autowired
    private WebPersonalFilestoreService filestoreService;
    private PersonalFilestoreTicket personalFilestoreTicket;

    @Action(value = "grab-ticket", results = { @Result(name = SUCCESS, type = JSONRESULT)
    })
    public String grabTicket() throws IOException {

        setPersonalFilestoreTicket(filestoreService.grabTicket(getAuthenticatedUser()));
        setJsonObject(getPersonalFilestoreTicket(), JsonLookupFilter.class);

        return SUCCESS;
    }

    public PersonalFilestoreTicket getPersonalFilestoreTicket() {
        return personalFilestoreTicket;
    }

    public void setPersonalFilestoreTicket(PersonalFilestoreTicket personalFilestoreTicket) {
        this.personalFilestoreTicket = personalFilestoreTicket;
    }

}
