package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

public abstract class AbstractSupportingInformationResourceController<R extends InformationResource> extends AbstractInformationResourceController<R> {

    private static final String TXT = ".txt";

    private static final long serialVersionUID = -3261759402735229520L;

    @Autowired
    private transient CategoryVariableService categoryVariableService;

    private Long categoryId;
    private String fileInputMethod;
    private String fileTextInput;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public void setSubcategories(List<CategoryVariable> subcategories) {
        this.subcategories = subcategories;
    }


    private Long subcategoryId;

    private List<CategoryVariable> subcategories;


    @Override
    protected FileProxy processTextInput() {
        if (!isTextInput()) {
            return null;
        }
        if (StringUtils.isBlank(getFileTextInput())) {
            addActionError(getText("abstractSupportingInformationResourceController.please_enter"));
            return null;
        }
        InformationResourceFileVersion latestUploadedTextVersion = getLatestUploadedTextVersion();
        if ((latestUploadedTextVersion != null)
                && (latestUploadedTextVersion.getInformationResourceFile().getStatus() != FileStatus.PROCESSING_ERROR)) {
            if (Objects.equals(getFileTextInput(), getLatestUploadedTextVersionText())) {
                getLogger().info("incoming and current file input text is the same, skipping further actions");
                return null;
            } else {
                getLogger().info("processing updated text input for {}", getPersistable());
            }
        }

        try {
            // process the String uploaded via the fileTextInput box verbatim as the UPLOADED_TEXT version
            // 2013-22-04 AB: if our validation rules for Struts are working, this is not needed as the title already is checked way before this
            // if (StringUtils.isBlank(getPersistable().getTitle())) {
            // getLogger().error("Resource title was empty, client side validation failed for {}", getPersistable());
            // addActionError("Please enter a title for your " + getPersistable().getResourceType().getLabel());
            // return null;
            // }
            String uploadedTextFilename = getPersistable().getTitle() + TXT;

            FileProxy uploadedTextFileProxy = new FileProxy(uploadedTextFilename, FileProxy.createTempFileFromString(getFileTextInput()),
                    VersionType.UPLOADED_TEXT);

            // next, generate "uploaded" version of the file. In this case the VersionType.UPLOADED isn't entirely accurate
            // as this is UPLOADED_GENERATED, but it's the file that we want to process in later parts of our code.
            FileProxy primaryFileProxy = createUploadedFileProxy(getFileTextInput());
            primaryFileProxy.addVersion(uploadedTextFileProxy);
            setFileProxyAction(primaryFileProxy);
            return primaryFileProxy;
        } catch (IOException e) {
            getLogger().error("unable to create temp file or write " + getFileTextInput() + " to temp file", e);
            throw new TdarRecoverableRuntimeException(e);
        }
    };

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        super.loadCustomMetadata();
        if (getPersistable() instanceof SupportsResource) {
            SupportsResource supporting = (SupportsResource) getPersistable();
            CategoryVariable categoryVariable = supporting.getCategoryVariable();
            if (categoryVariable != null) {
                if (categoryVariable.getParent() == null) {
                    setCategoryId(categoryVariable.getId());
                } else {
                    setCategoryId(categoryVariable.getParent().getId());
                    setSubcategoryId(categoryVariable.getId());
                    loadSubcategories();
                }
            }
            setFileTextInput(getLatestUploadedTextVersionText());
        }
    }

    protected void saveCategories() {
        if (getPersistable() instanceof SupportsResource) {
            SupportsResource supporting = (SupportsResource) getPersistable();
            getLogger().info("Category: {} ; subcategory: {} ", categoryId, subcategoryId);
            if (PersistableUtils.isNullOrTransient(subcategoryId)) {
                supporting.setCategoryVariable(categoryVariableService.find(categoryId));
            } else {
                supporting.setCategoryVariable(categoryVariableService.find(subcategoryId));
            }
        }
    }

    public List<CategoryVariable> getSubcategories() {
        if (subcategories == null) {
            loadSubcategories();
        }
        return subcategories;
    }

    private void loadSubcategories() {
        if (categoryId == null) {
            subcategories = Collections.emptyList();
        }
        subcategories = new ArrayList<CategoryVariable>(categoryVariableService.find(categoryId).getSortedChildren());
    }


    protected InformationResourceFileVersion getLatestUploadedTextVersion() {
        InformationResourceFileVersion version = null;
        Collection<InformationResourceFileVersion> versions = getPersistable().getLatestVersions(VersionType.UPLOADED_TEXT);
        if (!versions.isEmpty()) {
            version = getPersistable().getLatestVersions(VersionType.UPLOADED_TEXT).iterator().next();

        }
        return version;
    }

    protected String getLatestUploadedTextVersionText() {
        // in order for this to work we need to be generating text versions
        // of these files for both text input and file uploads
        String versionText = "";
        InformationResourceFileVersion version = getLatestUploadedTextVersion();
        if (version != null) {
            try {
                versionText = FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
            } catch (IOException e) {
                getLogger().debug("an error occurred when trying to load the text version of a file", e);
            }
        }
        return versionText;
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    public String getFileInputMethod() {
        return fileInputMethod;
    }

    private boolean isTextInput() {
        return FILE_INPUT_METHOD.equals(fileInputMethod);
    }

    public void setFileInputMethod(String fileInputMethod) {
        this.fileInputMethod = fileInputMethod;
    }

    public String getFileTextInput() {
        return fileTextInput;
    }

    public void setFileTextInput(String fileTextInput) {
        this.fileTextInput = fileTextInput;
    }

}
