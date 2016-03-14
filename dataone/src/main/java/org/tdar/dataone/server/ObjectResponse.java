package org.tdar.dataone.server;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.dataone.service.types.v1.Event;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.dataone.service.DataOneService;
import org.tdar.dataone.service.ObjectResponseContainer;

@Component
@Scope("prototype")
@Path(AbstractDataOneResponse.BASE_PATH + "object")
public class ObjectResponse extends AbstractDataOneResponse {

    private static final String SERVER_ERROR = "ServerError";
    private static final String DATA_ONE_OBJECT_FORMAT = "DataONE-ObjectFormat";
    private static final String DATA_ONE_CHECKSUM = "DataONE-Checksum";
    private static final String LAST_MODIFIED = "Last-Modified";
    private static final String DATA_ONE_SERIAL_VERSION = "DataONE-SerialVersion";
    private static final String IDONTEXIST = "IDONTEXIST";
    private static final String SPECIFIED_OBJECT_DOES_NOT_EXIST_ON_THIS_NODE = "specified object does not exist on this node.";
    private static final String DATA_ONE_EXCEPTION_NAME = "DataONE-Exception-Name";
    private static final String DATA_ONE_EXCEPTION_DETAIL_CODE = "DataONE-Exception-DetailCode";
    private static final String DATA_ONE_EXCEPTION_DESCRIPTION = "DataONE-Exception-Description";
    private static final String DATA_ONE_EXCEPTION_PID = "DataONE-Exception-PID";
    private static final String NOT_FOUND = "NotFound";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataOneService service;
    /*
     * Raises:
     * Exceptions.NotImplemented –
     * (errorCode=501, detailCode=2041)
     * Ping is a required operation and so an operational member node should never return this exception unless under development.
     * 
     * Exceptions.ServiceFailure –
     * (errorCode=500, detailCode=2042)
     * 
     * A ServiceFailure exception indicates that the node is not currently operational as a member node. A coordinating node or monitoring service may use this
     * as an indication that the member node should be taken out of the pool of active nodes, though ping should be called on a regular basis to determine when
     * the node might b ready to resume normal operations.
     * 
     * Exceptions.InsufficientResources –
     * (errorCode=413, detailCode=2045)
     * 
     * A ping response may return InsufficientResources if for example the system is in a state where normal DataONE operations may be impeded by an unusually
     * high load on the node.
     */

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("{id:.*}")
    public Response object(@PathParam("id") String id) {
        setupResponseContext(response, request);
        return constructObjectResponse(id, request,Event.READ);
    }

    @HEAD
    @Path("{id:.*}")
    @Produces(APPLICATION_XML)
    public Response describe(@PathParam("id") String id) {
        logger.debug("object head request: {}", request);
        setupResponseContext(response, request);
        try {
            ObjectResponseContainer container = service.getObject(id, request, null);
            if (container != null) {
                logger.debug("returning OK");
                response.setHeader(DATA_ONE_OBJECT_FORMAT, container.getObjectFormat());
                response.setHeader(DATA_ONE_CHECKSUM, "MD5," + container.getChecksum());
                response.setHeader(LAST_MODIFIED, toIso822(container.getLastModified()));
                response.setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(container.getSize()));
                response.setHeader(DATA_ONE_SERIAL_VERSION, container.getSerialVersionId().toString());
                return Response.ok().build();
            }

        } catch (Exception e) {
            logger.error("execption in DataOne object head request", e);
            response.setHeader(DATA_ONE_EXCEPTION_NAME, SERVER_ERROR);
//            response.setHeader(DATA_ONE_EXCEPTION_DETAIL_CODE, "1380");
//            response.setHeader(DATA_ONE_EXCEPTION_DESCRIPTION, SPECIFIED_OBJECT_DOES_NOT_EXIST_ON_THIS_NODE);
            response.setHeader(DATA_ONE_EXCEPTION_PID, id);
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        response.setHeader(DATA_ONE_EXCEPTION_NAME, NOT_FOUND);
        response.setHeader(DATA_ONE_EXCEPTION_DETAIL_CODE, "1380");
        response.setHeader(DATA_ONE_EXCEPTION_DESCRIPTION, SPECIFIED_OBJECT_DOES_NOT_EXIST_ON_THIS_NODE);
        response.setHeader(DATA_ONE_EXCEPTION_PID, IDONTEXIST);
        return Response.serverError().status(Status.NOT_FOUND).build();
    }

    @GET
    @Produces(APPLICATION_XML)
    public Response listObjects(
            @QueryParam(FROM_DATE) String fromDate_,
            @QueryParam(TO_DATE) String toDate_,
            @QueryParam(FORMAT_ID) String formatid,
            @QueryParam(IDENTIFIER) String identifier,
            @QueryParam(START) @DefaultValue("0") int start,
            @QueryParam(COUNT) @DefaultValue("10") int count

            ) {
        setupResponseContext(response, request);
        try {
            Date fromDate = null;
            Date toDate = null;
            if (StringUtils.isNotBlank(fromDate_)) {
                fromDate = DateTime.parse(fromDate_).toDate();
            }
            if (StringUtils.isNotBlank(toDate_)) {
                toDate = DateTime.parse(toDate_).toDate();
            }
            return Response.ok().entity(service.getListObjectsResponse(fromDate, toDate, formatid, identifier, start, count)).build();
        } catch (Exception e) {
            logger.error("error in listObjects: {}",e ,e);
        }
        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
    }

}
