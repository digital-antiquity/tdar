package org.tdar.struts.action.collection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ListCollection;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/listcollection")
public class ListCollectionRightsController extends AbstractCollectionRightsController<ListCollection> {


    private static final long serialVersionUID = 4318434880012567197L;

    @Override
    public Class<ListCollection> getPersistableClass() {
        return ListCollection.class;
    }

}
