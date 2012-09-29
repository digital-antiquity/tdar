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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

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
@XmlRootElement
public class DataTableColumn extends Persistable.Sequence<DataTableColumn> implements Validatable {

    private static final String COLUMN_S_IS_NOT_VALID_BECAUSE = "Column %s is not valid because %s";

    private static final long serialVersionUID = 430090539610139732L;

    public static final DataTableColumn TDAR_ROW_ID = new DataTableColumn() {

        private static final long serialVersionUID = 3518018865128797773L;

        @Override
        public String getDisplayName() {
            return "Row Id";
        }

        @Override
        public String getName() {
            return TargetDatabase.TDAR_ID_COLUMN;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public Long getId() {
            return -1l;
        }
    };

    @ManyToOne(optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "data_table_id")
    private DataTable dataTable;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "display_name")
    @Field
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
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

    private transient CategoryVariable tempSubCategoryVariable;

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

    @Column
    private Boolean mappingColumn = false;

    @Column
    private String delimiterValue;

    @Column
    private Boolean ignoreFileExtension = Boolean.TRUE;

    @Column
    private Boolean visible = Boolean.TRUE;

    @Transient
    private Map<Long, List<String>> ontologyNodeIdToValuesMap;

    @Transient
    private Integer length = -1;

    public DataTableColumn() {
    }

    @XmlElement(name = "dataTableRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
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

    @XmlElement(name = "ontologyRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Ontology getDefaultOntology() {
        return defaultOntology;
    }

    public void setDefaultOntology(Ontology defaultOntology) {
        this.defaultOntology = defaultOntology;
    }

    @XmlElement(name = "codingSheetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CodingSheet getDefaultCodingSheet() {
        return defaultCodingSheet;
    }

    public void setDefaultCodingSheet(CodingSheet defaultCodingSheet) {
        this.defaultCodingSheet = defaultCodingSheet;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    @XmlElementWrapper(name = "dataValueOntologyNodeMappings")
    @XmlElement(name = "dataValueOntologyNodeMapping")
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
        StringBuilder builder = new StringBuilder(name == null ? "null" : name).append(" - ")
                .append(columnDataType == null ? "null" : columnDataType)
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

    public String getDelimiterValue() {
        if (delimiterValue == "") {
            return null;
        }
        return delimiterValue;
    }

    public void setDelimiterValue(String delimiterValue) {
        this.delimiterValue = delimiterValue;
    }

    public Boolean getIgnoreFileExtension() {
        return ignoreFileExtension;
    }

    public Boolean isIgnoreFileExtension() {
        return ignoreFileExtension;
    }

    public void setIgnoreFileExtension(Boolean ignoreFileExtension) {
        this.ignoreFileExtension = ignoreFileExtension;
    }

    public void copyUserMetadataFrom(DataTableColumn column) {
        if (StringUtils.isNotBlank(column.getDisplayName())) { // NOT NULLABLE FIELD
            setDisplayName(column.getDisplayName());
        }
        setDescription(column.getDescription());
        // XXX: this should be set by the dataset conversion process
        // if (column.getColumnDataType() != null) { // NOT NULLABLE FIELD
        // setColumnDataType(column.getColumnDataType());
        // }

        if (column.getColumnEncodingType() != null) { // NOT NULLABLE FIELD
            setColumnEncodingType(column.getColumnEncodingType());
        }
        setMeasurementUnit(column.getMeasurementUnit());
        setMappingColumn(column.isMappingColumn());
        setDelimiterValue(column.getDelimiterValue());
        setIgnoreFileExtension(column.getIgnoreFileExtension());
    }

    public CategoryVariable getTempSubCategoryVariable() {
        return tempSubCategoryVariable;
    }

    public void setTempSubCategoryVariable(CategoryVariable tempSubCategoryVariable) {
        this.tempSubCategoryVariable = tempSubCategoryVariable;
    }

    public boolean isVisible() {
        if (visible == null) {
            return Boolean.TRUE;
        }
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    @JSONTransient
    public boolean isValidForController() {
        // not implemented
        return true;
    }

    @Override
    @JSONTransient
    public boolean isValid() {
        if (columnEncodingType == null) {
            throw new TdarRecoverableRuntimeException(String.format(COLUMN_S_IS_NOT_VALID_BECAUSE, getName(), "no encoding type is specified"));
        }
        switch (columnEncodingType) {
            case CODED_VALUE:
                if (getDefaultCodingSheet() == null) {
                    throw new TdarRecoverableRuntimeException(String.format(COLUMN_S_IS_NOT_VALID_BECAUSE, getName(),
                            "no coding sheet was specified for 'coded value'"));
                }
                break;
            case MEASUREMENT:
                if (measurementUnit == null) {
                    throw new TdarRecoverableRuntimeException(String.format(COLUMN_S_IS_NOT_VALID_BECAUSE, getName(), "no measurement unit was specified"));
                }
                // FIXME: Not 100% sure this is correct with the NUMERIC check
                if (columnDataType == null || !columnDataType.isNumeric()) {
                    throw new TdarRecoverableRuntimeException(String.format(COLUMN_S_IS_NOT_VALID_BECAUSE, getName(), "measurement unit was not numeric"));
                }
                break;
            case COUNT:
                // FIXME: Not 100% sure this is correct with the NUMERIC check
                if (columnDataType == null || !columnDataType.isNumeric()) {
                    throw new TdarRecoverableRuntimeException(String.format(COLUMN_S_IS_NOT_VALID_BECAUSE, getName(), "count was not numeric"));
                }
            case UNCODED_VALUE:
        }
        return true;
    }

    public Boolean isMappingColumn() {
        return mappingColumn;
    }

    public void setMappingColumn(Boolean mappingColumn) {
        this.mappingColumn = mappingColumn;
    }

    public boolean hasDifferentMappingMetadata(DataTableColumn column) {
        logger.trace("delim: '{}' - '{}'", getDelimiterValue(), column.getDelimiterValue());
        logger.trace("mapping: {} - {}", isMappingColumn(), column.isMappingColumn());
        logger.trace("extension: {} - {}", getIgnoreFileExtension(), column.getIgnoreFileExtension());
        return ! (StringUtils.equals(getDelimiterValue(), column.getDelimiterValue()) &&
                ObjectUtils.equals(getIgnoreFileExtension(), column.getIgnoreFileExtension()) &&
                ObjectUtils.equals(isMappingColumn(), column.isMappingColumn()));
    }

    @Transient
    @JSONTransient
    @XmlTransient
    public String getJsSimpleName() {
        return getName().replaceAll("[\\s\\,\"\']", "_");
    }
}
