/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.GenericService;
import org.tdar.utils.Pair;

/**
 * @author Adam Brin
 * 
 */
public class QueryDescriptionBuilder {

    public static final String TITLE_FILTERED_BY_KEYWORD = "Filtered by Keyword";
    public static final String LIMITED_TO = "Limited to:";
    public static final String TITLE_ALL_RECORDS = "All Records";
    public static final String TITLE_BY_TDAR_ID = "Search by TDAR ID";
    public static final String WITH_SITE_NAME_KEYWORDS = " with Site name Keywords";
    public static final String WITH_SITE_TYPE_KEYWORDS = " with Site type Keywords";
    public static final String WITH_MATERIAL_KEYWORDS = " with Material Keywords";
    public static final String WITH_OTHER_KEYWORDS = " with Other Keywords";
    public static final String WITH_TEMPORAL_KEYWORDS = " with Temporal Keywords";
    public static final String WITH_GEOGRAPHIC_KEYWORDS = " with Geographic Keywords";
    public static final String WITH_CULTURE_KEYWORDS = " with Culture Keywords";
    public static final String WITH_INVESTIGATION_TYPES = " with Investigation Types";
    public static final String WITH_UNCONTROLLED_SITE_TYPE_KEYWORDS = " with Site Type Keywords";
    public static final String WITH_UNCONTROLLED_CULTURE_KEYWORDS = " with Culture Keywords";
    public static final String WITH_SUBMITTER = " submitted by";
    public static final String WITH_SUBMITTER_LAST_NAME = " with Submitter Last Name";
    public static final String WITH_SUBMITTER_FIRST_NAME = " with Submitter First Name";
    public static final String WITH_SUBMITTER_EMAIL = " with Submitter Email";
    public static final String WITH_SUBMITTER_INSTITUTION = " with Submitter Insitution";
    public static final String WITH_AUTHOR = " with Author/Contributor";
    public static final String WITH_AUTHOR_LAST_NAME = " with Author Last Name";
    public static final String WITH_AUTHOR_FIRST_NAME = " with Author First Name";
    public static final String BETWEEN = " Between";
    public static final String WITH_TEXT_IN_TITLE = " with Text in Title";
    public static final String USING_KEYWORD = " Using Keyword";
    public static final String COMMA_SEPARATOR = ", ";
    public static final String AND = " and ";
    public static final String BC = " BC ";
    public static final String AD = " AD ";
    public static final String WITH_RADIOCARBON_DATE_BETWEEN = " with Radiocarbon Date Between";
    private static final String WITH_DATE_BETWEEN = "with Date Between";
    
    public static final String WITH_REGISTRATION_DATE_AFTER = "with resource creation date after";
    public static final String WITH_REGISTRATION_DATE_BEFORE = "with resource creation date before";
    public static final String WITH_UPDATE_DATE_AFTER = "with update date after";
    public static final String WITH_UPDATE_DATE_BEFORE = "with update date before";
    
    public static final String WITHIN_MAP_CONSTRAINTS = " Within Map Constraints";
    public static final String SEARCHING_FOR_RESOURCE_WITH_T_DAR_ID = "Searching for Resource with tDAR ID";
    public static final String SELECTED_RESOURCE_TYPES = "Selected Resource Types";
    public static final String SELECTED_DOCUMENT_TYPES = "Selected Document Type";
    public static final String SEARCHING_ALL_RESOURCE_TYPES = " Searching all Resource Types";
    public static final String WITH_STATUSES = "with Statuses";
    public static final String FILE_ACCESS = "with Files Marked as";
    public static final String PROJECT = "with Project Id";

    private List<Pair<String, Object>> query = new ArrayList<Pair<String, Object>>();
    public static final String TITLE_TAG_KEYWORD_PHRASE = "Referred Query from the Transatlantic Archaeology Gateway";
    public static final String DATE_CREATED_RANGE_BEFORE = "Created Before";
    public static final String DATE_CREATED_RANGE_BETWEEN = "Created Between";
    public static final String DATE_CREATED_RANGE_AFTER = "Created After";
    public static final String RAW_QUERY = "Advanced Search";

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    
    public void append(String label, Object values) {
        Pair<String, Object> val = new Pair<String, Object>(label, values);
        query.add(val);
    }
   
    //FIXME: format to something friendlier
    public void append(String label, Date date) {
        append(label, date.toString());
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toHtml() {
        return toString(true);
    }

    private String toString(boolean html) {
        Iterator<Pair<String, Object>> iterator = query.iterator();
        StringBuilder builder = new StringBuilder();
        boolean blank = true;
        while (iterator.hasNext()) {
            Pair<String, Object> pair = iterator.next();
            String values = extractValues(pair.getSecond());
            if (StringUtils.isBlank(values))
                continue;

            blank = false;
            if (!blank && StringUtils.isNotBlank(builder.toString())) {
                builder.append(" AND ");
            }
            createLabel(html, builder, pair.getFirst());
            if (html) {
                values = HtmlUtils.htmlEscape(values);
            }
            builder.append(values);

        }
//        if (blank) {
//            createLabel(html, builder, TITLE_ALL_RECORDS);
//        }
        return StringUtils.replace(builder.toString(), "  ", " ");
    }

    /**
     * @param values_
     * @return
     */
    private String extractValues(Object values_) {
        List<String> values = new ArrayList<String>();
        if (values_ instanceof Collection) {
            Collection<?> valueCollection = (Collection<?>) values_;
            for (Object val_ : valueCollection) {
                String val = GenericService.extractStringValue(val_);
                if (StringUtils.isNotEmpty(val)) {
                    values.add(val);
                }
            }
        } else {
            String val = GenericService.extractStringValue(values_);
            if (StringUtils.isNotEmpty(val)) {
                values.add(val);
            }
        }
        if (values.size() == 0) {
            return "";
        }
        return StringUtils.join(values, ", ");
    }

    private void createLabel(boolean html,StringBuilder builder, String label) {
        if (html) {
            builder.append(" <b> ");
        }
        builder.append(label.trim()).append(": ");
        if (html) {
            builder.append(" </b> ");
        }
    }

    /**
     * @param label
     * @param minx
     * @param maxx
     * @param miny
     * @param maxy
     */
    public void appendSpatialQuery(String label, Double minx, Double maxx, Double miny, Double maxy) {
        StringBuilder sb = new StringBuilder();
        append(label, sb.append(minx).append(",").append(miny).append(" x ").append(maxx).append(",").append(maxy));
    }

    /**
     * @param yearType
     * @param fromYear
     * @param toYear
     */
    //FIXME: remove this
    public void appendTemporalRange(CoverageType yearType, Integer fromYear, Integer toYear) {
        String label = "";
        StringBuilder value = new StringBuilder();
        switch (yearType) {
            case CALENDAR_DATE:
                label = WITH_DATE_BETWEEN;
                if (fromYear > 0) {
                    value.append(fromYear).append(AD);
                } else {
                    value.append(Math.abs(fromYear)).append(BC);
                }
                value.append(" and ");
                if (toYear > 0) {
                    value.append(toYear).append(AD);
                } else {
                    value.append(Math.abs(fromYear)).append(BC);
                }
                break;
            case RADIOCARBON_DATE:
                label = WITH_RADIOCARBON_DATE_BETWEEN;
                value.append(fromYear).append(" and ").append(toYear);
                break;
        }
        append(label, value);

    }
    
    public void appendCoverageDate(CoverageDate cd) {
        appendTemporalRange(cd.getDateType(), cd.getStartDate(), cd.getEndDate());
    }

    /**
     * @param label
     * @param startValue
     * @param endValue
     */
    public void appendRange(String label, String startValue, String endValue) {
        if (StringUtils.isEmpty(startValue)) {
            append(label, endValue);
        } else if (StringUtils.isEmpty(endValue)) {
            append(label, startValue);
        } else {
            append(label, startValue + " and " + endValue);
        }
    }

    public void appendAuthor(Person person) {
        if (person.getId() != -1L) {
            append(WITH_AUTHOR, person.toString());
        }
        else {
            // we don't want the person tostring for a record that is mostly blank
            append(WITH_AUTHOR_LAST_NAME, person.getLastName());
            append(WITH_AUTHOR_FIRST_NAME, person.getFirstName());
        }
    }

    public void appendSubmitter(Person person) {
        if (person.getId() != -1L) {
            append(WITH_AUTHOR, person);
        }
        else {
            // we don't want the person tostring for a record that is mostly blank
            append(WITH_SUBMITTER_LAST_NAME, person.getLastName());
            append(WITH_SUBMITTER_FIRST_NAME, person.getFirstName());
            append(WITH_SUBMITTER_EMAIL, person.getEmail());
            append(WITH_SUBMITTER_INSTITUTION, person.getInstitutionName());
        }
    }
    
}
