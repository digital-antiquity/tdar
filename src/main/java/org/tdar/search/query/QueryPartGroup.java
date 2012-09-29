/**
 * 
 */
package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.QueryParser.Operator;

/**
 * @author Adam Brin
 * 
 */
public class QueryPartGroup implements QueryPart {
    List<QueryPart> parts = new ArrayList<QueryPart>();
    Operator operator = Operator.AND;

    public List<QueryPart> getParts() {
        return parts;
    }

    public void setParts(List<QueryPart> parts) {
        this.parts = parts;
    }

    public void addPart(List<QueryPart> parts) {
        this.parts.addAll(parts);
    }

    public void addPart(QueryPart part) {
        this.parts.add(part);
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator or) {
        this.operator = or;
    }

    public boolean isEmpty() {
        return this.parts.isEmpty();
    }

    @Override
    public String generateQueryString() {
        StringBuffer buff = new StringBuffer();
        buff.append("( ");
        for (int i = 0; i < parts.size(); i++) {
            buff.append(parts.get(i).generateQueryString());
            if (i + 1 < parts.size()) {
                buff.append(" ");
                buff.append(operator.toString());
                buff.append(" ");
            }
        }
        buff.append(" ) ");
        return buff.toString();
    }

}
