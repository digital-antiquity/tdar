package org.tdar.core.bean.resource.datatable;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;

/**
 * This Class represents a Primary or Foreign Key relationship between two data tables
 * These relationships can represent a single column foreign key, or multiple column
 * foreign key, as well as more basic primary keys.
 */
@Entity
@Table(name = "data_table_relationship")
public class DataTableRelationship extends Persistable.Base {

    private static final long serialVersionUID = 7389360675412671860L;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", length = FieldLength.FIELD_LENGTH_255)
    private DataTableColumnRelationshipType type;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "relationship_id")
    private Set<DataTableColumnRelationship> columnRelationships = new HashSet<DataTableColumnRelationship>();

    public void setType(DataTableColumnRelationshipType type) {
        this.type = type;
    }

    public DataTableColumnRelationshipType getType() {
        return type;
    }

    @XmlElementWrapper(name = "columnRelationships")
    @XmlElement(name = "columnRelationship")
    public Set<DataTableColumnRelationship> getColumnRelationships() {
        return columnRelationships;
    }

    public void setColumnRelationships(Set<DataTableColumnRelationship> columnRelationships) {
        this.columnRelationships = columnRelationships;
    }

    @XmlTransient
    public DataTable getForeignTable() {
        // try {
        DataTableColumnRelationship relationship = getColumnRelationships().iterator().next();
        return relationship.getForeignColumn().getDataTable();
        // } catch (Exception e) {
        // }
        // return null;
    }

    @XmlTransient
    public DataTable getLocalTable() {
        // try {
        DataTableColumnRelationship relationship = getColumnRelationships().iterator().next();
        return relationship.getLocalColumn().getDataTable();
        // } catch (Exception e) {
        //
        // }
        // return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getType().name());
        sb.append(" - ").append(getLocalTable().getName()).append(" (");
        for (DataTableColumnRelationship rel : getColumnRelationships()) {
            sb.append(rel.getLocalColumn().getName());
            sb.append("<==>");
            sb.append(rel.getForeignColumn().getName());
            sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

}
