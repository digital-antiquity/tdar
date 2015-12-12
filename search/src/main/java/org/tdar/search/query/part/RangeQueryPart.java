package org.tdar.search.query.part;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.range.Range;

import com.opensymphony.xwork2.TextProvider;

public class RangeQueryPart<C> extends FieldQueryPart<Range<C>> {

    private String descriptionLabel;
    private boolean inclusive = true;
    private final Logger logger = LoggerFactory.getLogger(getClass());

//    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");

    @SafeVarargs
    public RangeQueryPart(String field, Range<C>... values) {
        this(field, "Value", values);
    }

    @SuppressWarnings("unchecked")
    public RangeQueryPart(String field, String label, Operator operator, List<Range<C>> values) {
        this(field, label);
        if (CollectionUtils.isNotEmpty(values)) {
            for (Range<C> range : values) {
                if ((range == null) || !range.isInitialized() || ((range.getStart() == null) && (range.getEnd() == null))) {
                    continue;
                }
                add(range);
            }
            setOperator(operator);
        }
    }

    public RangeQueryPart(String field, String descriptionLabel, @SuppressWarnings("unchecked") Range<C>... values) {
        super(field, values);
        this.descriptionLabel = descriptionLabel;
    }

    @Override
    protected void appendPhrase(StringBuilder sb, int index) {
        Range<C> value = getFieldValues().get(index);
        String start = convert(value.getStart());
        String end = convert(value.getEnd());
        if (StringUtils.isBlank(start)) {
            start = "*";
        }
        if (StringUtils.isBlank(end)) {
            end = "*";
        }

        String phrase = String.format("%s TO %s", start, end);
        if (inclusive) {
            phrase = String.format("[%s]", phrase);
        } else {
            phrase = String.format("{%s}", phrase);
        }
        logger.trace(phrase);
        sb.append(phrase);
    }

    private static String convert(Date date) {
        if (date == null) {
            return null;
        }
        DateTime dateTime = new DateTime(date);
        // we convert dates to utc when indexing them in lucene, therefore when performing a search we need to similarly convert the
        // dates in a date range.
        dateTime = dateTime.toDateTime(DateTimeZone.UTC);
        return dateTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    private static String convert(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Date) {
            return convert((Date) object);
        }
        if (object instanceof Number) {
            return convert((Number) object);
        }

        String objString = object.toString();
//        if (StringUtils.isNumeric(objString) && StringUtils.isNotBlank(objString)) {
//            return objString;
//        }
        return objString;
    }

    private static String convert(Number number) {
        if (number == null) {
            return null;
        }
        return number.toString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        String fmt = "%s is %s";
        String op = " " + getOperator().toString().toLowerCase() + " ";
        List<String> valueDescriptions = new ArrayList<String>();
        for (Range<C> range : getFieldValues()) {
            valueDescriptions.add(getDescription(provider, range));
        }
        return String.format(fmt, descriptionLabel, StringUtils.join(valueDescriptions, op));
    }

    private String getDescription(TextProvider provider, Range<C> singleValue) {

        String fmt = provider.getText("rangeQueryPart.fmt_description_value_between");
        C start = singleValue.getStart();
        C end = singleValue.getEnd();
        if (isBlank(start) || isBlank(end)) {
            if (isBlank(start)) {
                fmt = provider.getText("rangeQueryPart.fmt_description_value_less");
            } else {
                fmt = provider.getText("rangeQueryPart.fmt_description_value_greater");
            }
        }
        return MessageFormat.format(fmt, start, end);
    }

    private boolean isBlank(C item) {
        if (item == null) {
            return true;
        }
        return StringUtils.isBlank(item.toString());
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

}
