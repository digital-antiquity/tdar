package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.ResourceType;

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
    protected void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException {
        return;
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

    public Class<Image> getPersistableClass() {
        return Image.class;
    }
}
