package org.tdar.db.conversion.analyzers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.TDataTableColumn;

/**
 * Determines whether the column contains a Long value
 * 
 * @author abrin
 * 
 */
public class LongAnalyzer implements ColumnAnalyzer<Long> {
    private Map<Long,Integer> values = new HashMap<>();

    /**
     * Get mapped @link DataTableColumnType
     */
    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.BIGINT;
    }

    /**
     * Analyze if the String is a Long
     */
    @Override
    public boolean analyze(String value, TDataTableColumn column, int row) {
        if (value == null) {
            return true;
        }
        if ("".equals(value)) {
            return true;
        }
        Long lval = null;
        try {
            lval = Long.parseLong(value);
        } catch (NumberFormatException nfx) {
            return false;
        }
        String lastChar = value.substring(value.length() - 1);
        // handles cases like "1D" which Double.parseDouble handles but cannot be casted to double in postgres
        if (StringUtils.isAlpha(lastChar)) {
            return false;
        }
        
        Integer def = values.getOrDefault(lval, 0);
        values.put(lval, def + 1);

        return true;
    }

    /**
     * Return the SQL declared length (for a long this is always 0
     * 
     */
    @Override
    public int getLength() {
        return 0;
    }
    
    @Override
    public Map<Long,Integer> getValues() {
        return values;
    }

}