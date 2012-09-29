package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class QueryPartGroup implements QueryPart, QueryGroup {
    private List<QueryPart> parts = new ArrayList<QueryPart>();
    private Operator operator = Operator.AND;
    private boolean descriptionVisible = true;

    public boolean isDescriptionVisible() {
        return descriptionVisible;
    }

    public void setDescriptionVisible(boolean descriptionVisible) {
        this.descriptionVisible = descriptionVisible;
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public QueryPartGroup() {
    }

    public void clear() {
        parts.clear();
    }
    
    public QueryPartGroup(Operator or) {
        this.operator = or;
    }

    public QueryPartGroup(Operator or, List<QueryPart<?>> parts_) {
        setOperator(or);
        append(parts_);
    }

    public QueryPartGroup(Operator or, QueryPart<?>... parts_) {
        setOperator(or);
        append(Arrays.asList(parts_));
    }

    @SuppressWarnings("rawtypes")
    public List<QueryPart> getParts() {
        return parts;
    }

    public void append(List<? extends QueryPart> parts) {
        for (QueryPart part : parts) {
            append(part);
        }
    }

    public void append(QueryPart q) {
        if (q == null || q.isEmpty())
            return;
        if (q instanceof QueryPartGroup) {
            QueryPartGroup group = (QueryPartGroup) q;
            // this may not be a good idea
            if (group.size() == 1) {
                this.getParts().add(group.get(0));
            } else {
                this.getParts().add(group);
            }
        } else {
            this.getParts().add(q);
        }
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator or) {
        this.operator = or;
    }

    public boolean isEmpty() {
        return this.getParts().isEmpty();
    }

    @Override
    public String generateQueryString() {
        StringBuilder buff = new StringBuilder();
        for (QueryPart part : getParts()) {
            String queryString = part.generateQueryString();
            logger.trace("{} -> {} ", part.getClass().getSimpleName(), queryString);
            if (StringUtils.isNotBlank(queryString) && StringUtils.isNotBlank(queryString.trim())) {
                if (buff.length() > 0) {
                    buff.append(' ').append(getOperator().toString()).append(' ');
                }
                buff.append(queryString);
            }
        }
        if (buff.length() == 0) {
            return "";
        }
        buff.insert(0, "( ");
        buff.append(" ) ");
        logger.trace(buff.toString());
        return buff.toString();
    }

    @Override
    public String toString() {
        return generateQueryString();
    }

    private String getDescription(boolean escape) {
        if (!descriptionVisible)
            return "";
        List<String> partDescriptions = new ArrayList<String>();
        for (QueryPart<?> part : getParts()) {
            String description = "";
            if (escape) {
                description = part.getDescriptionHtml();
            } else {
                description = part.getDescription();
            }

            // filter out blank descriptions
            if (StringUtils.isNotBlank(description)) {
                partDescriptions.add(description);
            }
        }

        String description = StringUtils.join(partDescriptions, ";");

        // Describing the role of the operator only necessary when we have multiple terms
        if (partDescriptions.size() > 1) {
            String optype = operator == Operator.OR ? "Any" : "All";
            String fmt = "%s of the following conditions: ( %s ) ";
            description = String.format(fmt, optype, description);
        }

        return description;
    }

    @Override
    public String getDescription() {
        return getDescription(false);
    }

    @Override
    public String getDescriptionHtml() {
        return getDescription(true);
    }

    public int size() {
        return getParts().size();
    }

    public QueryPart get(int i) {
        return getParts().get(i);
    }

    public void setParts(List<QueryPart> parts) {
        this.parts = parts;
    }
}
