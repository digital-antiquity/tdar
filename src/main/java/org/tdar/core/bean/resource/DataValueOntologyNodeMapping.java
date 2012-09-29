package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * $Id$
 * 
 * Represents the mapping between a data value from a data table column and a node in an ontology.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "data_value_ontology_node_mapping")
public class DataValueOntologyNodeMapping extends Persistable.Base {

    private static final long serialVersionUID = 3365672931671990791L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "data_table_column_id")
    @XStreamOmitField
    private DataTableColumn dataTableColumn;

    @Column(name = "data_value")
    private String dataValue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ontology_node_id")
    private OntologyNode ontologyNode;

    public DataValueOntologyNodeMapping() {
    }

    public DataValueOntologyNodeMapping(DataTableColumn column, OntologyNode node, String value) {
        setDataTableColumn(column);
        setOntologyNode(node);
        setDataValue(value);
    }

    @XmlIDREF
    @XmlAttribute(name = "dataTableColumnId")
    public DataTableColumn getDataTableColumn() {
        return dataTableColumn;
    }

    public void setDataTableColumn(DataTableColumn dataTableColumn) {
        this.dataTableColumn = dataTableColumn;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String columnValue) {
        this.dataValue = columnValue;
    }

    @XmlIDREF
    @XmlAttribute(name = "ontologyNodeId")
    public OntologyNode getOntologyNode() {
        return ontologyNode;
    }

    public void setOntologyNode(OntologyNode ontologyNode) {
        this.ontologyNode = ontologyNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(dataValue).append(" -> ").append(ontologyNode.getDisplayName());
        return sb.toString();
    }
}
