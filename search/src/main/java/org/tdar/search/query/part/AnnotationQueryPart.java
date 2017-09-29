package org.tdar.search.query.part;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.search.converter.ResourceDocumentConverter;
import org.tdar.utils.StringPair;

import com.opensymphony.xwork2.TextProvider;

/**
 * Formulate a Luence/SOLR query based on a ResourceAnnotation StringPair
 * @author abrin
 *
 * @param <C>
 */
public class AnnotationQueryPart<C> extends FieldQueryPart<StringPair> {

    private String descriptionLabel;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @SafeVarargs
    public AnnotationQueryPart(String field, StringPair... values) {
        this(field, "Value", values);
    }

    public AnnotationQueryPart(String field, String label, Operator operator, List<StringPair> values) {
        this(field, label);
        if (CollectionUtils.isNotEmpty(values)) {
            for (StringPair pair : values) {
                if ((pair == null) || !pair.isInitialized() || (!pair.isValid())) {
                    continue;
                }
                add(pair);
            }
            setOperator(operator);
        }
    }

    @SafeVarargs
    public AnnotationQueryPart(String field, String descriptionLabel, StringPair... values) {
        super(field, values);
        this.descriptionLabel = descriptionLabel;
    }
    

    @Override
    protected void appendPhrase(StringBuilder sb, int index) {
        StringPair pair = getFieldValues().get(index);
        String key = PhraseFormatter.ESCAPED.format(pair.getFirst());
        String value = PhraseFormatter.ESCAPED.format(pair.getSecond());
        String phrase = PhraseFormatter.QUOTED.format(ResourceDocumentConverter.formatResourceAnnotation(key, value));
        sb.append(phrase);
    }


    @Override
    public String getDescription(TextProvider provider) {
        String fmt = "%s is %s";
        String op = " " + getOperator().toString().toLowerCase() + " ";
        List<String> valueDescriptions = new ArrayList<String>();
        for (StringPair pair : getFieldValues()) {
            valueDescriptions.add(getDescription(provider, pair));
        }
        return String.format(fmt, descriptionLabel, StringUtils.join(valueDescriptions, op));
    }

    private String getDescription(TextProvider provider, StringPair singleValue) {

        String fmt = provider.getText("annotationQueryPart.fmt_description_annotation");
        return MessageFormat.format(fmt, singleValue.getKey(), singleValue.getValue());
    }

}
