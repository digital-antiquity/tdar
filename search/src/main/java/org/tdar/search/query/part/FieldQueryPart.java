package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;

import com.opensymphony.xwork2.TextProvider;

/**
 * @author abrin
 * 
 *         This is the basic class for managing Lucene/Solr Queries. It handles a number of issues related to queries from building them out to formatting the
 *         "text" description of the query part. The main entry into this class is "generateQueryString()"
 * 
 * @param <C>
 */
public class FieldQueryPart<C> implements QueryPart<C> {

    private static final String NOT = " NOT ";

    // the "lucene" name for this field
    private String fieldName;
    // the display name for the field (shown to users)
    private String displayName;
    // the list of values to search for
    private List<C> fieldValues = new ArrayList<C>();
    // boost the relevancy by
    private Float boost;
    // how "fuzzy" should the search be?
    private Float fuzzy;
    // how many words away can the terms be to match
    private Integer proximity;
    // How should the term be formatted (escaped)
    private List<PhraseFormatter> phraseFormatters;
    // operator
    private Operator operator = Operator.AND;
    private boolean inverse;
    // a few fields may be "hidden" from the description
    private boolean descriptionVisible = true;
    // if the field value implements "Validatable" should we allow invalid values (e.g. a Person with just a first name)
    private boolean allowInvalid = false;

    public FieldQueryPart() {
    }

    public FieldQueryPart(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setLimit(C obj) {
        getFieldValues().add(obj);
    }

    public FieldQueryPart(String fieldName, String displayName, Collection<C> incomingValues) {
        this.fieldName = fieldName;
        setFieldValues(incomingValues);
        setDisplayName(displayName);
    }

    public FieldQueryPart(String fieldName, String displayName, Operator oper, Collection<C> incomingValues) {
        this(fieldName, displayName, incomingValues);
        this.operator = oper;
    }

    @SafeVarargs
    public FieldQueryPart(String fieldName, String displayName, Operator oper, C... incomingValues) {
        this(fieldName, displayName, Arrays.asList(incomingValues));
        this.operator = oper;
    }

    @SafeVarargs
    public FieldQueryPart(String fieldName, C... incomingValues) {
        this(fieldName, "", Arrays.asList(incomingValues));
    }

    public FieldQueryPart(String fieldName, Collection<C> incomingValues) {
        this(fieldName, "", incomingValues);
    }

    public FieldQueryPart(String fieldName, Operator oper, Collection<C> incomingValues) {
        this(fieldName, "", oper, incomingValues);
    }

    @SafeVarargs
    public FieldQueryPart(String fieldName, Operator oper, C... incomingValues) {
        this(fieldName, "", oper, incomingValues);
    }

    public FieldQueryPart<C> setPhraseFormatters(PhraseFormatter... phraseFormatters) {
        this.phraseFormatters = Arrays.asList(phraseFormatters);
        return this;
    }

    @Override
    public boolean isEmpty() {
        if (CollectionUtils.isEmpty(fieldValues)) {
            return true;
        }
        for (C value : fieldValues) {
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return generateQueryString();
    }

    @Override
    /**
     * For each field value, convert it to a phrase and append it, then construct the entire query phrase from the values
     */
    public String generateQueryString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldValues.size(); i++) {
            appendPhrase(sb, i);
        }
        if (sb.length() == 0) {
            return "";
        }
        constructQueryPhrase(sb, getFieldName());
        return sb.toString();
    }

    /**
     * Given a list of values (or a single one), prepend the text with the field name. Then append boost if needed
     * 
     * @param sb
     * @param fieldName
     */
    protected void constructQueryPhrase(StringBuilder sb, String fieldName) {
        StringBuilder startPhrase = new StringBuilder();
        // support for "all fields query"
        if (StringUtils.isNotBlank(fieldName)) {
            startPhrase.append(fieldName).append(":");
        }
        startPhrase.append('(');
        sb.insert(0, startPhrase);
        sb.append(')');

        if (getBoost() != null) {
            sb.append('^').append(getBoost());
        }
    }

    /**
     * Format the specified value as a string, apply the phrase formatters, and then append it to the query.
     * 
     * E.g. take a Institution, extract the name, format it and escape it ("Arizona\ State\ University"
     * 
     * Then append proximity and fuzzyness modifiers
     * 
     * @param sb
     * @param index
     */
    protected void appendPhrase(StringBuilder sb, int index) {
        String value = "";
        value = formatValueAsStringForQuery(index);

        if (StringUtils.isBlank(value)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(phraseFormatters)) {
            for (PhraseFormatter formatter : phraseFormatters) {
                value = formatter.format(value);
            }
        }

        if (sb.length() > 0) {
            sb.append(" ");
            if (operator != Operator.OR) {
                sb.append(operator).append(" ");
            }
        }
        sb.append(value);

        if (getFuzzy() != null) {
            sb.append('~').append(getFuzzy());
        }

        if (getProximity() != null) {
            sb.append('~').append(getProximity());
        }
    }

    /**
     * Either call "toString()" or get the name of the enum
     * 
     * @param index
     * @return
     */
    protected String formatValueAsStringForQuery(int index) {
        C item = fieldValues.get(index);
        if (item == null) {
            return "";
        }
        if (item instanceof Enum) {
            return ((Enum<?>) item).name();
        } else {
            return item.toString();
        }
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
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
    public FieldQueryPart<C> setBoost(Float boost) {
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
    public FieldQueryPart<C> setFuzzy(Float fuzzy) {
        if (fuzzy > 1) {
            throw new TdarRecoverableRuntimeException("fieldQueryPart.fuzzyness_out_of_range");
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
    public FieldQueryPart<C> setProximity(Integer proximity) {
        this.proximity = proximity;
        return this;
    }

    /**
     * Get a "Plain Text" version of the query
     */
    @Override
    public String getDescription(TextProvider provider) {
        if (!descriptionVisible) {
            return "";
        }
        List<Object> vals = new ArrayList<Object>();
        for (int i = 0; i < getFieldValues().size(); i++) {
            Object fieldValue = getFieldValues().get(i);
            StringBuilder builder = new StringBuilder();
            if (fieldValue == null) {
                continue;
            }
            if (Resource.class.isAssignableFrom(fieldValue.getClass())) {
                fieldValue = ((Resource) fieldValue).getTitle();
            } else if (ResourceCollection.class.isAssignableFrom(fieldValue.getClass())) {
                fieldValue = ((ResourceCollection) fieldValue).getTitle();
            } else if (fieldValue instanceof Localizable) {
                fieldValue = provider.getText(((Localizable) fieldValue).getLocaleKey());
            } else if (fieldValue instanceof HasLabel) {
                fieldValue = ((HasLabel) fieldValue).getLabel();
            }
            builder.append(' ').append(fieldValue).append(' ');
            vals.add(builder.toString());
        }
        return String.format("%s: \"%s\" ", getDisplayName(), StringUtils.join(vals, getDescriptionOperator(provider)));
    }

    /**
     * get a HTML acceptable version of the query
     */
    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    // /**
    // * @param inverse
    // * the inverse to set
    // */
    // public void setInverse(boolean inverse) {
    // this.inverse = inverse;
    // }

    // protected String getInverse() {
    // if (isInverse()) {
    // return NOT;
    // }
    // return "";
    // }
    //
    // /**
    // * @return the inverse
    // */
    // public boolean isInverse() {
    // return inverse;
    // }

    public List<PhraseFormatter> getPhraseFormatters() {
        return phraseFormatters;
    }

    public void setPhraseFormatters(List<PhraseFormatter> phraseFormatters) {
        this.phraseFormatters = phraseFormatters;
    }

    public List<C> getFieldValues() {
        return fieldValues;
    }

    @SuppressWarnings("unchecked")
    public void setFieldValues(Collection<C> fieldValues) {
        this.fieldValues.clear();
        for (C item : fieldValues) {
            add(item);
        }
    }

    @SuppressWarnings("unchecked")
    public void add(C... values) {
        for (C value : values) {
            if (validate(value)) {
                fieldValues.add(value);
            }
        }
    }

    // should a fieldValue be ignored when adding it to the value list? breaking out into separate method so that subclasses can make the call
    protected boolean validate(C value) {
        if (value == null) {
            return false;
        }
        if ((value instanceof Validatable) && !isAllowInvalid() && !((Validatable) value).isValidForController()) {
            throw new TdarValidationException("fieldQueryPart.is_not_valid", Arrays.asList(value.toString()));
        }
        return true;
    }

    public String getDisplayName() {
        if (StringUtils.isBlank(displayName)) {
            return fieldName;
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean isDescriptionVisible() {
        return descriptionVisible;
    }

    @Override
    public void setDescriptionVisible(boolean descriptionVisible) {
        this.descriptionVisible = descriptionVisible;
    }

    public boolean isAllowInvalid() {
        return allowInvalid;
    }

    public void setAllowInvalid(boolean allowInvalid) {
        this.allowInvalid = allowInvalid;
    }

    public void update() {
    }

    public String getDescriptionOperator(TextProvider provider) {
        StringBuilder builder = new StringBuilder(" ");
        if (getOperator() == Operator.OR) {
            builder.append(provider.getText("fieldQueryPart.or"));
        } else {
            builder.append(provider.getText("fieldQueryPart.and"));
        }
        builder.append(' ');
        return builder.toString();
    }

    public String getCleanedQueryString(String value) {
        String cleanedQueryString = value.trim();
        // if we have a leading and trailng quote, strip them
        if (cleanedQueryString.startsWith("\"") && cleanedQueryString.endsWith("\"")) {
            cleanedQueryString = cleanedQueryString.substring(1, cleanedQueryString.length() - 1);
        }
        return PhraseFormatter.ESCAPE_QUOTED.format(cleanedQueryString);
    }

}
