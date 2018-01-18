package org.tdar.core.dao.integration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.utils.json.JsonIdNameFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect
/**
 * Represents one DataTableColumn in one Integration Column
 * 
 * @author abrin
 *
 */
public class IntegrationColumnPartProxy implements Serializable {

    private static final long serialVersionUID = -6564768298774032395L;

    private Ontology sharedOntology;
    private Set<OntologyNode> flattenedNodes = new HashSet<>();
    private DataTableColumn dataTableColumn;

    @JsonView({ JsonIdNameFilter.class })
    public Ontology getSharedOntology() {
        return sharedOntology;
    }

    public void setSharedOntology(Ontology shared) {
        this.sharedOntology = shared;
    }

    @JsonView({ JsonIdNameFilter.class })
    public Set<OntologyNode> getFlattenedNodes() {
        return flattenedNodes;
    }

    public void setFlattenedNodes(Set<OntologyNode> flattenedNodes) {
        this.flattenedNodes = flattenedNodes;
    }

    @JsonView({ JsonIdNameFilter.class })
    public DataTableColumn getDataTableColumn() {
        return dataTableColumn;
    }

    public void setDataTableColumn(DataTableColumn dataTableColumn) {
        this.dataTableColumn = dataTableColumn;
    }

}
