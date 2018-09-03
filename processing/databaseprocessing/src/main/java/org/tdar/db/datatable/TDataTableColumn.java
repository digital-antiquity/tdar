package org.tdar.db.datatable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metadata for a column in a data table.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TDataTableColumn implements Serializable, ImportColumn {

    private static final long serialVersionUID = 8546010007954571579L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String TDAR_ID_COLUMN = "id_row_tdar";

    public static final ImportColumn TDAR_ROW_ID = new TDataTableColumn() {

        private static final long serialVersionUID = 3518018865128797773L;

        @Override
        public String getDisplayName() {
            return "Row Id";
        }

        @Override
        public String getName() {
            return TDAR_ID_COLUMN;
        }

        @Override
        public Integer getImportOrder() {
            return -1;
        }
    };

    private String name;
    private String displayName;
    private String description;
    private DataTableColumnType columnDataType = DataTableColumnType.VARCHAR;
    private Integer importOrder;
    private Integer sequenceNumber;
    private Integer length = -1;
    private Set<String> values = new HashSet<>();
    private Set<Integer> intValues = new HashSet<>();
    private Set<Double> floatValues = new HashSet<>();


    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getColumnDataType()
     */
    @Override
    public DataTableColumnType getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(DataTableColumnType type) {
        this.columnDataType = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", name, columnDataType);
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getLength()
     */
    @Override
    public Integer getLength() {
        return length;
    }

    @XmlTransient
    public String getPrettyDisplayName() {
        String displayName = getDisplayName().replaceAll(" (?i)Ontology", "");
        displayName = StringUtils.replace(displayName, "  ", " ");
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getImportOrder()
     */
    @Override
    public Integer getImportOrder() {
        return importOrder;
    }

    public void setImportOrder(Integer importOrder) {
        this.importOrder = importOrder;
    }

    /* (non-Javadoc)
     * @see org.tdar.datatable.ImportColumn#getSequenceNumber()
     */
    @Override
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public int compareToBySequenceNumber(ImportColumn b) {
        if ((sequenceNumber == null) || (b.getSequenceNumber() == null)) {
            return 0;
        }
        return sequenceNumber.compareTo(b.getSequenceNumber());
    }

    @Override
    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        this.values = values;
    }

    public Set<Integer> getIntValues() {
        return intValues;
    }

    public void setIntValues(Set<Integer> intValues) {
        this.intValues = intValues;
    }

    public Set<Double> getFloatValues() {
        return floatValues;
    }

    public void setFloatValues(Set<Double> floatValues) {
        this.floatValues = floatValues;
    }
}
