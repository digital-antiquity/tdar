package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.service.XmlService;

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
@ParentPackage("secured")
@Scope("prototype")
@Namespace("/resource/ajax")
public class AjaxController extends TdarActionSupport {

    private static final long serialVersionUID = -1202795099371942148L;

    private Long categoryVariableId;
    private InputStream resultJson;
    
    @Autowired
    private transient XmlService xmlService;
    
    @Action(value="column-metadata-subcategories", results = { 
            @Result(name = SUCCESS, type = JSONRESULT, params={"stream","resultJson"})})
    public String columnMetadataSubcategories() {
        if (Persistable.Base.isNullOrTransient(categoryVariableId)) {
            getLogger().debug("Invalid category variable: " + categoryVariableId);
        }
        List<CategoryVariable> subcategories = getSubcategories();
        if (CollectionUtils.isEmpty(subcategories)) {
            CategoryVariable e = new CategoryVariable();
            e.setId(-1L);
            e.setLabel("N/A");
            subcategories.add(e);
        }
        setResultJson(new ByteArrayInputStream(xmlService.convertFilteredJsonForStream(subcategories, null, null).getBytes()));
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

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

}
