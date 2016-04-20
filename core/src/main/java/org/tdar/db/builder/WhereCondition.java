package org.tdar.db.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class WhereCondition extends AbstractSqlTools implements Serializable {

    private static final long serialVersionUID = -5257989550031533775L;

    public enum ValueCondition {
        EQUALS, IN, IS_NOT, NOT_EQUALS;
    }

    public enum Condition {
        AND, OR;
    }

    private String column;
    private ValueCondition valueCondition = ValueCondition.EQUALS;
    private Object value;
    private Condition condition = Condition.AND;
    private List<Object> inValues = new ArrayList<>();
    // THIS MAY SEEM SILLY, but adding to be clearer when the SQL is produced what the second set of unmapped values is vs. the selected set  
    private List<Object> moreInValues = new ArrayList<>();
    private boolean includeNullsWithInQuery;

    public WhereCondition(String name) {
        this.column = name;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public ValueCondition getValueCondition() {
        return valueCondition;
    }

    public void setValueCondition(ValueCondition condition) {
        this.valueCondition = condition;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<Object> getInValues() {
        return inValues;
    }

    public void setInValues(List<Object> inValues) {
        this.inValues = inValues;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setIncludeNulls(boolean b) {
        this.includeNullsWithInQuery = b;
    }

    public String toSql() {
        StringBuilder sb = new StringBuilder(" ");
        if (includeNullsWithInQuery) {
            sb.append(" (");
        }
        sb.append(quote(column));
        if (CollectionUtils.isEmpty(inValues) && CollectionUtils.isEmpty(moreInValues)) {
            buildSimpleCondition(sb);
        } else {
            if (!CollectionUtils.isEmpty(inValues)) {
                createInPart(sb, getInValues());
            }
            if (!CollectionUtils.isEmpty(moreInValues)) {
                if (!CollectionUtils.isEmpty(inValues)) {
                    sb.append(" OR ");
                }
                createInPart(sb, getMoreInValues());
            }
            if (includeNullsWithInQuery) {
                sb.append(" OR ").append(quote(column)).append(" IS NULL");
            }
        }
        if (includeNullsWithInQuery) {
            sb.append(") ");
        }

        return sb.toString();
    }

    private void buildSimpleCondition(StringBuilder sb) {
        sb.append(" ");
        if (getValue() == null) {
            if (valueCondition == ValueCondition.EQUALS) {
                sb.append("IS NULL");
            } else {
                sb.append("IS NOT NULL");
            }
        } else {
            switch (valueCondition) {
                case EQUALS:
                    sb.append("=");
                    break;
                case IN:
                    sb.append(ValueCondition.IN.name());
                    break;
                case NOT_EQUALS:
                    sb.append("!=");
                    break;
                default:
                    break;

            }
            appendValue(sb, getValue());
        }
    }

    public List<Object> getMoreInValues() {
        return moreInValues;
    }

    public void setMoreInValues(List<Object> moreInValues) {
        this.moreInValues = moreInValues;
    }

}
