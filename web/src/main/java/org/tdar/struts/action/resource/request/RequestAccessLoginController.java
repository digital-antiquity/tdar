package org.tdar.struts.action.resource.request;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.action.AbstractRequestAccessLoginAction;
/**
 * Handle login for new users and pass back to request-access-action
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class RequestAccessLoginController extends AbstractRequestAccessLoginAction<Resource> {

    private static final long serialVersionUID = 1525006233392261028L;

    @Override
    public String getTypeNamespace() {
        return "resource";
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

}
