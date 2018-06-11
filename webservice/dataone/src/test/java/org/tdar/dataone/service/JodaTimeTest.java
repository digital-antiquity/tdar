package org.tdar.dataone.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class JodaTimeTest {

    @Test
    public void testDateParsing() {
        DateTime dt = new DateTime("2016-12-21T18:10:37.445", DateTimeZone.UTC);
        DateTime dt2 = new DateTime("2016-12-21T18:10:37.445+00:00", DateTimeZone.UTC);
        DateTime dt3 = new DateTime("2016-12-21T18:10:37.445Z", DateTimeZone.UTC);
        System.out.println(dt.toDateTime(DateTimeZone.getDefault()));
        System.out.println(dt2.toDateTime(DateTimeZone.getDefault()));
        System.out.println(dt3.toDateTime(DateTimeZone.getDefault()));
        // DateTime.parse("2016-12-21T18:10:37.445 00:00", DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS Z"));
    }
}
