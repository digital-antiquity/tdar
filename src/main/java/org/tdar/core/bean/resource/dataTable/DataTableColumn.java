package org.tdar.core.bean.resource.dataTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;

/**
 * $Id$
 * 
 * Metadata for a column in a data table.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Table(name = "data_table_column")
public class DataTableColumn extends Persistable.Base implements Comparable<DataTableColumn> {

    private static final long serialVersionUID = 430090539610139732L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "data_table_id")
    private DataTable dataTable;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "display_name")
    private String displayName;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "column_data_type")
    private DataTableColumnType columnDataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "column_encoding_type")
    private DataTableColumnEncodingType columnEncodingType;

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    private CategoryVariable categoryVariable;

    @ManyToOne
    @JoinColumn(name = "default_ontology_id")
    private Ontology defaultOntology;

    @ManyToOne
    @JoinColumn(name = "default_coding_sheet_id")
    private CodingSheet defaultCodingSheet;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit")
    private MeasurementUnit measurementUnit;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTableColumn")
    private List<DataValueOntologyNodeMapping> valueToOntologyNodeMapping = new ArrayList<DataValueOntologyNodeMapping>();

    @Transient
    private Map<Long, List<String>> ontologyNodeIdToValuesMap;

    @Transient
    private Integer length = -1;

    public DataTableColumn() {
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataTableColumnType getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(DataTableColumnType type) {
        this.columnDataType = type;
    }

    public DataTableColumnEncodingType getColumnEncodingType() {
        return columnEncodingType;
    }

    public void setColumnEncodingType(DataTableColumnEncodingType columnEncodingType) {
        this.columnEncodingType = columnEncodingType;
    }

    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    public void setCategoryVariable(CategoryVariable categoryVariable) {
        this.categoryVariable = categoryVariable;
    }

    public Ontology getDefaultOntology() {
        return defaultOntology;
    }

    public void setDefaultOntology(Ontology defaultOntology) {
        this.defaultOntology = defaultOntology;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public int compareTo(DataTableColumn o) {
        return getName().compareTo(o.getName());
    }

    public CodingSheet getDefaultCodingSheet() {
        return defaultCodingSheet;
    }

    public void setDefaultCodingSheet(CodingSheet defaultCodingSheet) {
        this.defaultCodingSheet = defaultCodingSheet;
    }

    public List<DataValueOntologyNodeMapping> getValueToOntologyNodeMapping() {
        return valueToOntologyNodeMapping;
    }

    public void setValueToOntologyNodeMapping(List<DataValueOntologyNodeMapping> valueToOntologyNodeMapping) {
        this.valueToOntologyNodeMapping = valueToOntologyNodeMapping;
    }

    @Transient
    public Map<String, Long> getValueToOntologyNodeIdMap() {
        if (CollectionUtils.isEmpty(valueToOntologyNodeMapping)) {
            return Collections.emptyMap();
        }
        HashMap<String, Long> map = new HashMap<String, Long>();
        for (DataValueOntologyNodeMapping mapping : valueToOntologyNodeMapping) {
            map.put(mapping.getDataValue(), mapping.getOntologyNode().getId());
        }
        return map;
    }

    @Transient
    public Map<String, OntologyNode> getValueToOntologyNodeMap() {
        HashMap<String, OntologyNode> map = new HashMap<String, OntologyNode>();
        for (DataValueOntologyNodeMapping mapping : valueToOntologyNodeMapping) {
            map.put(mapping.getDataValue(), mapping.getOntologyNode());
        }
        return map;
    }

    @Transient
    public Map<Long, List<String>> getOntologyNodeIdToValuesMap() {
        if (ontologyNodeIdToValuesMap == null) {
            HashMap<Long, List<String>> map = new HashMap<Long, List<String>>();
            for (DataValueOntologyNodeMapping mapping : valueToOntologyNodeMapping) {
                Long nodeId = mapping.getOntologyNode().getId();
                List<String> mappedValues = map.get(nodeId);
                if (mappedValues == null) {
                    mappedValues = new ArrayList<String>();
                    map.put(nodeId, mappedValues);
                }
                mappedValues.add(mapping.getDataValue());
            }
        }
        return ontologyNodeIdToValuesMap;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(name).append(" - ")
                        .append(columnDataType)
                        .append(' ')
                        .append(getId() == null ? -1 : getId());
        return builder.toString();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getLength() {
        return length;
    }

}
