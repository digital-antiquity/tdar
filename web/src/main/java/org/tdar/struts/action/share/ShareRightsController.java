package org.tdar.struts.action.share;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.struts.action.AbstractCollectionRightsController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/share")
public class ShareRightsController extends AbstractCollectionRightsController<SharedCollection> {


    private static final long serialVersionUID = 5522048517742464825L;

    @Override
    public Class<SharedCollection> getPersistableClass() {
        return SharedCollection.class;
    }

}
