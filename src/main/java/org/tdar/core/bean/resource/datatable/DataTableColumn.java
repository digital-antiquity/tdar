package org.tdar.core.bean.resource.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
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
            return -1L;
        }
    };

    @ManyToOne(optional = false)
    // , cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "data_table_id")
    @Index(name = "data_table_column_data_table_id_idx")
    private DataTable dataTable;

    @Column(nullable = false)
    @Length(max = 255)
    private String name;

    @Column(nullable = false, name = "display_name")
    @Field
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    @Length(max = 255)
    private String displayName;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "column_data_type", length = 255)
    private DataTableColumnType columnDataType = DataTableColumnType.VARCHAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "column_encoding_type", length = 25)
    private DataTableColumnEncodingType columnEncodingType;

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    private CategoryVariable categoryVariable;

    @ManyToOne
    @JoinColumn(name = "default_ontology_id")
    @Index(name = "data_table_column_default_ontology_id_idx")
    private Ontology defaultOntology;

    @ManyToOne
    @JoinColumn(name = "default_coding_sheet_id")
    @Index(name = "data_table_column_default_coding_sheet_id_idx")
    private CodingSheet defaultCodingSheet;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", length = 25)
    private MeasurementUnit measurementUnit;

    @Column(columnDefinition = "boolean default FALSE")
    private boolean mappingColumn = false;

    @Column
    @Length(max = 4)
    private String delimiterValue;

    @Column(columnDefinition = "boolean default TRUE")
    private boolean ignoreFileExtension = true;

    @Column(columnDefinition = "boolean default TRUE")
    private boolean visible = true;

    @Transient
    private Map<Long, List<String>> ontologyNodeIdToValuesMap;

    @Transient
    private Integer length = -1;

    // XXX: only used for data transfer from the web layer.
    @Transient
    private transient CategoryVariable tempSubCategoryVariable;

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
        if (defaultOntology != null) {
            setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        }
    }

    @XmlElement(name = "codingSheetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CodingSheet getDefaultCodingSheet() {
        return defaultCodingSheet;
    }

    public void setDefaultCodingSheet(CodingSheet defaultCodingSheet) {
        this.defaultCodingSheet = defaultCodingSheet;
        if (defaultCodingSheet != null) {
            setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        }
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    @Transient
    public Map<String, OntologyNode> getValueToOntologyNodeMap() {
        CodingSheet codingSheet = getDefaultCodingSheet();
        if (codingSheet == null) {
            return Collections.emptyMap();
        }
        return codingSheet.getTermToOntologyNodeMap();
    }

    @Transient
    public Map<String, List<Long>> getValueToOntologyNodeIdMap() {
        CodingSheet codingSheet = getDefaultCodingSheet();
        if (codingSheet == null) {
            return Collections.emptyMap();
        }
        return codingSheet.getTermToOntologyNodeIdMap();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s", name, columnDataType, getId());
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
        if (StringUtils.isEmpty(delimiterValue)) {
            return null;
        }
        return delimiterValue;
    }

    public void setDelimiterValue(String delimiterValue) {
        this.delimiterValue = delimiterValue;
    }

    public boolean isIgnoreFileExtension() {
        return ignoreFileExtension;
    }

    public void setIgnoreFileExtension(boolean ignoreFileExtension) {
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
        setIgnoreFileExtension(column.isIgnoreFileExtension());
    }

    public CategoryVariable getTempSubCategoryVariable() {
        return tempSubCategoryVariable;
    }

    public void setTempSubCategoryVariable(CategoryVariable tempSubCategoryVariable) {
        this.tempSubCategoryVariable = tempSubCategoryVariable;
    }

    public boolean isVisible() {
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

    public boolean isMappingColumn() {
        return mappingColumn;
    }

    public void setMappingColumn(boolean mappingColumn) {
        this.mappingColumn = mappingColumn;
    }

    public boolean hasDifferentMappingMetadata(DataTableColumn column) {
        logger.trace("delim: '{}' - '{}'", getDelimiterValue(), column.getDelimiterValue());
        logger.trace("mapping: {} - {}", isMappingColumn(), column.isMappingColumn());
        logger.trace("extension: {} - {}", isIgnoreFileExtension(), column.isIgnoreFileExtension());
        return !(StringUtils.equals(getDelimiterValue(), column.getDelimiterValue()) &&
                ObjectUtils.equals(isIgnoreFileExtension(), column.isIgnoreFileExtension()) && ObjectUtils.equals(isMappingColumn(), column.isMappingColumn()));
    }

    @Transient
    @JSONTransient
    @XmlTransient
    public String getJsSimpleName() {
        return getName().replaceAll("[\\s\\,\"\']", "_");
    }

    public List<String> getMappedDataValues(OntologyNode node) {
        ArrayList<String> values = new ArrayList<String>();
        for (CodingRule rule : getDefaultCodingSheet().getCodingRules()) {
            if (ObjectUtils.equals(node, rule.getOntologyNode())) {
                values.add(rule.getTerm());
            }
        }
        return values;
    }

}
