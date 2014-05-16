package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryVariable;

/**
 * $Id$
 * <p>
 * Handles ajax requests. Currently only used for subcategory variables displayed for editing column metadata for a dataset.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
@Scope("prototype")
@Namespace("/resource/ajax")
public class AjaxController extends TdarActionSupport {

    private static final long serialVersionUID = -1202795099371942148L;

    private Long categoryVariableId;

    @Action("column-metadata-subcategories")
    public String columnMetadataSubcategories() {
        if (Persistable.Base.isNullOrTransient(categoryVariableId)) {
            getLogger().error("Invalid category variable: " + categoryVariableId);
        }
        return SUCCESS;
    }

    public CategoryVariable getCategoryVariable() {
        return getGenericService().find(CategoryVariable.class, categoryVariableId);
    }

    /**
     * Returns a list of CategoryVariables that are subcategories of the category variable id.
     * 
     * @return
     */
    public List<CategoryVariable> getSubcategories() {
        if (Persistable.Base.isNullOrTransient(categoryVariableId)) {
            return Collections.emptyList();
        }
        return new ArrayList<CategoryVariable>(getCategoryVariable().getSortedChildren());
    }

    public void setCategoryVariableId(Long categoryVariableId) {
        this.categoryVariableId = categoryVariableId;
    }

}
