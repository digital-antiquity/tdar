package org.tdar.core.bean.resource.dataTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;

import org.hibernate.annotations.Type;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Dataset;

/**
 * 
 * A representation of a data table in tDAR.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Id$
 */
@Entity
@Table(name = "data_table")
public class DataTable extends Persistable.Base {

    private static final long serialVersionUID = -4875482933981074863L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, optional = false)
    private Dataset dataset;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private boolean aggregated;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    private CategoryVariable categoryVariable;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTable")
    private List<DataTableColumn> dataTableColumns = new ArrayList<DataTableColumn>();

    /**
     * Default constructor, needed by JPA/Hibernate.
     */
    public DataTable() {
    }

    /**
     * Constructs a data table associated with the given {@link Dataset}.
     * 
     * @param dataset 
     */
    public DataTable(Dataset dataset) {
        setDataset(dataset);
    }

    @XmlIDREF
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
                if (a.getSequenceNumber() == null || b.getSequenceNumber() == null) {
                    return a.getDisplayName().compareTo(b.getDisplayName());
                }
                return a.getSequenceNumber().compareTo(b.getSequenceNumber());
            }
       });
    }

    public List<DataTableColumn> getSortedDataTableColumns(Comparator<DataTableColumn> comparator) {
        ArrayList<DataTableColumn> sortedDataTableColumns = new ArrayList<DataTableColumn>(dataTableColumns);
        Collections.sort(sortedDataTableColumns, comparator);
        return sortedDataTableColumns;
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
        for (DataTableColumn column : getDataTableColumns()) {
            if (column.getName().equals(name))
                return column;
        }
        return null;
    }

    @Transient
    public DataTableColumn getColumnByDisplayName(String name) {
        for (DataTableColumn column : getDataTableColumns()) {
            if (column.getDisplayName().equals(name))
                return column;
        }
        return null;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
