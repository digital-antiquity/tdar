package org.tdar.search.query.part;

import java.util.Collection;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.search.index.TdarIndexNumberFormatter;
import org.tdar.search.query.QueryFieldNames;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class TemporalQueryPart extends FieldQueryPart<CoverageDate> {

    // FIXME: there's a possibility that lucene is not going to do what we think it's going to do when
    // binding to multiple values see TDAR-1163
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TEMPORAL_QUERY_FORMAT = QueryFieldNames.ACTIVE_START_DATE + ":[00000000000 TO %2$s] AND " + QueryFieldNames.ACTIVE_END_DATE
            + ":[%1$s TO 19999999999] AND " + QueryFieldNames.ACTIVE_COVERAGE_TYPE + ":%3$s ";

    public TemporalQueryPart() {
    }

    public TemporalQueryPart(CoverageDate... coverageDates) {
        add(coverageDates);
    }

    public TemporalQueryPart(Collection<CoverageDate> coverageDates, Operator operator) {
        this(coverageDates.toArray(new CoverageDate[0]));
        setOperator(operator);
    }

    @Override
    protected String formatValueAsStringForQuery(int index) {
        CoverageDate date = getFieldValues().get(index);
        if (date == null || !date.isValidForController()) {
            return "";
        }
        return String.format(
                TEMPORAL_QUERY_FORMAT,
                TdarIndexNumberFormatter.format(date.getStartDate()), TdarIndexNumberFormatter.format(date.getEndDate()), date.getDateType().name());
    };

    @Override
    public String getDescription(TextProvider provider) {
        return provider.getText("temporalQueryPart.date_between", getFieldValues().get(0).toString());
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    @Override
    public void add(CoverageDate... coverageDates) {
        for (CoverageDate date : coverageDates) {
            logger.info("adding {}", date);
            if (date != null && date.getDateType() != null && date.getDateType() != CoverageType.NONE && date.isInitialized()) {
                super.add(date);
            }
        }
    }
}
