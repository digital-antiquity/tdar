package org.tdar.struts.action.sensoryData;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.struts.action.dataset.AbstractDatasetViewAction;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/sensory-data")
public class SensoryDataViewAction extends AbstractDatasetViewAction<SensoryData> {

    private static final long serialVersionUID = -6147128935043196832L;

    @Override
    public Class<SensoryData> getPersistableClass() {
        return SensoryData.class;
    }
}
