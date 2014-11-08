package org.tdar.db.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;


public class WhereCondition extends AbstractSqlTools implements Serializable {

    private static final long serialVersionUID = -5257989550031533775L;

    enum ValueCondition {
        EQUALS, IN;
    }

    enum Condition {
        AND, OR;
    }

    private String column;
    private ValueCondition valueCondition;
    private Object value;
    private Condition condition = Condition.AND;
    private List<Object> inValues = new ArrayList<>();

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
        // TODO Auto-generated method stub

    }

    public String toSql() {
        StringBuilder sb = new StringBuilder(" ");
        sb.append(quote(column));
        if (CollectionUtils.isEmpty(inValues)) {
            sb.append(" ");
            if (getValue() == null) {
                sb.append("IS NULL");
            } else {
                sb.append(ValueCondition.EQUALS.name());
                appendValue(sb, getValue());
            }
        } else {
            createInPart(sb, getInValues());
            sb.append(") ");
        }
        return sb.toString();
    }

}
