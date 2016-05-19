package org.tdar.search.query.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.QueryPartGroup;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public abstract class QueryBuilder extends QueryPartGroup {

    // private static final String _AUTO = "_auto";
    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Operator operator = Operator.AND;
    private String type;

    private List<String> filters = new ArrayList<>();

    public void appendFilter(List<String> filters) {
        this.getFilters() .addAll(filters);
        
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public void append(SearchParameters param, TextProvider provider) {
        if (param != null) {
            append(param.toQueryPartGroup(provider));
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
        return super.isEmpty();
    }
    
    public void setTypeLimit(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    public abstract String getCoreName();

    @Override
    public String generateQueryString() {
        String q = super.generateQueryString();
        if (StringUtils.isBlank(type)) {
            return q;
        }
        
        if (StringUtils.isBlank(q)) {
            return String.format("%s:%s",QueryFieldNames.TYPE, type);
        }
        // bind to type
        return String.format("%s:%s AND (%s)",QueryFieldNames.TYPE, type, q);
    }
}
