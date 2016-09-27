package org.tdar.struts.action.resource.request;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.AbstractRequestAcessRegistrationAction;

/**
 * Handle registration for new users and pass back to request-access-action
 * 
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class RequestAccessRegistrationController extends AbstractRequestAcessRegistrationAction<Resource> {

    private static final long serialVersionUID = -893535919691607147L;

    @Override
    public String getTypeNamespace() {
        return "resource";
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }
}
