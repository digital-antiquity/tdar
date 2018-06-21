package org.tdar.struts.action.geospatial;

import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.dataset.AbstractDatasetController;
import org.tdar.struts_base.action.TdarActionSupport;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an Image and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:Adam.Brin@asu.edu'>Adam Brin</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/geospatial")
@Result(name = TdarActionSupport.INPUT, location = "edit.ftl")
public class GeospatialController extends AbstractDatasetController<Geospatial> {

    private static final long serialVersionUID = 6576781526708737335L;

    @Override
    public Class<Geospatial> getPersistableClass() {
        return Geospatial.class;
    }

    public Geospatial getGeospatial() {
        return getPersistable();
    }

    public void setGeospatial(Geospatial dataset) {
        setPersistable(dataset);
    }

    @Override
    public Set<String> getValidFileExtensions() {
        Set<String> extensionsForTypes = getExtensionsForType(getPersistable().getResourceType(), ResourceType.GEOSPATIAL);
        return extensionsForTypes;
    }

    @Override
    public Geospatial getResource() {
        if (getPersistable() == null) {
            setPersistable(createPersistable());
        }
        return getPersistable();
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    public Geospatial getDataset() {
        return getPersistable();
    }

    public void setDataset(Geospatial geospatial) {
        setGeospatial(geospatial);
    }
}
