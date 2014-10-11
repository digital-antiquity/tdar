package org.tdar.struts.action.geospatial;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.struts.action.resource.AbstractDatasetViewAction;


@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/geospatial")
public class GeospatialViewAction extends AbstractDatasetViewAction<Geospatial> {

    private static final long serialVersionUID = 6518833514525728322L;

}
