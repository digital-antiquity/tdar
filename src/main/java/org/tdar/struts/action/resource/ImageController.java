package org.tdar.struts.action.resource;

import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.TdarActionException;

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
@Namespace("/image")
public class ImageController extends AbstractInformationResourceController<Image> {

    private static final long serialVersionUID = 377533801938016848L;

    @Override
    protected String save(Image image) throws TdarActionException {
        saveBasicResourceMetadata();
        saveInformationResourceProperties();
        // getGenericService().saveOrUpdate(image);
        handleUploadedFiles();
        // getGenericService().saveOrUpdate(image);
        return SUCCESS;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.IMAGE);
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    public void setImage(Image image) {
        setPersistable(image);
    }

    public Image getImage() {
        return getPersistable();
    }

    @Override
    public Class<Image> getPersistableClass() {
        return Image.class;
    }

}
