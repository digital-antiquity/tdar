package org.tdar.dataone.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public class AbstractDataOneResponse {

    
    public void setupResponseContext(HttpServletResponse response) {
        String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date());
        response.setHeader("Date", date);
    }
    
    
    
}
