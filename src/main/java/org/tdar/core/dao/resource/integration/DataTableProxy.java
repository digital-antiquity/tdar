package org.tdar.core.dao.resource.integration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import static org.apache.commons.lang3.StringUtils.abbreviate;


@JsonAutoDetect 
public class DataTableProxy implements Serializable {

    private static final long serialVersionUID = -5052540683421478297L;
    private Dataset dataset;
    private DataTable dataTable;
    private Person submitter;

    public DataTableProxy(DataTable dataTable) {
        dataset = dataTable.getDataset();
        //Truncate verbose descriptions that we aren't displaying to twitter-safe proportions.
        //FIXME: I suspect there's a better place to do this. Also I hope these objects aren't writeable.
        //FIXME: In the event that the previous fixme was too subtle I should point out that, yes, these persisted objects are in fact TOTALLY WRITABLE.
        dataset.setDescription(abbreviate(dataset.getDescription(), 140));

        this.setDataTable(dataTable);
        this.setSubmitter(dataTable.getDataset().getSubmitter());
    }


    //search result rows should be smaller than a megabyte.
    class MappedOntology {
        @JsonView(JsonIntegrationFilter.class)
        Long id;
        @JsonView(JsonIntegrationFilter.class)
        String title;
        MappedOntology(Ontology o) {
            id = o.getId();
            title = o.getTitle();
        }
    }

    //Like a person, but less so.
    class Person {
        @JsonView(JsonIntegrationFilter.class)
        Long id;
        @JsonView(JsonIntegrationFilter.class)
        String properName;
        Person(TdarUser u) {
            id = u.getId();
            properName = u.getProperName();
        }
    }


    @JsonView(JsonIntegrationFilter.class)
    public Set<MappedOntology> getMappedOntologies() {
        Set<MappedOntology> ontologies = new HashSet<>();
        for (DataTableColumn column : getDataTable().getDataTableColumns()) {
            if (column.getDefaultOntology() != null) {
                ontologies.add(new MappedOntology(column.getDefaultOntology()));
            }
        }
        return ontologies;
    }

    @JsonView(JsonIntegrationFilter.class)
    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = new Person(submitter);
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
