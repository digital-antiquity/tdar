package org.tdar.db.conversion.analyzers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.TDataTableColumn;

public class DoubleAnalyzer implements ColumnAnalyzer<Double> {

    private Map<Double,Integer> values = new HashMap<>();
    
    /**
     * Get mapped @link DataTableColumnType
     */
    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.DOUBLE;
    }

    /**
     * Analyze whether the String can be mapped to a Double
     */
    @Override
    public boolean analyze(String value, TDataTableColumn column, int row) {
        if (value == null) {
            return true;
        }
        if ("".equals(value)) {
            return true;
        }
        Double dval = null;
        try {
            dval = Double.parseDouble(value);
        } catch (NumberFormatException nfx) {
            return false;
        }

        String lastChar = value.substring(value.length() - 1);
        // handles cases like "1F" which Double.parseDouble handles but cannot be casted to double in postgres
        if (StringUtils.isAlpha(lastChar)) {
            return false;
        }

        Integer def = values.getOrDefault(dval, 0);
        values.put(dval, def + 1);
 
        return true;
    }

    /**
     * For a double, always 0
     */
    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Map<Double,Integer> getValues() {
        return values;
    }
}