package org.tdar.core.dao.integration;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.integration.IntegrationSearchFilter;

public class OntologyIntegrationSearchFilter extends IntegrationSearchFilter {

    private static final long serialVersionUID = -5221366878292263318L;

    private CategoryVariable categoryVariable;
    private List<DataTable> dataTables = new ArrayList<>();
    
    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    public void setCategoryVariable(CategoryVariable categoryVariable) {
        this.categoryVariable = categoryVariable;
    }

    public List<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

}
