package org.tdar.struts.action.api.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.struts.action.api.AbstractJsonApiAction;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/api/dataset")
public class FieldValueAutocomplete extends AbstractJsonApiAction {

    private static final long serialVersionUID = -1403433795930971910L;
    private List<String> values = new ArrayList<>();
    private Long id;
    private Dataset dataset;
    private Long columnId;
    private String value;
    private DataTableColumn column;

    @Autowired
    private DatasetService datasetService;


    @Override
    public void prepare() throws Exception {
        dataset = getGenericService().find(Dataset.class, id);
        column = getGenericService().find(DataTableColumn.class, columnId);
        super.prepare();
    }

    @Action(value = "listSearchFields")
    public String execute() throws IOException {
        setValues(datasetService.findAutocompleteValues(dataset, column, value, getAuthenticatedUser()));
        setResultObject(getValues());
        return SUCCESS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
