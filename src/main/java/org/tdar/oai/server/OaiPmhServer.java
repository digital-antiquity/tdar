package org.tdar.oai.server;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Transient;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.exception.OAIException;
import org.tdar.core.service.GenericService;
import org.tdar.oai.bean.generated.OAIPMHerrorType;
import org.tdar.oai.bean.generated.OAIPMHerrorcodeType;
import org.tdar.oai.bean.generated.OAIPMHtype;
import org.tdar.oai.bean.generated.ObjectFactory;
import org.tdar.oai.bean.generated.RequestType;
import org.tdar.oai.bean.generated.VerbType;
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

    @Autowired
    private GenericService genericService;

    @Context 
    UriInfo uriInfo;
    
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
        OAIPMHtype response = new OAIPMHtype();
        RequestType request = createRequest(verb_, identifier_, metadataPrefix_, set, from_, until_, resumptionToken_);
        response.setRequest(request);
        ObjectFactory factory = new ObjectFactory();
        genericService.markReadOnly();
        try {
            response.setResponseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
            prepare(verb_, identifier_, metadataPrefix_, from_, until_, resumptionToken_);
            execute(set, from_, until_, response);
        } catch (OAIException oaie) {
            logger.error("OaiException", oaie);
            OAIPMHerrorType error = new OAIPMHerrorType();
            error.setCode(oaie.getCode());
            error.setValue(oaie.getMessage());
            response.getError().add(error);
        } catch (DatatypeConfigurationException e) {
            logger.error("DatatypeConfigurationException", e);
        } catch (Throwable e) {
            logger.error("Thrownable: {} {}", e.getClass(), e, e);
        }
        return Response.ok(factory.createOAIPMH(response)).build();
    }

    private RequestType createRequest(String verb_, String identifier_, String metadataPrefix_, String set, String from_, String until_, String resumptionToken_) {
        RequestType request = new RequestType();
        if (identifier_ != null) {
            request.setIdentifier(identifier_);
        }
        if (metadataPrefix_ != null) {
            request.setMetadataPrefix(metadataPrefix_);
        }
        if (resumptionToken_ != null) {
            request.setResumptionToken(resumptionToken_);
        }
        if (set != null) {
            request.setSet(set);
        }
        if (until_ != null) {
            request.setUntil(until_);
        }
        if (from_ != null) {
            request.setFrom(from_);
        }
        request.setValue(uriInfo.getRequestUri().toString());
        logger.debug(request.getValue());
        request.setVerb(VerbType.fromValue(verb_));
        return request;
    }

    @Transactional(readOnly = true)
    public void execute(String set, String from_, String until_, OAIPMHtype response) throws OAIException, ParseException {
        String message;
        switch (verb) {
            case GET_RECORD:
                message = getText("oaiController.not_allowed_with_get");

                assertParameterIsNull(resumptionToken, "resumptionToken", message);
                assertParameterIsNull(from_, "from", message);
                assertParameterIsNull(until_, "until", message);

                if (identifier == null) {
                    throw new OAIException(getText("oaiController.missing_identifer"), OAIPMHerrorcodeType.BAD_ARGUMENT);
                }

                if (requestedFormat == null) {
                    throw new OAIException(getText("oaiController.invalid_metadata_param"), OAIPMHerrorcodeType.BAD_ARGUMENT);
                }

                response.setGetRecord(service.getGetRecordResponse(identifier, requestedFormat));
                break;
            case IDENTIFY:
                message = getText("oaiController.not_allowed_with_identity");
                assertParameterIsNull(requestedFormat, "metadataPrefix", message);
                assertParameterIsNull(identifier, "identifier", message);
                assertParameterIsNull(set, "set", message);
                assertParameterIsNull(resumptionToken, "resumptionToken", message);
                assertParameterIsNull(from_, "from", message);
                assertParameterIsNull(until_, "until", message);
                response.setIdentify(service.getIdentifyResponse());
                break;
            case LIST_IDENTIFIERS:
                response.setListIdentifiers(service.listIdentifiers(from, until, requestedFormat, resumptionToken));
                break;
            case LIST_METADATA_FORMATS:
                message = "Not allowed with ListMetadataFormats verb";
                assertParameterIsNull(requestedFormat, "metadataPrefix", message);
                assertParameterIsNull(set, "set", message);
                assertParameterIsNull(resumptionToken, "resumptionToken", message);
                assertParameterIsNull(from_, "from", message);
                assertParameterIsNull(until_, "until", message);
                response.setListMetadataFormats(service.listMetadataFormats(identifier));
                break;
            case LIST_RECORDS:
                response.setListRecords(service.listRecords(from, until, requestedFormat, resumptionToken));
                break;
            case LIST_SETS:
                response.setListSets(service.listSets(from, until, requestedFormat, resumptionToken));
                break;
        }
    }

    private void prepare(String verb_, String identifier_, String metadataPrefix_, String from_, String until_, String resumptionToken_) throws OAIException {
        if (verb_ == null) {
            throw new OAIException(getText("oaiController.bad_verb"), OAIPMHerrorcodeType.BAD_VERB);
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
            throw new OAIException(getText("oaiController.bad_arguement", parameterName, message), OAIPMHerrorcodeType.BAD_ARGUMENT);
        }
    }

    public String getText(String key) {
        return MessageHelper.getMessage(key);
    }

    public String getText(String key, String param, String msg) {
        return MessageHelper.getMessage(key, Arrays.asList(param, msg));
    }
}
