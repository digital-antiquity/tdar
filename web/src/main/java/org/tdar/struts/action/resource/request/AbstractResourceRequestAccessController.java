package org.tdar.struts.action.resource.request;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.AbstractRequestAccessController;
import org.tdar.struts_base.action.TdarActionSupport;

@Namespace("/resource/request")
@Component
@Scope("prototype")
@Results({
        @Result(name = TdarActionSupport.SUCCESS, type = AbstractRequestAccessController.REDIRECT, location = AbstractRequestAccessController.SUCCESS_REDIRECT_REQUEST_ACCESS),
        @Result(name = TdarActionSupport.ERROR, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.FORBIDDEN, type = TdarActionSupport.HTTPHEADER, params = { "error", "403" })
})
/**
 * Abstract class for backing unauthenticated requests (Login and Register)
 * @author abrin
 *
 */
public class AbstractResourceRequestAccessController extends AbstractRequestAccessController<Resource> {

    private static final long serialVersionUID = -1831798412944149018L;

    public Resource getResource() {
        return getPersistable();
    }


    @Override
    public String getTypeNamespace() {
        return "resource";
    }


    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }
}
