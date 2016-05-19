package org.tdar.core.dao.integration.search;

import java.util.ArrayList;
import java.util.List;

public class OntologySearchFilter extends AbstractIntegrationSearchFilter {

    private static final long serialVersionUID = 2222923660768327734L;

    private Long categoryVariableId = -1L;
    private List<Long> dataTableIds = new ArrayList<>();

    public Long getCategoryVariableId() {
        return categoryVariableId;
    }

    public void setCategoryVariableId(Long categoryVariableId) {
        this.categoryVariableId = categoryVariableId;
    }

    @SuppressWarnings("unused")
    @Deprecated
    //"ignore, required for hibernate"
    private void setHasDatasets(boolean b) {
        
    }
    
    public boolean isHasDatasets() {
        return !getDataTableIds().isEmpty();
    }

    public List<Long> getDataTableIds() {
        return dataTableIds;
    }

    @SuppressWarnings("unused")
    @Deprecated
    //"ignore, required for hibernate"
    private void setPaddedDataTableIds(List<Long> id) {
        
    }

    public List<Long> getPaddedDataTableIds() {
        if (dataTableIds.isEmpty()) {
            return paddedIdList();
        }
        return dataTableIds;
    }

    public void setDataTableIds(List<Long> dataTableIds) {
        this.dataTableIds = dataTableIds;
    }
}
