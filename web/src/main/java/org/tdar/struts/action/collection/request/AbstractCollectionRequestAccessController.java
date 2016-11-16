package org.tdar.struts.action.collection.request;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts.action.AbstractRequestAccessController;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.core.bean.collection.ResourceCollection;

@Namespace("/collection/request")
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
public class AbstractCollectionRequestAccessController extends AbstractRequestAccessController<ResourceCollection>{


    private static final long serialVersionUID = 2209267919681648552L;

    @Override
    public String getTypeNamespace() {
        return "collection";
    }
    
    public ResourceCollection getCollection() {
        return getPersistable();
    }

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

}
