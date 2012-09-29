package org.tdar.struts.action;

import java.io.IOException;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an CodingSheet and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/image")
public class ImageController extends AbstractInformationResourceController<Image> {

    private static final long serialVersionUID = 377533801938016848L;

    @Autowired
    private transient ModsTransformer.ImageTransformer imageModsTransformer;

    @Autowired
    private transient DcTransformer.ImageTransformer imageDcTransformer;

    @Override
    protected void loadCustomMetadata() {
        loadInformationResourceProperties();
    }

    @Override
    protected String save(Image image) {
        saveBasicResourceMetadata();
        saveInformationResourceProperties();
        getImageService().saveOrUpdate(image);
        handleUploadedFiles();
        getImageService().saveOrUpdate(image);
        return SUCCESS;
    }

    @Override
    protected Image loadResourceFromId(Long resourceId) {
        Image image = getImageService().find(resourceId);
        if (image != null) {
            setProject(image.getProject());
        }
        return image;
    }

    @Override
    protected void processUploadedFile() throws IOException {
        return;
    }

    @Override
    protected Image createResource() {
        return new Image();
    }

    /**
     * Get the current concept.
     * 
     * @return
     */
    public Image getImage() {
        return resource;
    }

    public void setImage(Image image) {
        this.resource = image;
    }

    @Override
    public DcTransformer<Image> getDcTransformer() {
        return imageDcTransformer;
    }

    @Override
    public ModsTransformer<Image> getModsTransformer() {
        return imageModsTransformer;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.IMAGE);
    }
    
    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

}
