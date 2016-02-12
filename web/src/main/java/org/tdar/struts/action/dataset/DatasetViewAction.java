package org.tdar.struts.action.dataset;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/dataset")
public class DatasetViewAction extends AbstractDatasetViewAction<Dataset> {

    private static final long serialVersionUID = -6320076338548768011L;

    @Override
    public Class<Dataset> getPersistableClass() {
        return Dataset.class;
    }
}
