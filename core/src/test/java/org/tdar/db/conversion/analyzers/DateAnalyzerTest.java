package org.tdar.db.conversion.analyzers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;

public class DateAnalyzerTest {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String[] validDates = {
            "1st June, 2003",
            "7/7/87",
            "1978-01-28",
            "1984/04/02",
            "1/02/1980",
            "2/28/79",
            "The 31st of April in the year 2008",
            "Fri, 21 Nov 1997",
            "Jan 21, '97",
            "Sun, Nov 21",
            "jan 1st",
            "february twenty-eighth",
            "1/04/2003",
            "1st March 03",
            "4/03/2003",
    };

    // Faulty dates are accepted: they are just either nudged along into the next month or have parts dropped.
    private String[] faultyDates = {
            "31st February, 2011", // not a leap year
    };

    // It could be argued that some of these invalid dates are actually perfectly acceptable.
    private String[] invalidDates = {
            "1234",
            "1234.6",
            "stringa4",
            "14/13/2013",
            "40/4/2011",
            "1-Feb-03",
            "2/1",
            "August 1993",
            "personal communication, email 2/23/08"
    };

    private String[] alternativeDates = {
            "next wed or thurs",
            "oct 3rd or 4th"
    };

    private String[] recurringDates = {
            "every January 31st",
            "every 30th February"
    };

    private DateAnalyzer da;

    @Before
    public void setup() {
        da = new DateAnalyzer();
    }

    @Test
    public void doesReturnCorrectColumnType() {
        DataTableColumnType type = da.getType();
        assertTrue("DateAnalyzer.getType() should return DataTableColumnType.DATE, not " + type, DataTableColumnType.DATE.equals(type));
    }

    @Test
    public void testInvalidMonthDay() {
        boolean result = da.analyze("9-01", null, 1);
        logger.debug("result: {}", result);
        assertFalse(result);
        result = da.analyze("9-01-2014", null, 1);
        logger.debug("result: {}", result);
        assertTrue(result);
    }

    private void testLength(final String target, final int expectation) {
        da.analyze(target, null, 1);
        final int length = da.getLength();
        assertTrue("DateAnalyzer.getLength() should return " + expectation + " for the string '" + target + "', not " + length, length == expectation);
    }

    @Test
    public void doesReturnCorrectLengthForNull() {
        testLength(null, 0);
    }

    @Test
    public void doesReturnCorrectLengthForEmptyString() {
        testLength("", 0);
    }

    @Test
    public void doesReturnCorrectLengthForNonEmptyString() {
        testLength("some", 0);
    }

    @Test
    public void doesNotRemberLongestString() {
        testLength("", 0);
        testLength("some", 0);
        testLength("se", 0);
    }

    @Test
    public void doesNotThrowExceptionOnNonDateString() {
        da.analyze("ostridge", null, 1);
    }

    @Test
    public void doesNotReturnDateOnNonDateString() {
        assertFalse("An ostridge is not a date!", da.analyze("ostridge", null, 1));
    }

    @Test
    public void doesFindValidDate() {
        for (String aDate : validDates) {
            assertTrue("A valid date has not been found: " + aDate, da.analyze(aDate, null, 1));
        }
    }

    @Test
    public void doesFindFaultyDates() {
        for (String aDate : faultyDates) {
            assertTrue("A valid date has not been created: " + aDate, da.analyze(aDate, null, 1));
        }
    }

    @Test
    public void invalidDatesAreRejected() {
        for (String aDate : invalidDates) {
            assertFalse("An invalid date has been accepted: " + aDate, da.analyze(aDate, null, 1));
        }
    }

    @Test
    public void alternativeDatesAreRejected() {
        for (String aDate : alternativeDates) {
            assertFalse("An alternate date has been accepted: " + aDate, da.analyze(aDate, null, 1));
        }
    }

    @Test
    public void recurringDatesAreRejected() {
        for (String aDate : recurringDates) {
            assertFalse("An recurring date has been accepted: " + aDate, da.analyze(aDate, null, 1));
        }
    }
}
