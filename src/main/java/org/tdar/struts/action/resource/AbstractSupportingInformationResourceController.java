package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

public abstract class AbstractSupportingInformationResourceController<R extends InformationResource> extends AbstractInformationResourceController<R> {

    private static final long serialVersionUID = -3261759402735229520L;

    private Long categoryId;

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
    protected void loadCustomMetadata() {
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
    protected void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<? extends Persistable> getDeleteIssues() {
        return getRelatedResources();
    }

    public List<Resource> getRelatedResources() {
        relatedResources = new ArrayList<Resource>();
        for (DataTable table : getDataTableService().findDataTablesUsingResource((Resource) getPersistable())) {
            if (!table.getDataset().isDeleted()) {
                relatedResources.add(table.getDataset());
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
}
