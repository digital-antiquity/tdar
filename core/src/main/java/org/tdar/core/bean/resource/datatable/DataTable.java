package org.tdar.core.bean.resource.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * <p>
 * A DataTable belonging to a Dataset and carrying a list of ordered DataTableColumns and descriptive metadata.
 * </p>
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Entity
@Table(name = "data_table")
@XmlRootElement
public class DataTable extends AbstractPersistable {

    private static final long serialVersionUID = -4875482933981074863L;

    @ManyToOne(optional = false)
    private Dataset dataset;

    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Column(name = "display_name")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String displayName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTable")
    private List<DataTableColumn> dataTableColumns = new ArrayList<DataTableColumn>();

    private transient Map<String, DataTableColumn> nameToColumnMap;
    private transient Map<Long, DataTableColumn> idToColumnMap;
    private transient Map<String, DataTableColumn> displayNameToColumnMap;
    private transient int dataTableColumnHashCode = -1;

    @XmlElement(name = "resourceRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @JsonView(JsonIntegrationFilter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "dataTableColumns")
    @XmlElement(name = "dataTableColumn")
    @JsonView(JsonIntegrationDetailsFilter.class)
    public List<DataTableColumn> getDataTableColumns() {
        return dataTableColumns;
    }

    public void setDataTableColumns(List<DataTableColumn> dataTableColumns) {
        this.dataTableColumns = dataTableColumns;
    }

    /**
     * Get the data table columns sorted in the ascending order of column names.
     * 
     * @return
     */
    @XmlTransient
    public List<DataTableColumn> getSortedDataTableColumns() {
        return getSortedDataTableColumns(new Comparator<DataTableColumn>() {
            @Override
            public int compare(DataTableColumn a, DataTableColumn b) {
                int comparison = a.compareTo(b);
                if (comparison == 0) {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return comparison;
            }
        });
    }

    /**
     * Get the data table columns sorted in the ascending order of sequence_number which should be the import order if available
     * 
     * @return
     */
    @XmlTransient
    @JsonView(JsonIntegrationFilter.class)
    public List<DataTableColumn> getSortedDataTableColumnsByImportOrder() {
        return getSortedDataTableColumns(new Comparator<DataTableColumn>() {
            @Override
            public int compare(DataTableColumn a, DataTableColumn b) {
                return ObjectUtils.compare(a.getSequenceNumber(), b.getSequenceNumber());
            }
        });
    }

    public List<DataTableColumn> getSortedDataTableColumns(Comparator<DataTableColumn> comparator) {
        ArrayList<DataTableColumn> sortedDataTableColumns = new ArrayList<DataTableColumn>(dataTableColumns);
        Collections.sort(sortedDataTableColumns, comparator);
        return sortedDataTableColumns;
    }

    /**
     * <p>
     * List all the columns in this table and any left-joined tables (including recursively left-joined)
     * <p>
     * Consider the following very unlikely scenario:
     * 
     * <pre>
     *   B
     *  / \
     * A   C-E-A
     *  \ /
     *   D
     * Table A reference Table B & D, B & D reference the same column in C, and C references E, which in turn references A...
     * </pre>
     * <p>
     * What still needs doing in this method is:
     * <ol>
     * <li>Adding a filter that excludes columns that are already in the result set (B-C and D-C). It would be nice if we could include them both using aliases
     * but that's not possible at this moment in time.
     * <li>to stop recursing if we detect tables that have already been visited (but if the columns referenced are not in the list of columns, to still add
     * them)
     * </ol>
     * <p>
     * There is an implicit assumption that the referenced foreign key's are in fact primary keys...
     * 
     * <p>
     * What still needs doing to support this method is:
     * <ol>
     * <li>The screen that displays the resultant tables/columns might need to be enhanced to ensure that the user doesn't become confused by columns with the
     * same name in multiple tables.
     * <li>The code that generates the SQL queries needs to be updated to perform the required joins.
     * </ol>
     * 
     * @return list of columns
     */
    @XmlTransient
    public List<DataTableColumn> getLeftJoinColumns() {
        ArrayList<DataTableColumn> leftJoinColumns = new ArrayList<DataTableColumn>(getSortedDataTableColumns());
        for (DataTableRelationship r : getRelationships()) {
            // Include fields from related tables unless they're on the "many" side of a one-to-many relationship
            if (this.equals(r.getLocalTable()) && (r.getType() != DataTableColumnRelationshipType.ONE_TO_MANY)) {
                // this is the "local" table in a many-to-one or one-to-one relationship,
                // so including the "foreign" table's fields will not increase the cardinality of this query
                leftJoinColumns.addAll(r.getForeignTable().getLeftJoinColumns());
            } else if (this.equals(r.getForeignTable()) && (r.getType() != DataTableColumnRelationshipType.MANY_TO_ONE)) {
                // this is the "foreign" table in a one-to-many or one-to-one relationship,
                // so including the "local" table's fields will not increase the cardinality of this query
                leftJoinColumns.addAll(r.getLocalTable().getLeftJoinColumns());
            }
        }
        return leftJoinColumns;
    }

    /**
     * The relationships in which this dataset is the local table
     * 
     * @return the set of relationships
     */
    @XmlTransient
    @Transient
    public Set<DataTableRelationship> getRelationships() {
        Set<DataTableRelationship> relationships = new HashSet<DataTableRelationship>();
        for (DataTableRelationship r : dataset.getRelationships()) {
            // return the relationship if this table is either the relationship's foreign or local table
            if (this.equals(r.getLocalTable()) || this.equals(r.getForeignTable())) {
                relationships.add(r);
            }
        }
        return relationships;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name).append(" - ").append(getId() == null ? -1 : getId());
        return builder.toString();
    }

    @Transient
    public List<String> getColumnNames() {
        List<String> columns = new ArrayList<String>();
        for (DataTableColumn column : getDataTableColumns()) {
            columns.add(column.getName());
        }
        return columns;
    }

    @Transient
    public DataTableColumn getColumnByName(String name) {
        if ((nameToColumnMap == null) || ObjectUtils.notEqual(dataTableColumnHashCode, getDataTableColumns().hashCode())) {
            initializeNameToColumnMap();
        }
        // NOTE: IF the HashCode is not implemented properly, on DataTableColumn, this may get out of sync
        return nameToColumnMap.get(name);
    }

    private void initializeNameToColumnMap() {
        nameToColumnMap = new HashMap<String, DataTableColumn>();
        displayNameToColumnMap = new HashMap<String, DataTableColumn>();
        idToColumnMap = new HashMap<Long, DataTableColumn>();

        // using the HashCode to detect changes to the map, and thus rebuid
        dataTableColumnHashCode = getDataTableColumns().hashCode();
        for (DataTableColumn column : getDataTableColumns()) {
            nameToColumnMap.put(column.getName(), column);
            displayNameToColumnMap.put(column.getDisplayName(), column);
            idToColumnMap.put(column.getId(), column);
        }
        nameToColumnMap.put(DataTableColumn.TDAR_ROW_ID.getName(), DataTableColumn.TDAR_ROW_ID);
        displayNameToColumnMap.put(DataTableColumn.TDAR_ROW_ID.getDisplayName(), DataTableColumn.TDAR_ROW_ID);
    }

    @Transient
    public DataTableColumn getColumnByDisplayName(String name) {
        if ((displayNameToColumnMap == null) || ObjectUtils.notEqual(dataTableColumnHashCode, getDataTableColumns().hashCode())) {
            initializeNameToColumnMap();
        }
        return displayNameToColumnMap.get(name);
    }

    @Transient
    public DataTableColumn getColumnById(Long id) {
        if ((idToColumnMap == null) || ObjectUtils.notEqual(dataTableColumnHashCode, getDataTableColumns().hashCode())) {
            initializeNameToColumnMap();
        }
        return idToColumnMap.get(id);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonView(value = { JsonIntegrationFilter.class, JsonIntegrationDetailsFilter.class })
    public String getDisplayName() {
        return displayName;
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public String getDatasetTitle() {
        return getDataset().getTitle();
    }

    public String getInternalName() {
        return getName().replaceAll("^((\\w+)_)(\\d+)(_?)", "");
    }

    @Transient
    public List<DataTableColumn> getColumnsWithOntologyMappings() {
        List<DataTableColumn> columns = new ArrayList<>();
        for (DataTableColumn column : getDataTableColumns()) {
            if (column.getMappedOntology() != null) {
                columns.add(column);
            }
        }
        return columns;
    }

    @Transient
    public List<DataTableColumn> getFilenameColumns() {
        List<DataTableColumn> columns = new ArrayList<>();
        for (DataTableColumn column : getDataTableColumns()) {
            if (column.isFilenameColumn()) {
                columns.add(column);
            }
        }
        return columns;
    }

}
