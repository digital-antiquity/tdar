package org.tdar.db.builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;

public class AbstractSqlTools {

    public String quote(String term) {
        return quote(term, true);
    }

    public String quote(String term_, boolean doubleQuote) {
        String term = term_;
        String chr = "\'";
        if (doubleQuote) {
            chr = "\"";
        } else {
            term = StringUtils.replace(term, "'", "''");
        }
        return " " + chr + term + chr + " ";
    }

    public void createInPart(StringBuilder sb, List<Object> inVals) {
        sb.append(" IN (");
        boolean first = true;
        for (Object inValue : inVals) {
            if (!first) {
                sb.append(", ");
            }
            appendValue(sb, inValue);
            first = false;
        }
        sb.append(") ");
    }

    protected void appendValue(StringBuilder sb, Object value) {
        if (value instanceof Number) {
            sb.append(value);
        } else {
            sb.append(quote(value.toString(), false));
        }
    }

    public static String getResultSetValueAsString(ResultSet result, int i, DataTableColumn column) throws SQLException {
        try {
            switch (column.getColumnDataType()) {
                case BOOLEAN:
                    return Boolean.toString(result.getBoolean(i));
                case DOUBLE:
                    return Double.toString(result.getDouble(i));
                case BIGINT:
                    return Long.toString(result.getLong(i));
                case DATE:
                case DATETIME:
                    return result.getDate(i).toString();
                default:
                    return result.getString(i);
            }
        } catch (Exception e) {
            // FIXME: this may cause an issue with a coded value that's numeric
            if (column.getColumnEncodingType() == DataTableColumnEncodingType.CODED_VALUE) {
                return result.getString(i);
            }
        }
        return null;
    }

    public void joinListWithCommas(StringBuilder sb, List<String> list, boolean quote) {
        boolean first = true;
        for (String col : list) {
            if (!first) {
                sb.append(", ");
            }
            if (quote) {
                if (col != null) {
                    sb.append(quote(col));
                } else {
                    sb.append("NULL");
                }
            } else {
                sb.append(col);
            }
            first = false;
        }
    }
}
