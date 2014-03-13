package org.tdar.core.bean.resource.datatable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.Persistable;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * Represents a relationship between two data-tables via columns
 * 
 * @author abrin
 *
 */
@Entity
@Table(name = "data_table_column_relationship")
public class DataTableColumnRelationship extends Persistable.Base {
    // FIXME: should probably be called DataTableRelationship, since it represents a relationship between two tables, defined by a set of columns in one table
    // matching a set of columns in the other
    /**
     * 
     */
    private static final long serialVersionUID = 715161001656287643L;

    /**
     * The relationship which this inter-column relationship forms part of
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "relationship_id")
    private DataTableRelationship relationship;

    @ManyToOne(optional = false)
    @JoinColumn(name = "local_column_id")
    private DataTableColumn localColumn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "foreign_column_id")
    private DataTableColumn foreignColumn;

    /**
     * @return the localColumn
     */
    @XmlElement(name = "localColumnRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public DataTableColumn getLocalColumn() {
        return localColumn;
    }

    /**
     * @param localColumn
     *            the localColumn to set
     */
    public void setLocalColumn(DataTableColumn localColumn) {
        this.localColumn = localColumn;
    }

    /**
     * @return the foreignColumn
     */
    @XmlElement(name = "foreignColumnRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public DataTableColumn getForeignColumn() {
        return foreignColumn;
    }

    /**
     * @param foreignColumn
     *            the foreignColumn to set
     */
    public void setForeignColumn(DataTableColumn foreignColumn) {
        this.foreignColumn = foreignColumn;
    }

    /**
     * @return the relationship
     */
    @XmlTransient
    public DataTableRelationship getRelationship() {
        return relationship;
    }

    /**
     * @param relationship
     *            the relationship to set
     */
    public void setRelationship(DataTableRelationship relationship) {
        this.relationship = relationship;
    }
}
