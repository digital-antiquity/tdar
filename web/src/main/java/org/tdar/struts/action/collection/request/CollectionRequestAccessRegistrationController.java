package org.tdar.struts.action.collection.request;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.struts.action.AbstractRequestAccessRegistrationAction;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

/**
 * Handle registration for new users and pass back to request-access-action
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/collection/request")
@Component
@Scope("prototype")
public class CollectionRequestAccessRegistrationController extends AbstractRequestAccessRegistrationAction<ResourceCollection> implements Validateable, Preparable {

    private static final long serialVersionUID = 8033398514273270692L;

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
