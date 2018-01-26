package org.tdar.struts.action.collection.request;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.struts.action.AbstractRequestAccessController;

/**
 * Handle login for new users and pass back to request-access-action
 * 
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/collection/request")
@Component
@Scope("prototype")
public class CollectionRequestAccessLoginController extends AbstractRequestAccessController<ResourceCollection> {

    private static final long serialVersionUID = -1206933248591765156L;

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
