package org.tdar.dataone.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.dataone.service.types.v1.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.dataone.bean.DataOneError;
import org.tdar.dataone.service.DataOneService;
import org.tdar.dataone.service.ObjectResponseContainer;

@Component
@Scope("prototype")
public class AbstractDataOneResponse {

    public static final String BASE_PATH = "/{v:v2}/";
    private static final String ISO_822 = "EEE, dd MMM yyyy HH:mm:ss Z";
    private static final String HEADER_DATE = "Date";
    public static final String APPLICATION_XML = "application/xml; charset=utf-8";
    private static final SimpleDateFormat format = new SimpleDateFormat(ISO_822);
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String FORMAT_ID = "formatId";
    public static final String IDENTIFIER = "identifier";
    public static final String START = "start";
    public static final String COUNT = "count";
    public static final String EVENT = "event";
    public static final String ID_FILTER = "idFilter";
    public static final String PID_FILTER = "pidFilter";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataOneService service;

    public void setupResponseContext(HttpServletResponse response, HttpServletRequest request) {
        logger.debug(">>> {}?{}", request.getRequestURI(), request.getQueryString());
        response.setHeader(HEADER_DATE, toIso822(new Date()));
    }

    public String toIso822(Date date) {
        return format.format(date);
    }

    public DataOneError getNotFoundError() {
        return new DataOneError(404, "NotFound", "Not Found", 1800);
    }

    public DataOneError getNotImplementedError() {
        return new DataOneError(501, "NotImplemented", "Not Implemented", 2180);
    }

    public Response constructObjectResponse(String id, HttpServletRequest request, Event read) {
        logger.trace("object full request: {}", request);
        try {
            final ObjectResponseContainer container = service.getObject(id, request, Event.READ);
            StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    logger.debug("{} - {}", os, container.getReader());
                    OutputStreamWriter output = new OutputStreamWriter(os);
                    IOUtils.copyLarge(container.getReader(), output);
                    IOUtils.closeQuietly(output);
                };
            };
            if (container != null) {
                return Response.ok(stream).header(HttpHeaders.CONTENT_TYPE, container.getContentType()).build();
            } else {
                return Response.serverError().entity(getNotFoundError()).status(Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("error in DataOne getObject:", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
