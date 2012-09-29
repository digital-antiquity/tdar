package org.tdar.search.query;

import org.tdar.core.exception.TdarRecoverableRuntimeException;

public class FieldQueryPart extends QueryPart.Base {

    private String fieldName;
    private String fieldValue;
    private Float boost;
    private Float fuzzy;
    private Integer proximity;

    public FieldQueryPart() {
    }

    public FieldQueryPart(String fieldName) {
        this.fieldName = fieldName;
    }

    public FieldQueryPart(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    public String generateQueryString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getInverse()).append(getFieldName()).append(":(").append(getFieldValue());

        if (getFuzzy() != null) {
            sb.append("~").append(getFuzzy());
        }

        if (getProximity() != null) {
            sb.append("~").append(getProximity());
        }

        sb.append(")");

        if (getBoost() != null) {
            sb.append("^").append(getBoost());
        }

        return sb.toString();
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    /**
     * @return the boost
     */
    public Float getBoost() {
        return boost;
    }

    /**
     * @param boost
     *            the boost to set
     *            Lucene provides the relevance level of matching documents based on the terms found. To boost a term use the caret, "^", symbol with a boost
     *            factor (a number) at the end of the term you are searching. The higher the boost factor, the more relevant the term will be.
     * 
     *            Boosting allows you to control the relevance of a document by boosting its term. For example, if you are searching for
     * 
     *            jakarta apache
     *            and you want the term "jakarta" to be more relevant boost it using the ^ symbol along with the boost factor next to the term. You would type:
     * 
     *            jakarta^4 apache
     *            This will make documents with the term jakarta appear more relevant. You can also boost Phrase Terms as in the example:
     * 
     *            "jakarta apache"^4 "Apache Lucene"
     *            By default, the boost factor is 1. Although the boost factor must be positive, it can be less than 1 (e.g. 0.2)
     */
    public FieldQueryPart setBoost(Float boost) {
        this.boost = boost;
        return this;
    }

    /**
     * @return the fuzzy
     */
    public Float getFuzzy() {
        return fuzzy;
    }

    /**
     * @param fuzzy
     *            the fuzzy to set
     * 
     *            Fuzzy Searches
     *            Lucene supports fuzzy searches based on the Levenshtein Distance, or Edit Distance algorithm. To do a fuzzy search use the tilde, "~", symbol
     *            at the end of a Single word Term. For example to search for a term similar in spelling to "roam" use the fuzzy search:
     * 
     *            roam~
     *            This search will find terms like foam and roams.
     * 
     *            Starting with Lucene 1.9 an additional (optional) parameter can specify the required similarity. The value is between 0 and 1, with a value
     *            closer to 1 only terms with a higher similarity will be matched. For example:
     * 
     *            roam~0.8
     *            The default that is used if the parameter is not given is 0.5.
     */
    public FieldQueryPart setFuzzy(Float fuzzy) {
        if (fuzzy > 1) {
            throw new TdarRecoverableRuntimeException("fuzzyness can only be between 0 & 1");
        }
        this.fuzzy = fuzzy;
        return this;
    }

    /**
     * @return the proxyimity
     */
    public Integer getProximity() {
        return proximity;
    }

    /**
     * @param proximity
     *            Proximity Searches
     *            Lucene supports finding words are a within a specific distance away. To do a proximity search use the tilde, "~", symbol at the end of a
     *            Phrase. For example to search for a "apache" and "jakarta" within 10 words of each other in a document use the search:
     * 
     *            "jakarta apache"~10
     */
    public FieldQueryPart setProximity(Integer proximity) {
        this.proximity = proximity;
        return this;
    }

}
