package org.tdar.core.dao.resource.integration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;


@JsonAutoDetect 
public class DataTableProxy implements Serializable {

    private static final long serialVersionUID = -5052540683421478297L;

    public DataTableProxy(DataTable dataTable) {
        this.setDataset(dataTable.getDataset());
        this.setDataTable(dataTable);
        this.setSubmitter(dataTable.getDataset().getSubmitter());
    }

    private Dataset dataset;
    private DataTable dataTable;
    private TdarUser submitter;

    @JsonView(JsonIntegrationFilter.class)
    public Set<Ontology> getMappedOntologies() {
        Set<Ontology> ontologies = new HashSet<>();
        for (DataTableColumn column : getDataTable().getDataTableColumns()) {
            if (column.getDefaultOntology() != null) {
                ontologies.add(column.getDefaultOntology());
            }
        }
        return ontologies;
    }

    @JsonView(JsonIntegrationFilter.class)
    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    @JsonView(JsonIntegrationFilter.class)
    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @JsonView(JsonIntegrationFilter.class)
    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

}
