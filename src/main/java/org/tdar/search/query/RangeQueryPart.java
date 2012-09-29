package org.tdar.search.query;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class RangeQueryPart implements QueryPart {

    private String field;
    private String start;
    private String end;
    
    //the date format used by lucene (I think)
    private static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS");

    public RangeQueryPart(String field, String start, String end) {
        this.field = field;
        this.start = start == null ? "*" : start;
        this.end = end == null ? "*" : end;
    }
    
    public RangeQueryPart(String field, Date start, Date end) {
    	this(field, convert(start), convert(end));
    }

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();
        q.append(field).append(":[").append(start).append(" TO ").append(end).append("] ");
        return q.toString();
    }
    
    private static String convert(Date date) {
    	if(date==null) return null;
    	DateTime dateTime = new DateTime(date);
        return dateTime.toString(dtf);
    }


}
