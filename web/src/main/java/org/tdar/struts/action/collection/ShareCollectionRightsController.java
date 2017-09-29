package org.tdar.struts.action.collection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces(value={@Namespace("/share"), @Namespace("/collection")})
public class ShareCollectionRightsController extends AbstractCollectionRightsController<ResourceCollection> {


    private static final long serialVersionUID = 5522048517742464825L;

    @Override
    public Class<ResourceCollection> getPersistableClass() {
        return ResourceCollection.class;
    }

}
