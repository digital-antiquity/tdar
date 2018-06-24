package org.tdar.db.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for building complex SQL statements for Data Integration. Long
 * term, this would be great to get it to use parameterized sql statements
 * 
 * @author abrin
 *
 */
public class SqlSelectBuilder extends SqlTools implements Serializable {

    private static final long serialVersionUID = -1875170201260652139L;
    private List<String> columns = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private List<String> orderBy = new ArrayList<>();
    private List<String> tableNames = new ArrayList<>();
    private List<WhereCondition> where = new ArrayList<>();
    private String stringSelectValue;
    private String countColumn;
    private boolean distinct = false;

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
        StringBuilder sb = new StringBuilder("SELECT ");
        buildColumnList(sb);
        buildFromClause(sb);
        orderAndAggregate(sb);
        return sb.toString();
    }

    private void orderAndAggregate(StringBuilder sb) {
        appendAndJoin(sb, " GROUP BY ", groupBy);
        appendAndJoin(sb, " ORDER BY ", orderBy);
    }

    private void buildFromClause(StringBuilder sb) {
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
    }

    private void buildColumnList(StringBuilder sb) {
        if (distinct) {
            sb.append("DISTINCT ");
        }
        boolean hasColumns = false;
        if (StringUtils.isNotBlank(stringSelectValue)) {
            sb.append(quote(stringSelectValue, false));
            sb.append(", ");
            hasColumns = true;
        }
        if (StringUtils.isNotBlank(countColumn)) {
            hasColumns = true;
            sb.append("count(").append(quote(countColumn)).append(")");
            if (CollectionUtils.isNotEmpty(columns)
                    || StringUtils.isNotBlank(stringSelectValue)) {
                sb.append(", ");
            }
        }
        if (CollectionUtils.isNotEmpty(columns)) {
            hasColumns = true;
            joinListWithCommas(sb, columns, true);
        }
        if (!hasColumns) {
            sb.append(" * ");
        }
    }

    private void appendAndJoin(StringBuilder sb, String prefix, List<String> cols) {
        if (CollectionUtils.isNotEmpty(cols)) {
            sb.append(prefix);
            joinListWithCommas(sb, cols, true);
        }
    }

    public void setStringSelectValue(String txt) {
        this.stringSelectValue = txt;
    }

    public List<String> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<String> orderBy) {
        this.orderBy = orderBy;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    public String getCountColumn() {
        return countColumn;
    }

    public void setCountColumn(String countColumn) {
        this.countColumn = countColumn;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void evaluateWhereForEmpty() {
        boolean onlyEmptyWhere = true;
        for (WhereCondition cond : getWhere()) {
            if (CollectionUtils.isNotEmpty(cond.getInValues()) || CollectionUtils.isNotEmpty(cond.getMoreInValues())) {
                onlyEmptyWhere = false;
            }
        }

        if (onlyEmptyWhere) {
            getWhere().clear();
        }
    }

}
