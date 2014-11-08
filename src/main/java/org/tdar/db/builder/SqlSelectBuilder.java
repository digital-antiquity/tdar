package org.tdar.db.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for building complex SQL statements for Data Integration. Long term, this would be great to get it to use parameterized sql statements
 * @author abrin
 *
 */
public class SqlSelectBuilder extends AbstractSqlTools implements Serializable {

    private static final long serialVersionUID = -1875170201260652139L;
    private List<String> columns = new ArrayList<>();
    private List<String> tableNames = new ArrayList<>();
    private List<WhereCondition> where = new ArrayList<>();
    private String stringSelectValue;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public List<WhereCondition> getWhere() {
        return where;
    }

    public void setWhere(List<WhereCondition> where) {
        this.where = where;
    }

    public String toSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (StringUtils.isNotBlank(stringSelectValue)) {
            sb.append(quote(stringSelectValue, false));
            sb.append(", ");
        }
        joinListWithCommas(sb, columns, true);
        if (CollectionUtils.isEmpty(columns)) {
        	sb.append(" * ");
        }
        sb.append(" FROM ");
        joinListWithCommas(sb, tableNames, false);
        boolean first = true;
        if (CollectionUtils.isNotEmpty(where)) {
            sb.append(" where ");
        }
        for (WhereCondition whereCond : where) {
            if (!first) {
                sb.append(" ");
                sb.append(whereCond.getCondition().name());
                sb.append(" ");
            }
            first = false;
            sb.append(whereCond.toSql());
        }
        return sb.toString();
    }

    public void setStringSelectValue(String txt) {
        this.stringSelectValue = txt;
    }

}
