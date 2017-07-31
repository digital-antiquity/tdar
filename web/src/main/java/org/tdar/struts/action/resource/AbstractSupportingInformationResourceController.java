package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.SupportsResource;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.web.service.ResourceSaveControllerService;

public abstract class AbstractSupportingInformationResourceController<R extends InformationResource> extends AbstractInformationResourceController<R> {

    private static final long serialVersionUID = -3261759402735229520L;

    @Autowired
    private transient CategoryVariableService categoryVariableService;

    @Autowired
    private transient ResourceSaveControllerService saveService;

    private Long categoryId;
    private Long subcategoryId;
    private List<CategoryVariable> subcategories;

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
            setFileTextInput(saveService.getLatestUploadedTextVersionText(getPersistable()));
        }
    }

    protected void saveCategories() {
        proxy.setCategoryId(categoryId);
        proxy.setSubcategoryId(subcategoryId);
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



    @Override
    public boolean isMultipleFileUploadEnabled() {
        return false;
    }


}
