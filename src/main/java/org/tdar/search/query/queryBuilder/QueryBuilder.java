package org.tdar.search.query.queryBuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.tdar.search.query.QueryGroup;
import org.tdar.search.query.QueryPart;
import org.tdar.search.query.QueryPartGroup;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public abstract class QueryBuilder implements QueryGroup {
    protected final Logger logger = Logger.getLogger(getClass());
    private List<QueryPart> queryParts;
    private Class<?>[] classes;
    private List<DynamicQueryComponent> overrides;
    private List<String> omitContainedLabels = new ArrayList<String>();
    private Operator operator = Operator.AND;
    private QueryParser queryParser;
    private Query query;
    private String rawQuery;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(queryParts);
    }

    public QueryBuilder() {
        this.queryParts = new ArrayList<QueryPart>();
    }

    public void append(List<QueryPart> parts) {
        this.queryParts.addAll(parts);
    }

    public void append(QueryPart q) {
        if (q instanceof QueryPartGroup) {
            if (((QueryPartGroup) q).isEmpty()) {
                return;
            }
        }
        if (!StringUtils.isEmpty(q.toString()) && !q.toString().equals("()")) {
            this.queryParts.add(q);
        }
    }

    public List<DynamicQueryComponent> getOverrides() {
        return this.overrides;
    }

    public void setOverrides(List<DynamicQueryComponent> over) {
        this.overrides = over;
    }

    /*
     * The buildQuery method is designed to extract all of the fields from the
     * classes specified by looking at the annotations on the classes and
     * extracting the @Field annotations out.
     */
    public Query buildQuery() throws ParseException {
        String qstring = this.toString();
        setQuery(new MatchAllDocsQuery());
        if (!qstring.isEmpty()) {
            setQuery(getQueryParser().parse(qstring));
            logger.trace(getQuery().toString());
        }
        return getQuery();
    }

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(rawQuery)) {
            return rawQuery;
        }
        StringBuilder queryString = new StringBuilder();
        for (QueryPart part : this.queryParts) {
            String queryStringPart = part.generateQueryString();
            if (!StringUtils.isEmpty(queryStringPart) && !queryStringPart.equals("()")) {
                if (queryString.length() > 0) {
                    queryString.append(" ").append(getOperator()).append(" ");
                }
                queryString.append('(').append(queryStringPart).append(')');
            }
        }
        return queryString.toString();
    }

    /*
     * Use setClasses to specify exactly which classes should be inspected to
     * identify fields. Java Reflection doesn't give any good ways to get at
     * subclasses, so it's important to specify ALL subclasses that you want to
     * inspect. Eg: InformationResource.class, Document.class will not find all
     * fields specify to Ontology.class
     */
    void setClasses(Class<?>[] classes) {
        this.classes = classes;
    }

    public Class<?>[] getClasses() {
        return classes;
    }

    /**
     * @param omitContainedLabels
     *            the omitContainedLabels to set
     */
    public void setOmitContainedLabels(List<String> omitContainedLabels) {
        this.omitContainedLabels = omitContainedLabels;
    }

    /**
     * @return the omitContainedLabels
     */
    public List<String> getOmitContainedLabels() {
        return omitContainedLabels;
    }

    public boolean stringContainedInLabel(String label) {
        for (String omitItem : omitContainedLabels) {
            if (StringUtils.containsIgnoreCase(label, omitItem)) {
                return true;
            }
        }
        return false;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator or) {
        this.operator = or;
    }

    public QueryParser getQueryParser() {
        return queryParser;
    }

    public void setQueryParser(QueryParser queryParser2) {
        this.queryParser = queryParser2;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public String getRawQuery() {
        return rawQuery;
    }

}
