package org.tdar.oai.service;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.oai.bean.OAIMetadataFormat;
import org.tdar.oai.bean.OAIResumptionToken;
import org.tdar.oai.bean.OaiIdentifier;
import org.tdar.oai.bean.generated.oai._2_0.GetRecordType;
import org.tdar.oai.bean.generated.oai._2_0.IdentifyType;
import org.tdar.oai.bean.generated.oai._2_0.ListIdentifiersType;
import org.tdar.oai.bean.generated.oai._2_0.ListMetadataFormatsType;
import org.tdar.oai.bean.generated.oai._2_0.ListRecordsType;
import org.tdar.oai.bean.generated.oai._2_0.ListResponse;
import org.tdar.oai.bean.generated.oai._2_0.ListSetsType;
import org.tdar.oai.bean.generated.oai._2_0.ResumptionTokenType;
import org.tdar.oai.exception.OAIException;

public interface OaiPmhService {

    /**
     * Sets up and returns the "Identify" Verb for an OAI-PMH Response
     * 
     * @return
     */
    IdentifyType getIdentifyResponse();

    /**
     * Sets up a "ListIdentifiers" Verb for an OAI-PMH Response. Finds all
     * resources in tDAR based on the date/time info
     * 
     * @param from
     * @param until
     * @param metadataPrefix
     * @param resumptionToken
     * @param response
     * @return
     * @throws OAIException
     * @throws ParseException
     */
    ResumptionTokenType listIdentifiersOrRecords(Date from, Date until, OAIMetadataFormat metadataPrefix,
            OAIResumptionToken resumptionToken, ListResponse response) throws OAIException;

    /**
     * Lists the metadata formats for the given identifier. Response to
     * "ListIdentifiers" verbs
     * 
     * @param identifier
     * @return
     * @throws OAIException
     */
    ListMetadataFormatsType listMetadataFormats(OaiIdentifier identifier) throws OAIException;

    /**
     * Returns the result of the "GetRecord" verb, it looks up the identifier
     * and returns the recod.
     * 
     * @param identifier
     * @param requestedFormat
     * @return
     * @throws OAIException
     */
    GetRecordType getGetRecordResponse(OaiIdentifier identifier, OAIMetadataFormat requestedFormat)
            throws OAIException;

    /**
     * Lists records in tDAR based on the number of tDAR IDs called by
     * "ListRecords" verb
     * 
     * @param from
     * @param until
     * @param requestedFormat
     * @param resumptionToken
     * @return
     * @throws OAIException
     * @throws ParseException
     */
    ListRecordsType listRecords(Date from, Date until, OAIMetadataFormat requestedFormat,
            OAIResumptionToken resumptionToken) throws OAIException;

    /**
     * Lists identifiers in tDAR based on the number of tDAR IDs called by
     * "ListIdentifiers" verb
     * 
     * @param from
     * @param until
     * @param requestedFormat
     * @param resumptionToken
     * @return
     * @throws OAIException
     * @throws ParseException
     */
    ListIdentifiersType listIdentifiers(Date from, Date until, OAIMetadataFormat requestedFormat,
            OAIResumptionToken resumptionToken) throws OAIException;

    /**
     * List collections in tDAR which are mapped to OAI as "sets." response to
     * ListSets verb.
     * 
     * @param from
     * @param until
     * @param requestedFormat
     * @param resumptionToken
     * @return
     * @throws OAIException
     */
    ListSetsType listSets(Date from, Date until, OAIMetadataFormat requestedFormat,
            OAIResumptionToken resumptionToken) throws OAIException;

}