package org.tdar.struts.action.archive;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Archive;
import org.tdar.struts.action.resource.AbstractResourceViewAction;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/archive")
public class ArchiveViewAction extends AbstractResourceViewAction<Archive> {

    private static final long serialVersionUID = 7665230957988896511L;

    @Override
    public Class<Archive> getPersistableClass() {
        return Archive.class;
    }
}
