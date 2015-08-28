package org.tdar.core.dao.integration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.entity.Person;
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
    private Dataset dataset;
    private DataTable dataTable;
    private Person submitter;

    public DataTableProxy(DataTable dataTable) {
        dataset = dataTable.getDataset();
        this.setDataTable(dataTable);
        this.setSubmitter(dataTable.getDataset().getSubmitter());
    }

    @JsonView(JsonIntegrationFilter.class)
    public Set<Ontology> getMappedOntologies() {
        Set<Ontology> ontologies = new HashSet<>();
        for (DataTableColumn column : getDataTable().getDataTableColumns()) {
            if (column.getMappedOntology() != null) {
                ontologies.add(column.getMappedOntology());
            }
        }
        return ontologies;
    }

    @JsonView(JsonIntegrationFilter.class)
    public Person getSubmitter() {
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
