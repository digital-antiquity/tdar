package org.tdar.struts.action.resource;

import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.ResourceType;

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
public class GeospatialController extends AbstractInformationResourceController<Geospatial>{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    @Override
    protected String save(Geospatial persistable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Geospatial> getPersistableClass() {
        return Geospatial.class;
    }
    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(getPersistable().getResourceType());
    }

}
