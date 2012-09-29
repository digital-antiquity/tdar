package org.tdar.struts.data;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;

/**
 * 
 * $Id$
 * 
 * Data class for ontology nodes in the Struts 2 web layer.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class OntologyNodeData implements Serializable {

    private static final long serialVersionUID = -7587876904161824713L;
    
    private OntologyNode ontologyNode;
    
    // the set of columns that associated with this node 
    private List<DataTableColumn> associatedColumns;
    
    // boolean flag array with the same number of entries and in the same order as the integration columns in WorkspaceController
    private boolean[] columnHasValueArray;

    private boolean parent;
    
    public OntologyNodeData(OntologyNode ontologyNode) {
        this.ontologyNode = ontologyNode;
    }

    public List<DataTableColumn> getAssociatedColumns() {
        return associatedColumns;
    }

    public void setAssociatedColumns(List<DataTableColumn> associatedColumns) {
        this.associatedColumns = associatedColumns;
    }

    public boolean[] getColumnHasValueArray() {
        return columnHasValueArray;
    }

    public void setColumnHasValueArray(boolean[] columnsWithValue) {
        this.columnHasValueArray = columnsWithValue;
    }

    public Long getId() {
        return ontologyNode.getId();
    }

    public String getIndex() {
        return ontologyNode.getIndex();
    }

    public Integer getIntervalEnd() {
        return ontologyNode.getIntervalEnd();
    }

    public Integer getIntervalStart() {
        return ontologyNode.getIntervalStart();
    }

    public String getLabel() {
        return ontologyNode.getIri();
    }

    public String getDisplayName() {
        return ontologyNode.getDisplayName();
    }

    public int getNumberOfParents() {
        return ontologyNode.getNumberOfParents();
    }

    public Ontology getOntology() {
        return ontologyNode.getOntology();
    }

    public String getUri() {
        return ontologyNode.getUri();
    }
    
    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }
    


}
