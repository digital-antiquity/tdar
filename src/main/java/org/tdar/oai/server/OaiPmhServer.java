package org.tdar.oai.server;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Transient;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.exception.OAIException;
import org.tdar.core.exception.OaiErrorCode;
import org.tdar.oai.service.OaiIdentifier;
import org.tdar.oai.service.OaiPmhService;
import org.tdar.struts.data.oai.OAIMetadataFormat;
import org.tdar.struts.data.oai.OAIResumptionToken;
import org.tdar.struts.data.oai.OAIVerb;
import org.tdar.utils.MessageHelper;

@Path("/oai")
@Component
@Scope("prototype")
public class OaiPmhServer {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OaiPmhService service;

    private OAIVerb verb;

    private OAIMetadataFormat requestedFormat;

    private OaiIdentifier identifier;

    private OAIResumptionToken resumptionToken;

    private Date from = new DateTime("1900").toDate();
    private Date until = new DateTime("3000").toDate();

    @Produces("application/xml")
    @GET
    public Response execute(@QueryParam("verb") String verb_,
            @QueryParam("identifier") String identifier_,
            @QueryParam("metadataPrefix") String metadataPrefix_,
            @QueryParam("set") String set,
            @QueryParam("from") String from_,
            @QueryParam("until") String until_,
            @QueryParam("resumptionToken") String resumptionToken_) throws ParseException {
        Object entity = null;
        String message;
        try {
            prepare(verb_, identifier_, metadataPrefix_, from_, until_, resumptionToken_);
            switch (verb) {
                case GET_RECORD:
                    message = getText("oaiController.not_allowed_with_get");

                    assertParameterIsNull(resumptionToken, "resumptionToken", message);
                    assertParameterIsNull(from_, "from", message);
                    assertParameterIsNull(until_, "until", message);

                    if (identifier == null) {
                        throw new OAIException(getText("oaiController.missing_identifer"), OaiErrorCode.BAD_ARGUMENT);
                    }

                    if (requestedFormat == null) {
                        throw new OAIException(getText("oaiController.invalid_metadata_param"), OaiErrorCode.BAD_ARGUMENT);
                    }

                    entity = service.getGetRecordResponse(identifier, requestedFormat);
                    break;
                case IDENTIFY:
                    message = getText("oaiController.not_allowed_with_identity");
                    assertParameterIsNull(requestedFormat, "metadataPrefix", message);
                    assertParameterIsNull(identifier, "identifier", message);
                    assertParameterIsNull(set, "set", message);
                    assertParameterIsNull(resumptionToken, "resumptionToken", message);
                    assertParameterIsNull(from_, "from", message);
                    assertParameterIsNull(until_, "until", message);
                    entity = service.getIdentifyResponse();
                    break;
                case LIST_IDENTIFIERS:
                    entity = service.listIdentifiers(from, until, requestedFormat, resumptionToken);
                    break;
                case LIST_METADATA_FORMATS:
                    message = "Not allowed with ListMetadataFormats verb";
                    assertParameterIsNull(requestedFormat, "metadataPrefix", message);
                    assertParameterIsNull(set, "set", message);
                    assertParameterIsNull(resumptionToken, "resumptionToken", message);
                    assertParameterIsNull(from_, "from", message);
                    assertParameterIsNull(until_, "until", message);
                    entity = service.listMetadataFormats(identifier);
                    break;
                case LIST_RECORDS:
                    entity = service.listRecords(from, until, requestedFormat, resumptionToken);
                    break;
                case LIST_SETS:
                    entity = service.listSets(from, until, requestedFormat, resumptionToken);
                    break;
            }
        } catch (OAIException oaie) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        }

        return Response.ok(entity).build();
    }

    private void prepare(String verb_, String identifier_, String metadataPrefix_, String from_, String until_, String resumptionToken_) throws OAIException {
        if (verb_ == null) {
            throw new OAIException(getText("oaiController.bad_verb"), OaiErrorCode.BAD_VERB);
        } 
        verb = OAIVerb.fromString(verb_);
        if (StringUtils.isNotBlank(metadataPrefix_)) {
            requestedFormat = OAIMetadataFormat.fromString(metadataPrefix_);
        }
        if (StringUtils.isNotBlank(identifier_)) {
            identifier = new OaiIdentifier(identifier_);
        }

        if (StringUtils.isNotBlank(resumptionToken_)) {
            resumptionToken = new OAIResumptionToken(resumptionToken_);
        }

        if (StringUtils.isNotBlank(from_)) {
            from = new DateTime(from_).toDate();
        }
        if (StringUtils.isNotBlank(until_)) {
            until = new DateTime(until_).toDate();
        }

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
