package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.TextProvider;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
public class QueryPartGroup implements QueryPart, QueryGroup {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private List<QueryPart<?>> parts = new ArrayList<QueryPart<?>>();
    private Operator operator = Operator.AND;
    private boolean descriptionVisible = true;

    @Override
    public boolean isDescriptionVisible() {
        return descriptionVisible;
    }

    @Override
    public void setDescriptionVisible(boolean descriptionVisible) {
        this.descriptionVisible = descriptionVisible;
    }

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

    @Override
    public List<QueryPart<?>> getParts() {
        return parts;
    }

    public void append(List<? extends QueryPart<?>> parts) {
        for (QueryPart<?> part : parts) {
            append(part);
        }
    }

    @Override
    public void append(QueryPart<?> q) {
        if ((q == null) || q.isEmpty()) {
            return;
        }
        if (q instanceof QueryPartGroup) {
            QueryPartGroup group = (QueryPartGroup) q;
            // this may not be a good idea
            if (group.size() == 1) {
                getParts().add(group.get(0));
            } else {
                getParts().add(group);
            }
        } else {
            getParts().add(q);
        }
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public void setOperator(Operator or) {
        this.operator = or;
    }

    @Override
    public boolean isEmpty() {
        return getParts().isEmpty();
    }

    @Override
    public String generateQueryString() {
        StringBuilder builder = generateContents();
        if (builder.length() == 0) {
            return "";
        }
        builder.insert(0, "( ");
        builder.append(" ) ");
        logger.trace("{}", builder);
        return builder.toString();
    }

    protected StringBuilder generateContents() {
        StringBuilder builder = new StringBuilder();
        for (QueryPart<?> part : getParts()) {
            String queryString = part.generateQueryString();
            logger.trace("{} -> {} ", part.getClass().getSimpleName(), queryString);
            if (StringUtils.isNotBlank(queryString) && StringUtils.isNotBlank(queryString.trim())) {
                if (builder.length() > 0) {
                    builder.append(' ').append(getOperator().toString()).append(' ');
                }
                builder.append(queryString);
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return generateQueryString();
    }

    private String getDescription(TextProvider provider, boolean escape) {
        if (!descriptionVisible) {
            return "";
        }
        List<String> partDescriptions = new ArrayList<String>();
        for (QueryPart<?> part : getParts()) {
            String description = "";
            if (escape) {
                description = part.getDescriptionHtml(provider);
            } else {
                description = part.getDescription(provider);
            }

            // filter out blank descriptions
            if (StringUtils.isNotBlank(description)) {
                partDescriptions.add(description);
            }
        }

        String delim = " " + getDescriptionOperator() + " ";

        String description = StringUtils.join(partDescriptions, delim);

        // Describing the role of the operator only necessary when we have multiple terms
        if (partDescriptions.size() > 1) {
            // String optype = operator == Operator.OR ? "Any" : "All";
            // String fmt = "%s of the following conditions: ( %s ) ";
            description = String.format(" (%s)", description);
        }

        return description;
    }

    @Override
    public String getDescription(TextProvider provider) {
        return getDescription(provider, false);
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return getDescription(provider, true);
    }

    public int size() {
        return getParts().size();
    }

    public QueryPart get(int i) {
        return getParts().get(i);
    }

    public void setParts(List<QueryPart<?>> parts) {
        this.parts = parts;
    }

    public String getDescriptionOperator() {
        String delim = " and ";
        if (getOperator() == Operator.OR) {
            delim = " or ";
        }
        return delim;
    }
}
