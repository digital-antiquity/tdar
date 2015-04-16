package org.tdar.dataone.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public class AbstractDataOneResponse {

    private static final String ISO_822 = "EEE, dd MMM yyyy HH:mm:ss Z";
    private static final String HEADER_DATE = "Date";
    public static final String APPLICATION_XML = "application/xml";
    private static final SimpleDateFormat format = new SimpleDateFormat(ISO_822);
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String FORMAT_ID = "formatId";
    public static final String IDENTIFIER = "identifier";
    public static final String START = "start";
    public static final String COUNT = "count";
    public static final String EVENT = "event";
    public static final String ID_FILTER = "idFilter";

    public void setupResponseContext(HttpServletResponse response) {
        response.setHeader(HEADER_DATE, toIso822(new Date()));
    }

    public String toIso822(Date date) {
        return format.format(date);
    }
    
    
    
}
