package org.tdar.search.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.tdar.core.service.SearchService;
import org.tdar.index.LowercaseWhiteSpaceStandardAnalyzer;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public abstract class QueryBuilder {
    protected final Logger logger = Logger.getLogger(getClass());
    private List<QueryPart> queryParts;
    private Class<?>[] classes;
    private List<DynamicQueryComponent> overrides;
    private List<String> omitContainedLabels = new ArrayList<String>();

    public QueryBuilder() {
        this.queryParts = new ArrayList<QueryPart>();
    }

    public void append(QueryPart q) {
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
        Query query = new MatchAllDocsQuery();
        String qstring = this.toString();
        List<String> fields = new ArrayList<String>();
        Set<DynamicQueryComponent> cmpnts = new HashSet<DynamicQueryComponent>();

        if (!qstring.isEmpty()) {
            PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new LowercaseWhiteSpaceStandardAnalyzer());

            // add all DynamicQueryComponents for specified classes
            for (Class<?> cls : classes) {
                cmpnts.addAll(SearchService.createFields(cls, ""));
            }

            List<DynamicQueryComponent> toRemove = new ArrayList<DynamicQueryComponent>();
            // add all overrides and replace existing settings
            if (getOverrides() != null) {
                for (DynamicQueryComponent over : getOverrides()) {
                    for (DynamicQueryComponent cmp : cmpnts) {
                        if (over.getLabel().equals(cmp.getLabel())) {
                            toRemove.add(cmp);
                        }
                    }
                }
                cmpnts.removeAll(toRemove);
                cmpnts.addAll(getOverrides());
            }
            /*
             * The <b>fields</b> list specifies all of the generic fields that
             * use the default analyzer.
             * 
             * The rest of the fields have analyzers specified and get added to
             * the analyzer with the specific analyzer
             */
            for (DynamicQueryComponent cmp : cmpnts) {
                if (stringContainedInLabel(cmp.getLabel()))
                    continue;
                fields.add(cmp.getLabel());
                if (cmp.getAnalyzer() != null) {
                    try {
                        analyzer.addAnalyzer(cmp.getLabel(), (Analyzer) cmp.getAnalyzer().newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    logger.trace(cmp.getLabel() + " : " + cmp.analyzer.getCanonicalName());
                }
            }

            MultiFieldQueryParser qp = new MultiFieldQueryParser(Version.LUCENE_31, fields.toArray(new String[0]), analyzer);
            query = qp.parse(qstring);
            logger.trace(query.toString());
        }
        return query;
    }

    @Override
    public String toString() {
        StringBuilder queryString = new StringBuilder();
        for (QueryPart part : this.queryParts) {
            if (!StringUtils.isEmpty(part.generateQueryString()) && !part.generateQueryString().equals("()")) {
                if (queryString.length() > 0)
                    queryString.append(" AND ");

                queryString.append('(').append(part.generateQueryString()).append(')');
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
}
