package org.tdar.core.bean.resource.datatable;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

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
    @Column(name = "relationship_type")
    private DataTableColumnRelationshipType type;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "relationship")
    private Set<DataTableColumnRelationship> columnRelationships;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, optional = false)
    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to a Dataset.  
     */
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

    @XmlElementWrapper(name = "columnRelationships")
    @XmlElement(name = "columnRelationship")
    public Set<DataTableColumnRelationship> getColumnRelationships() {
        return columnRelationships;
    }

    public void setColumnRelationships(Set<DataTableColumnRelationship> columnRelationships) {
        this.columnRelationships = columnRelationships;
    }

    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to DataTables  
     */
    public void setForeignTable(DataTable foreignTable) {
        this.foreignTable = foreignTable;
    }

    @XmlElement(name = "foreignTableRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to DataTables  
     */
    public DataTable getForeignTable() {
        return foreignTable;
    }

    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to DataTables  
     */
    public void setLocalTable(DataTable localTable) {
        this.localTable = localTable;
    }

    @XmlElement(name = "localTableRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to DataTables  
     */
    public DataTable getLocalTable() {
        return localTable;
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

    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to a Dataset.  
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @Deprecated
    /**
     * @deprecated  Redundant since the DataTableRelationship has columnRelationships
     *  which link it to a Dataset.  
     */
    @XmlTransient
    public Dataset getDataset() {
        return dataset;
    }
}
