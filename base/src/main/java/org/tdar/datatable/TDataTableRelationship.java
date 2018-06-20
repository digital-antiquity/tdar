package org.tdar.datatable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This Class represents a Primary or Foreign Key relationship between two data tables
 * These relationships can represent a single column foreign key, or multiple column
 * foreign key, as well as more basic primary keys.
 */
public class TDataTableRelationship implements Serializable {

    private static final long serialVersionUID = -9205684029932129972L;

    private DataTableColumnRelationshipType type;

    private Set<TDataTableColumnRelationship> columnRelationships = new HashSet<TDataTableColumnRelationship>();

    public void setType(DataTableColumnRelationshipType type) {
        this.type = type;
    }

    public DataTableColumnRelationshipType getType() {
        return type;
    }

    public Set<TDataTableColumnRelationship> getColumnRelationships() {
        return columnRelationships;
    }

    public void setColumnRelationships(Set<TDataTableColumnRelationship> columnRelationships) {
        this.columnRelationships = columnRelationships;
    }

    public TDataTable getForeignTable() {
        TDataTableColumnRelationship relationship = getColumnRelationships().iterator().next();
        return relationship.getForeignTable();
    }

    public TDataTable getLocalTable() {
        TDataTableColumnRelationship relationship = getColumnRelationships().iterator().next();
        return relationship.getLocalTable();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getType().name());
        sb.append(" - ").append(getLocalTable().getName()).append(" (");
        for (TDataTableColumnRelationship rel : getColumnRelationships()) {
            sb.append(rel.getLocalColumn().getName());
            sb.append("<==>");
            sb.append(rel.getForeignColumn().getName());
            sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

}
