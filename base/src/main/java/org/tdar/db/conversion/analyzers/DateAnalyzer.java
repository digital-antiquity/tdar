package org.tdar.db.conversion.analyzers;

import java.util.Date;
import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.datatable.TDataTableColumn;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * Tries to find out of the a string value passed to it contains a date or not.
 * 
 * @author Martin Paulo
 */
public class DateAnalyzer implements ColumnAnalyzer {

    /**
     * <p>
     * This method is surfaced as it is important that the same date is found by both the analyzer and the method that converts the value into a timestamp in
     * preparation for the database to convert the column into a time column. Not only does that avoid problems with different interpretations, but it also
     * allows the current implementation to be swapped out or refined in this one location if it isn't good enough.
     * <p>
     * As a static method it offends my sensibilities, but for the time being it allows us to explore the options and trade offs.
     * <p>
     * Uses <a href="http://natty.joestelmach.com/doc.jsp">natty</a>.
     * <p>
     * The original code in the PostgressDatabase.java file that did the conversion read:
     * 
     * <pre>
     * DateFormat dateFormat = new SimpleDateFormat();
     * DateFormat accessDateFormat = new SimpleDateFormat(&quot;EEE MMM dd hh:mm:ss z yyyy&quot;);
     * Date date = null;
     * try {
     *     java.sql.Date.valueOf(colValue);
     *     date = dateFormat.parse(colValue);
     * } catch (Exception e) {
     *     logger.trace(&quot;couldn't parse &quot; + colValue, e);
     * }
     * try {
     *     date = accessDateFormat.parse(colValue);
     * } catch (Exception e) {
     *     logger.trace(&quot;couldn't parse &quot; + colValue, e);
     * }
     * </pre>
     * 
     * @param value
     *            A string that is to be inspected to see if it contains a date
     * @return Either null if no date was found in the String, or the date expressed as a java.util.Date
     */
    public static Date convertValue(final String value) {
        Logger logger = LoggerFactory.getLogger(DateAnalyzer.class);

        Date result = null;
        logger.trace("---> " + value);
        List<DateGroup> candidateDates = new Parser().parse(value);
        if (isOnlyOneDateFound(candidateDates)) {
            logger.trace("only one found");
            DateGroup candidate = candidateDates.get(0);
            Tree syntaxTree = candidate.getSyntaxTree();
            // At the top of the syntax tree we want a single date_time alternative (more than one means alternate dates were be found)
            if (syntaxTree.getChildCount() == 1) {
                logger.trace("only one child");
                Tree datetime = syntaxTree.getChild(0);
                // The date_time instance will have a date plus a time, or a date, or a time
                // For the possible tree see: http://natty.joestelmach.com/doc.jsp
                Tree firstChild = datetime.getChild(0);
                // we are only interested in the date component if it is an explicit date.
                if ("EXPLICIT_DATE".equals(firstChild.toString())) {
                    // could further demand a day, a month and a year
                    result = candidate.getDates().get(0);
                }
            }
            // Dealing with case: 'personal communication, email 2/23/08' gets parsed, want to make sure we're not cherry-picking a date from a larger piece of
            // text
            logger.trace("{}<==>{}", candidate.getText(), value);
            if (!StringUtils.equals(candidate.getText(), value)) {
                result = null;
            }

            // partial date's not helpful because we end up doing an implicit cast in postgres

            // removing partial date 2/2
            if (value.matches("(\\d+)(\\-|/)(\\d+)")) {
                return null;
            }

            // removing partial date August 93
            if (value.matches("^(\\w+)\\s(\\d+)$")) {
                return null;
            }

            logger.trace("== result: {} ", result);
        }
        return result;
    }

    /**
     * Make sure the parser only finds a single date within the string
     * 
     * @param candidateDates
     * @return
     */
    private static boolean isOnlyOneDateFound(List<DateGroup> candidateDates) {
        return (candidateDates.size() == 1)
                && (candidateDates.get(0).getDates().size() == 1)
                && !candidateDates.get(0).isRecurring();
    }

    /**
     * For a String, see if it can be converted to a valid date
     */
    @Override
    public boolean analyze(final String value, final TDataTableColumn column, final int rowNumber) {
        if (null == value) {
            return false;
        }
        return null != convertValue(value);
    }

    /**
     * Get mapped @link DataTableColumnType
     */
    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.DATE;
    }

    /**
     * For a date, always 0
     */
    @Override
    public int getLength() {
        return 0;
    }

}
