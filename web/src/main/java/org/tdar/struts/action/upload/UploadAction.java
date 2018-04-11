package org.tdar.struts.action.upload;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
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
public class UploadAction extends AbstractUploadController {

//    @Action(value = "index", results = { @Result(name = SUCCESS, location = "index.ftl") })
//    public String index() {
//
//        // get a claimcheck that all uploads will use
//        // personalFilestoreTicket = filestoreService.createPersonalFilestoreTicket(getAuthenticatedUser());
//        return SUCCESS;
//    }

    @Action(value = "upload",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" }),
                    @Result(name = ERROR, type = JSONRESULT, params = { "stream", "jsonInputStream", "statusCode", "400" })
            })
    @PostOnly
    public String upload() {
        return super.upload();
    }

}