package org.tdar.dataone.server;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.dataone.service.types.v1.Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.dataone.service.DataOneService;

@Path(AbstractDataOneResponse.BASE_PATH + "checksum")
@Component
@Scope("prototype")
public class ChecksumResponse extends AbstractDataOneResponse {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataOneService service;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("{pid:.*}")
    @Produces(APPLICATION_XML)
    public Response checksum(@PathParam("pid") String pid,
            @QueryParam("checksumAlgorithm") String checksum) {
        setupResponseContext(response, request);

        if (StringUtils.isNotEmpty(checksum) && !"MD5".equalsIgnoreCase(checksum)) {
            // FIXME: how do we get a list of algorithms
            // how do we set a detail code?
            return Response.serverError().status(Status.BAD_REQUEST).build();
        }

        try {
            Checksum checksumResponse = service.getChecksumResponse(pid, checksum);
            if (checksumResponse == null) {
                return Response.serverError().entity(getNotFoundError()).status(Status.NOT_FOUND).build();
            }
            return Response.ok().entity(checksumResponse).build();
        } catch (Exception e) {
            logger.error("error in checksum response: {}", e, e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
