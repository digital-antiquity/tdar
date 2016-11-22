package org.tdar.struts.action.collection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.struts.action.AbstractCollectionRightsController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/collection")
public class CollectionRightsController extends AbstractCollectionRightsController<ListCollection> {


    private static final long serialVersionUID = 4318434880012567197L;

    @Override
    public Class<ListCollection> getPersistableClass() {
        return ListCollection.class;
    }

}
