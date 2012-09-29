package org.tdar.core.bean.resource.dataTable;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Dataset;

@Entity
@Table(name = "data_table_relationship")
public class DataTableRelationship extends Persistable.Base {

    /**
     * 
     */
    private static final long serialVersionUID = 7389360675412671860L;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type")
    private DataTableColumnRelationshipType type;

    //FIXME: I think we need two join tables --one for localcolumns, one for foreigncolumns -- but hibernate creates one table w/ three fields.  don't think this will work.
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<DataTableColumn> localColumns;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<DataTableColumn> foreignColumns;

    @ManyToOne
    private Dataset dataset;

    @ManyToOne
    private DataTable localTable;

    @ManyToOne
    private DataTable foreignTable;

    public void setType(DataTableColumnRelationshipType type) {
        this.type = type;
    }

    public DataTableColumnRelationshipType getType() {
        return type;
    }

    public void setLocalColumns(Set<DataTableColumn> localColumns) {
        this.localColumns = localColumns;
    }

    public Set<DataTableColumn> getLocalColumns() {
        if (localColumns == null) {
            localColumns = new HashSet<DataTableColumn>();
        }
        return localColumns;
    }

    public void setForeignColumns(Set<DataTableColumn> foreignColumns) {
        this.foreignColumns = foreignColumns;
    }

    public Set<DataTableColumn> getForeignColumns() {
        if (foreignColumns == null) {
            foreignColumns = new HashSet<DataTableColumn>();
        }
        return foreignColumns;
    }

    public void setForeignTable(DataTable foreignTable) {
        this.foreignTable = foreignTable;
    }

    public DataTable getForeignTable() {
        return foreignTable;
    }

    public void setLocalTable(DataTable localTable) {
        this.localTable = localTable;
    }

    public DataTable getLocalTable() {
        return localTable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getType().name());
        sb.append(" - ").append(getLocalTable().getName()).append(" (");
        for (DataTableColumn col : getLocalColumns()) {
            sb.append(col.getName());
        }
        sb.append(")");
        if (getType() == DataTableColumnRelationshipType.FOREIGN_KEY) {
            sb.append(" <==> ").append(getForeignTable().getName()).append(" (");
            for (DataTableColumn col : getForeignColumns()) {
                sb.append(col.getName());
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }
}
