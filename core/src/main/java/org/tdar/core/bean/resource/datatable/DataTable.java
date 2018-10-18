package org.tdar.core.bean.resource.datatable;

import java.util.ArrayList;
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
import org.tdar.db.datatable.ImportTable;
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
public class DataTable extends AbstractPersistable implements ImportTable<DataTableColumn> {

    private static final long serialVersionUID = -4875482933981074863L;

    @ManyToOne(optional = false)
    @Deprecated() // used only in data integration
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

    @Column(name = "import_order")
    private Integer importOrder;

    private transient Map<String, DataTableColumn> nameToColumnMap;
    private transient Map<Long, DataTableColumn> idToColumnMap;
    private transient Map<String, DataTableColumn> displayNameToColumnMap;
    private transient int dataTableColumnHashCode = -1;
    private transient Long datasetId;
    private transient String datasetName;
    
//    @XmlElement(name = "resourceRef")
//    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
//    @Deprecated
//    public Dataset getDataset() {
//        return dataset;
//    }
//
//    @Deprecated
//    public void setDataset(Dataset dataset) {
//        this.dataset = dataset;
//    }

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

//    @JsonView(JsonIntegrationDetailsFilter.class)
//    public String getDatasetTitle() {
//        return getDataset().getTitle();
//    }
//
//    @JsonView(JsonIntegrationDetailsFilter.class)
//    public Long getDatasetId() {
//        return getDataset().getId();
//    }

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

    public Integer getImportOrder() {
        return importOrder;
    }

    public void setImportOrder(Integer importOrder) {
        this.importOrder = importOrder;
    }

    @JsonView(value = { JsonIntegrationFilter.class, JsonIntegrationDetailsFilter.class })
    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    @JsonView(value = { JsonIntegrationFilter.class, JsonIntegrationDetailsFilter.class })
    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

}
