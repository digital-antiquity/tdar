package org.tdar.dataone.server;

import java.util.Date;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.dataone.service.DataOneService;

@Path("/v1/dirtySystemMetadata")
@Component
@Scope(value="prototype")
public class DirtySystemMetadataResponse extends AbstractDataOneResponse {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    @Autowired
    DataOneService service;
    
    @POST
    @Produces("text/plain")
    public Response synchronizationFailed(@QueryParam("id") String id,@QueryParam("serialVersion") long serialVersion, @QueryParam("dateSysMetaLastModified") Date dateSysMetaLastModified) {
        setupResponseContext(response);
        service.synchronizationFailed(id, serialVersion, dateSysMetaLastModified, request);
        return Response.ok().build();
    }

}
