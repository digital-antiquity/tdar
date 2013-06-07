package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;

public abstract class AbstractSupportingInformationResourceController<R extends InformationResource> extends AbstractInformationResourceController<R> {

    private static final long serialVersionUID = -3261759402735229520L;

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

    public void setRelatedResources(ArrayList<Resource> relatedResources) {
        this.relatedResources = relatedResources;
    }

    private Long subcategoryId;

    private List<CategoryVariable> subcategories;

    private ArrayList<Resource> relatedResources;

    @Override
    protected FileProxy processTextInput() {
        if (!isTextInput()) {
            return null;
        }

        if (StringUtils.isBlank(getFileTextInput())) {
            addActionError("Please enter your " + getPersistable().getResourceType().getLabel() + " into the text area.");
            return null;
        }

        if (ObjectUtils.equals(getFileTextInput(), getLatestUploadedTextVersionText())) {
            logger.info("incoming and current file input text is the same, skipping further actions");
            return null;
        } else {
            logger.info("processing updated text input for {}", getPersistable());
        }

        try {
            // process the String uploaded via the fileTextInput box verbatim as the UPLOADED_TEXT version
            // 2013-22-04 AB: if our validation rules for Struts are working, this is not needed as the title already is checked way before this
            // if (StringUtils.isBlank(getPersistable().getTitle())) {
            // logger.error("Resource title was empty, client side validation failed for {}", getPersistable());
            // addActionError("Please enter a title for your " + getPersistable().getResourceType().getLabel());
            // return null;
            // }
            String uploadedTextFilename = getPersistable().getTitle() + ".txt";

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
            logger.info("Category: {} ; subcategory: {} ", categoryId, subcategoryId);
            if (Persistable.Base.isNullOrTransient(subcategoryId)) {
                supporting.setCategoryVariable(getCategoryVariableService().find(categoryId));
            } else {
                supporting.setCategoryVariable(getCategoryVariableService().find(subcategoryId));
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
        subcategories = new ArrayList<CategoryVariable>(getCategoryVariableService().find(categoryId).getSortedChildren());
    }

    @Override
    public Collection<? extends Persistable> getDeleteIssues() {
        return getRelatedResources();
    }

    public List<Resource> getRelatedResources() {
        if (relatedResources == null) {
            relatedResources = new ArrayList<Resource>();
            for (DataTable table : getDataTableService().findDataTablesUsingResource((Resource) getPersistable())) {
                if (!table.getDataset().isDeleted()) {
                    relatedResources.add(table.getDataset());
                }
            }
        }
        return relatedResources;
    }

    @Override
    public String deleteCustom() {
        List<Resource> related = getRelatedResources();
        if (related.size() > 0) {
            String titles = StringUtils.join(related, ',');
            String message = "please remove the mappings before deleting: " + titles;
            addActionErrorWithException("this resource is still mapped to the following datasets", new TdarRecoverableRuntimeException(message));
            return ERROR;
        }
        return SUCCESS;
    }

    @Override
    public boolean supportsMultipleFileUpload() {
        return false;
    }

    protected String getLatestUploadedTextVersionText() {
        // in order for this to work we need to be generating text versions
        // of these files for both text input and file uploads
        for (InformationResourceFileVersion version : getPersistable().getLatestVersions(VersionType.UPLOADED_TEXT)) {
            try {
                return FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(version));
            } catch (Exception e) {
                logger.debug("an error occurred when trying to load the text version of a file", e);
            }
        }
        return "";
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
