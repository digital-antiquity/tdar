package org.tdar.struts.action;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CategoryVariable;

/**
 * $Id$
 * <p>
 * Handles ajax requests.  Currently only used for subcategory variables displayed for
 * editing column metadata for a dataset.
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
    private Integer index;
    
    private String logMessage;
    
    
    @Action("column-metadata-subcategories")
    public String columnMetadataSubcategories() {
        if (categoryVariableId == null || categoryVariableId == -1L) {
            logger.error("Invalid category variable: " + categoryVariableId);
        }
        if (index == null) {
            logger.error(String.format("Didn't specify subcategory index [%d] ", index));
        }
        return SUCCESS;
    }

    @Action("subcategories")
    public String subcategories() {
        return SUCCESS;
    }
    

    @Action(value = "log-debug", results = {@Result(name=SUCCESS, type="httpheader", params={"status", "200"})})
    public String logDebug() {
        logger.debug(logMessage);
        return SUCCESS;
    }

    
    public CategoryVariable getCategoryVariable() {
        return getCategoryVariableService().find(categoryVariableId);
    }
    
    /**
     * Returns a list of CategoryVariables that are subcategories of the category variable id.
     * @return
     */
    public List<CategoryVariable> getSubcategories() {
        if (categoryVariableId == null || categoryVariableId == -1L) {
            return Collections.emptyList();
        }
        return getCategoryVariableService().findAllSubcategories(categoryVariableId);
    }

    public void setCategoryVariableId(Long categoryVariableId) {
        this.categoryVariableId = categoryVariableId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
    
    
    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }


}
