package org.tdar.oai.server;

import java.util.Arrays;

import javax.persistence.Transient;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.exception.OAIException;
import org.tdar.core.exception.OaiErrorCode;
import org.tdar.oai.service.OaiPmhService;
import org.tdar.struts.data.oai.OAIMetadataFormat;
import org.tdar.struts.data.oai.OAIVerb;
import org.tdar.utils.MessageHelper;

@Path("/")
@Component
public class OaiPmhServer {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OaiPmhService service;

    @Path("oai")
    @Produces("text/xml")
    @GET
    public Response execute(@QueryParam("verb") String verb_,
            @QueryParam("identifier") String identifier,
            @QueryParam("metadataPrefix") String metadataPrefix,
            @QueryParam("set") String set,
            @QueryParam("from") String from,
            @QueryParam("until") String until,
            @QueryParam("resumptionToken") String resumptionToken) throws ParseException {

        OAIVerb verb = OAIVerb.fromString(verb_);
        Object entity = null;
        try {
            String message;
            switch (verb) {
                case GET_RECORD:
                    message = getText("oaiController.not_allowed_with_get");

                    assertParameterIsNull(resumptionToken, "resumptionToken", message);
                    assertParameterIsNull(from, "from", message);
                    assertParameterIsNull(until, "until", message);

                    if (identifier == null) {
                        throw new OAIException(getText("oaiController.missing_identifer"), OaiErrorCode.BAD_ARGUMENT);
                    }

                    // check the requested metadata format
                    OAIMetadataFormat requestedFormat = OAIMetadataFormat.fromString(metadataPrefix);

                    if (requestedFormat == null) {
                        throw new OAIException(getText("oaiController.invalid_metadata_param"), OaiErrorCode.BAD_ARGUMENT);
                    }

                    entity = service.getGetRecordResponse(identifier, requestedFormat);
                    break;
                case IDENTIFY:
                    message = getText("oaiController.not_allowed_with_identity");
                    assertParameterIsNull(metadataPrefix, "metadataPrefix", message);
                    assertParameterIsNull(identifier, "identifier", message);
                    assertParameterIsNull(set, "set", message);
                    assertParameterIsNull(resumptionToken, "resumptionToken", message);
                    assertParameterIsNull(from, "from", message);
                    assertParameterIsNull(until, "until", message);
                    entity = service.getIdentifyResponse();
                    break;
                case LIST_IDENTIFIERS:
                    entity = service.listIdentifiers(from, until, metadataPrefix, resumptionToken);
                    break;
                case LIST_METADATA_FORMATS:
                    message = "Not allowed with ListMetadataFormats verb";
                    assertParameterIsNull(metadataPrefix, "metadataPrefix", message);
                    assertParameterIsNull(set, "set", message);
                    assertParameterIsNull(resumptionToken, "resumptionToken", message);
                    assertParameterIsNull(from, "from", message);
                    assertParameterIsNull(until, "until", message);
                    entity = service.listMetadataFormats(identifier);
                    break;
                case LIST_RECORDS:
                    entity = service.listRecords(from, until, metadataPrefix, resumptionToken);
                    break;
                case LIST_SETS:
                    entity = service.listSets(from, until, metadataPrefix, resumptionToken);
                    break;
            }
        } catch (OAIException oaie) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        }

        return Response.ok(entity).build();
    }

    private void assertParameterIsNull(Object parameter, String parameterName, String message) throws OAIException {
        if (parameter != null) {
            throw new OAIException(getText("oaiController.bad_arguement", parameterName, message), OaiErrorCode.BAD_ARGUMENT);
        }
    }

    public String getText(String key) {
        return MessageHelper.getMessage(key);
    }

    public String getText(String key, String param, String msg) {
        return MessageHelper.getMessage(key, Arrays.asList(param, msg));
    }
}
