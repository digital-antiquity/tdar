package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.tdar.search.index.TdarIndexNumberFormatter;
import org.tdar.struts.data.Range;

public class RangeQueryPart<C> extends FieldQueryPart<Range<C>> {

    private static final String FMT_DESCRIPTION_VALUE_BETWEEN = "between %s and %s";
    private static final String FMT_DESCRIPTION_VALUE_GREATER = "greater than %1$s";
    private static final String FMT_DESCRIPTION_VALUE_LESS = "less than %2$s";

    private String descriptionLabel;
    private boolean inclusive = true;

    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");

    public RangeQueryPart(String field, Range<C>... values) {
        this(field, "Value", values);
    }

    @SuppressWarnings("unchecked")
    public RangeQueryPart(String field, Operator operator, List<Range<C>> values) {
        this(field, "Value");
        for (Range<C> range : values) {
            if (!range.isInitialized() || range.getStart() == null && range.getEnd() == null) {
                continue;
            }
            add(range);
        }
        setOperator(operator);
    }

    public RangeQueryPart(String field, String descriptionLabel, Range<C>... values) {
        super(field, values);
        this.descriptionLabel = descriptionLabel;
    }

    @Override
    protected void appendPhrase(StringBuilder sb, int index) {
        Range<C> value = getFieldValues().get(index);
        String start = convert(value.getStart());
        String end = convert(value.getEnd());
        if (StringUtils.isBlank(start))
            start = "*";
        if (StringUtils.isBlank(end))
            end = "*";

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
        if (date == null)
            return null;
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(dtf);
    }

    private static String convert(Object object) {
        if (object == null)
            return null;
        if (object instanceof Date) {
            return convert((Date) object);
        }
        if (object instanceof Number) {
            return convert((Number) object);
        }

        String objString = object.toString();
        if (StringUtils.isNumeric(objString) && StringUtils.isNotBlank(objString)) {
            return TdarIndexNumberFormatter.format(NumberUtils.createNumber(objString));
        }
        return objString;
    }

    private static String convert(Number number) {
        if (number == null)
            return null;
        return TdarIndexNumberFormatter.format(number);
    }

    @Override
    public String getDescription() {
        String fmt = "%s is %s";
        String op = " " + getOperator().toString().toLowerCase() + " ";
        List<String> valueDescriptions = new ArrayList<String>();
        for (Range<C> range : getFieldValues()) {
            valueDescriptions.add(getDescription(range));
        }
        return String.format(fmt, descriptionLabel, StringUtils.join(valueDescriptions, op));
    }

    private String getDescription(Range<C> singleValue) {

        String fmt = FMT_DESCRIPTION_VALUE_BETWEEN;
        C start = singleValue.getStart();
        C end = singleValue.getEnd();
        if (isBlank(start) || isBlank(end)) {
            if (isBlank(start)) {
                fmt = FMT_DESCRIPTION_VALUE_LESS;
            } else {
                fmt = FMT_DESCRIPTION_VALUE_GREATER;
            }
        }
        return String.format(fmt, start, end);
    }

    private boolean isBlank(C item) {
        if (item == null)
            return true;
        return StringUtils.isBlank(item.toString());
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

}
