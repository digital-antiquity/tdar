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
import javax.persistence.JoinColumn;
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

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

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
public class DataTable extends Persistable.Base {

    private static final long serialVersionUID = -4875482933981074863L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, optional = false)
    private Dataset dataset;

    @Column(nullable = false)
    private String name;

    @Field
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private boolean aggregated;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    @IndexedEmbedded
    private CategoryVariable categoryVariable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTable")
    @IndexedEmbedded
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "dataTableColumns")
    @XmlElement(name = "dataTableColumn")
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
    public List<DataTableColumn> getSortedDataTableColumns() {
        return getSortedDataTableColumns(new Comparator<DataTableColumn>() {
            public int compare(DataTableColumn a, DataTableColumn b) {
                int comparison = a.compareTo(b);
                if (comparison == 0) {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return comparison;
            }
        });
    }

    public List<DataTableColumn> getSortedDataTableColumns(Comparator<DataTableColumn> comparator) {
        ArrayList<DataTableColumn> sortedDataTableColumns = new ArrayList<DataTableColumn>(dataTableColumns);
        Collections.sort(sortedDataTableColumns, comparator);
        return sortedDataTableColumns;
    }

    /**
     * List all the columns in this table and any left-joined tables (including recursively left-joined)
     * 
     * @return list of columns
     */
    @XmlTransient
    public List<DataTableColumn> getLeftJoinColumns() {
        ArrayList<DataTableColumn> leftJoinColumns = new ArrayList<DataTableColumn>(getSortedDataTableColumns());
        for (DataTableRelationship r : getRelationships()) {
            // FIXME getForeignTable() could use getColumnRelationships().iterator().next().getForeignColumn().getDataTable()
            // Include fields from related tables unless they're on the "many" side of a one-to-many relationship
            if (this.equals(r.getLocalTable()) && r.getType() == DataTableColumnRelationshipType.MANY_TO_ONE) {
                // this is the "local" table in a many-to-one relationship, so this is the "many" side, and we can include the "foreign" table's fields
                leftJoinColumns.addAll(r.getForeignTable().getLeftJoinColumns());
            } else if (this.equals(r.getForeignTable()) && r.getType() == DataTableColumnRelationshipType.ONE_TO_MANY) {
                // this is the "foreign" table in a one-to-many relationship, so this is the "many" side, and we can include the "local" table's fields
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
    private Set<DataTableRelationship> getRelationships() {
        Set<DataTableRelationship> relationships = new HashSet<DataTableRelationship>();
        for (DataTableRelationship r : dataset.getRelationships()) {
            DataTable localTable = r.getLocalTable();
            DataTable foreignTable = r.getForeignTable();
            // return the relationship if this table is either the relationship's foreign or local table
            if (this.equals(r.getLocalTable()) || this.equals(r.getForeignTable())) {
                relationships.add(r);
            }
        }
        return relationships;
    }

    public boolean isAggregated() {
        return aggregated;
    }

    public void setAggregated(boolean aggregated) {
        this.aggregated = aggregated;
    }

    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    public void setCategoryVariable(CategoryVariable categoryVariable) {
        this.categoryVariable = categoryVariable;
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
        if (nameToColumnMap == null || ObjectUtils.notEqual(dataTableColumnHashCode, getDataTableColumns().hashCode())) {
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
        if (displayNameToColumnMap == null || ObjectUtils.notEqual(dataTableColumnHashCode, getDataTableColumns().hashCode())) {
            initializeNameToColumnMap();
        }
        return displayNameToColumnMap.get(name);
    }

    @Transient
    public DataTableColumn getColumnById(Long id) {
        if (idToColumnMap == null || ObjectUtils.notEqual(dataTableColumnHashCode, getDataTableColumns().hashCode())) {
            initializeNameToColumnMap();
        }
        return idToColumnMap.get(id);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInternalName() {
        return getName().replaceAll("^((\\w+)_)(\\d+)(_?)", "");
    }
}
