package org.tdar.oai.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.SerializationService;
import org.tdar.oai.bean.OAIMetadataFormat;
import org.tdar.oai.bean.OAIRecordType;
import org.tdar.oai.bean.OAIResumptionToken;
import org.tdar.oai.bean.OaiIdentifier;
import org.tdar.oai.bean.Token;
import org.tdar.oai.bean.generated.oai._2_0.DeletedRecordType;
import org.tdar.oai.bean.generated.oai._2_0.DescriptionType;
import org.tdar.oai.bean.generated.oai._2_0.GetRecordType;
import org.tdar.oai.bean.generated.oai._2_0.GranularityType;
import org.tdar.oai.bean.generated.oai._2_0.HeaderType;
import org.tdar.oai.bean.generated.oai._2_0.IdentifyType;
import org.tdar.oai.bean.generated.oai._2_0.ListIdentifiersType;
import org.tdar.oai.bean.generated.oai._2_0.ListMetadataFormatsType;
import org.tdar.oai.bean.generated.oai._2_0.ListRecordsType;
import org.tdar.oai.bean.generated.oai._2_0.ListResponse;
import org.tdar.oai.bean.generated.oai._2_0.ListSetsType;
import org.tdar.oai.bean.generated.oai._2_0.MetadataFormatType;
import org.tdar.oai.bean.generated.oai._2_0.MetadataType;
import org.tdar.oai.bean.generated.oai._2_0.OAIPMHerrorcodeType;
import org.tdar.oai.bean.generated.oai._2_0.RecordType;
import org.tdar.oai.bean.generated.oai._2_0.ResumptionTokenType;
import org.tdar.oai.bean.generated.oai._2_0.SetType;
import org.tdar.oai.bean.generated.oai_identifier._2_0.OaiIdentifierType;
import org.tdar.oai.bean.generated.oai_identifier._2_0.ObjectFactory;
import org.tdar.oai.dao.OaiPmhDao;
import org.tdar.oai.dao.OaiSearchResult;
import org.tdar.oai.exception.OAIException;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ExtendedDcTransformer;
import org.tdar.transform.ModsTransformer;
import org.tdar.utils.MessageHelper;
import org.w3c.dom.Document;

import com.ibm.icu.util.BytesTrie.Result;

import edu.asu.lib.dc.DublinCoreDocument;

@Service
public class OaiPmhService {

	OaiPmhConfiguration config = OaiPmhConfiguration.getInstance();

	private boolean enableEntities = config.getEnableEntityOai();

	@Autowired
	private GenericDao genericDao;

	@Autowired
	private OaiPmhDao oaiDao;

	@Autowired
	private SerializationService serializationService;

	@Autowired
	private ObfuscationService obfuscationService;

	@Transient
	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Sets up and returns the "Identify" Verb for an OAI-PMH Response
	 * 
	 * @return
	 */
	 @Transactional(readOnly = true)
	public IdentifyType getIdentifyResponse() {
		IdentifyType ident = new IdentifyType();
		ident.setBaseURL(config.getBaseUrl() + "/oai-pmh/oai");
		ident.setDeletedRecord(DeletedRecordType.NO);
		ident.getAdminEmail().add(config.getSystemAdminEmail());
		ident.setEarliestDatestamp("2008-01-01");
		ident.setGranularity(GranularityType.YYYY_MM_DD);
		ident.setProtocolVersion("2.0");
		ident.setRepositoryName(config.getRepositoryName());
		OaiIdentifierType identifier = new OaiIdentifierType();
		identifier.setDelimiter(":");
		identifier.setRepositoryIdentifier(config.getRepositoryNamespaceIdentifier());
		OaiIdentifier id = new OaiIdentifier(OAIRecordType.RESOURCE, 1L);
		identifier.setSampleIdentifier(id.getOaiId());
		identifier.setScheme("oai");
		ObjectFactory factory = new ObjectFactory();
		DescriptionType description = new DescriptionType();
		description.setAny(factory.createOaiIdentifier(identifier));
		ident.getDescription().add(description);
		return ident;
	}

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
	public ResumptionTokenType listIdentifiersOrRecords(Date from, Date until, OAIMetadataFormat metadataPrefix,
			OAIResumptionToken resumptionToken, ListResponse response) throws OAIException {
		ResumptionTokenType token = null;
		Long collectionId = null;
		// start record number (cursor)
		Date effectiveFrom = from;
		Token cursor = null;
		Date effectiveUntil = until;
		OAIMetadataFormat metadataFormat = metadataPrefix;
		if (resumptionToken != null) {
			/*
			 * ... then this is the second or subsequent page of results. In
			 * this case there are no separate "from" and "until" parameters
			 * passed by the client; instead all the parameters come packed into
			 * a resumption token.
			 */
			collectionId = resumptionToken.getSet();
			cursor = resumptionToken.getCursor();
			effectiveFrom = resumptionToken.getEffectiveFrom(from);
			effectiveUntil = resumptionToken.getEffectiveUntil(until);
			metadataFormat = resumptionToken.getEffectiveMetadataPrefix(metadataPrefix);
		}

		// now actually build the queries and execute them

		if (!OaiPmhConfiguration.getInstance().enableTdarFormatInOAI() && Objects.equals(metadataFormat, OAIMetadataFormat.TDAR)) {
	          throw new OAIException(MessageHelper.getInstance().getText("oaiRecordType.unknown_type",Arrays.asList(OAIMetadataFormat.TDAR)),
	                    OAIPMHerrorcodeType.NO_RECORDS_MATCH);

		}

//		OaiSearchResult persons = null;
//		OaiSearchResult institutions = null;
		if (enableEntities && !Objects.equals(metadataFormat, OAIMetadataFormat.MODS) && !Objects.equals(metadataFormat, OAIMetadataFormat.EXTENDED_DC)) {
			// list people
		    throw new TdarRecoverableRuntimeException("not implemented");
//			persons = populateResult(OAIRecordType.PERSON, metadataFormat, effectiveFrom, effectiveUntil, token, response, null);
//
//			if (persons.getTotalRecords() > maxResults) {
//				maxResults = persons.getTotalRecords();
//			}
//
//			institutions = populateResult(OAIRecordType.INSTITUTION, metadataFormat, effectiveFrom, effectiveUntil, startRecord, response, null);
//
//			if (institutions.getTotalRecords() > maxResults) {
//				maxResults = institutions.getTotalRecords();
//			}
		}

		// list the resources
		OaiSearchResult resources = populateResult(OAIRecordType.RESOURCE, metadataFormat, effectiveFrom, effectiveUntil, cursor, response, collectionId);


		token = new ResumptionTokenType();
		// if any of the queries returned more than a page of search results,
		// create a resumptionToken to allow
		// the client to continue harvesting from that point
		if (resources.getResultSize() >= resources.getRecordsPerPage() ) {
			// ... then this is a partial response, and should be terminated
			// with a ResumptionToken
			// which may be empty if this is the last page of results advance
			// the cursor by one page
			OAIResumptionToken newResumptionToken = new OAIResumptionToken();
			// ... populate the resumptionToken so the harvester can continue
			// harvesting from the next page
			Token cursor2 = extractCursorFromResults(resources.getResults());
			newResumptionToken = new OAIResumptionToken(cursor2, effectiveFrom,
					effectiveUntil, metadataFormat, collectionId);
			logger.debug("newToken: {}", newResumptionToken.getToken());
			token.setValue(newResumptionToken.getToken());
			if (response instanceof ListIdentifiersType) {
				response.setResumptionToken(token);
			}

		}

		// if there were no records found, then throw an exception
		if (resources.getResultSize() == 0) {
			throw new OAIException(MessageHelper.getInstance().getText("oaiController.no_matches"),
					OAIPMHerrorcodeType.NO_RECORDS_MATCH);
		}
		return token;
	}

	/**
	 * Takes the query and performs the search, it then fills out the response
	 * with the results based on whether this is a "ListIdentifiers" or
	 * "ListRecords" Verb
	 * 
	 * @param recordType
	 * @param queryBuilder
	 * @param metadataFormat
	 * @param effectiveFrom
	 * @param effectiveUntil
	 * @param startRecord
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws OAIException
	 */
	private OaiSearchResult populateResult(OAIRecordType recordType, 
			OAIMetadataFormat metadataFormat, Date effectiveFrom, Date effectiveUntil, Token cursor,
			ListResponse response, Long collectionId) throws OAIException {
		boolean includeRecords = false;
		if (response instanceof ListRecordsType) {
			includeRecords = true;
		}
		OaiSearchResult search = new OaiSearchResult();
		if (cursor != null) {
		    search.setCursor(cursor);
		}

		List<RecordType> records = new ArrayList<>();
		try {
			for (OaiDcProvider resource : oaiDao.handleSearch(recordType, search, effectiveFrom, effectiveUntil, collectionId)) {
				// create OAI metadata for the record
				records.add(createRecord(resource, recordType, metadataFormat, includeRecords));
			}
		} catch (TdarRecoverableRuntimeException e) {
			// this is expected as the cursor follows the "max" results for
			// person/inst/resource so, whatever the max is
			// means that the others will throw this error.
			logger.debug("an exception happened .. {} ", e);
		}

		if (response instanceof ListRecordsType) {
			((ListRecordsType) response).getRecord().addAll(records);
		}

		// if we're listing identifiers, we just use the "header"
		if (response instanceof ListIdentifiersType) {
			ListIdentifiersType listIdentifiersType = (ListIdentifiersType) response;
			for (RecordType record : records) {
				listIdentifiersType.getHeader().add(record.getHeader());
			}
		}

		return search;
	}

	/**
	 * Creates the XML Record object that gets fed into "GetRecord" and
	 * "ListRecord"
	 * 
	 * @param resource
	 * @param recordType
	 * @param metadataFormat
	 * @param includeRecords
	 * @return
	 * @throws OAIException
	 */
	private RecordType createRecord(OaiDcProvider resource, OAIRecordType recordType, OAIMetadataFormat metadataFormat,
			boolean includeRecords) throws OAIException {
		RecordType record = new RecordType();
		HeaderType header = new HeaderType();

		obfuscationService.obfuscate((Obfuscatable) resource, null);
		header.setIdentifier(new OaiIdentifier(recordType, resource.getId()).getOaiId());
		Date updatedDate = resource.getDateUpdated();
		if (updatedDate == null) {
			updatedDate = resource.getDateCreated();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		header.setDatestamp(formatter.format(updatedDate));
		// iso_utc
		record.setHeader(header);
		if (resource instanceof Resource) {
            for (ResourceCollection rc : ((Resource) resource).getSharedResourceCollections()) {
                header.getSetSpec().add(Long.toString(rc.getId()));
                Set<Long> parents = new HashSet<>(rc.getParentIds());
                parents.addAll(rc.getAlternateParentIds());
                for (Long pid : parents) {
                header.getSetSpec().add(Long.toString(pid));
                }
            }
		}
		if (includeRecords) {
			MetadataType metadata = new MetadataType();
			try {
				Object meta = null;
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.newDocument();
				switch (metadataFormat) {
				case DC:
					if (resource instanceof Creator) {
						Creator<?> creator = (Creator<?>) resource;
						// create a "mock" DC record for the creator?
						DublinCoreDocument dcdoc = new DublinCoreDocument();
						dcdoc.getTitle().add(creator.getProperName());
						if (StringUtils.isNotBlank(creator.getDescription())) {
							dcdoc.getDescription().add(creator.getDescription());
						}
						dcdoc.getIdentifier().add(creator.getId().toString());
						dcdoc.getType().add(creator.getClass().getSimpleName());
						meta = dcdoc;
					} else {
						meta = DcTransformer.transformAny((Resource) resource);
					}
					break;
                case MODS:
                    // mods not supported by creators
                    if (resource instanceof Creator) {
                        throw new OAIException(MessageHelper.getInstance().getText("oaiController.cannot_disseminate"),
                                OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT);
                    }

                    meta = ModsTransformer.transformAny((Resource) resource).getRootElement();
                    break;
                case EXTENDED_DC:
                    // mods not supported by creators
                    if (resource instanceof Creator) {
                        throw new OAIException(MessageHelper.getInstance().getText("oaiController.cannot_disseminate"),
                                OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT);
                    }

                    meta = ExtendedDcTransformer.transformAny((Resource) resource).getRootElement();
                    break;
				case TDAR:
					try {
						// FIXME: this is less than ideal, but we seem to have
						// an issue with this throwing
						// LazyIntializationExceptions when the response
						// serializes this after the servlet controller method
						// is called
						serializationService.convertToXML(resource, document);
						meta = document.getDocumentElement();
					} catch (Exception e) {
						logger.error("cannot serialize:", e);
					}
					break;
				}
				metadata.setAny(meta);

			} catch (ParserConfigurationException pe) {
				logger.error("ParserConfigException", pe);
			}
			record.setMetadata(metadata);
		}
		return record;
	}

	/**
	 * Lists the metadata formats for the given identifier. Response to
	 * "ListIdentifiers" verbs
	 * 
	 * @param identifier
	 * @return
	 * @throws OAIException
	 */
	 @Transactional(readOnly = true)
	public ListMetadataFormatsType listMetadataFormats(OaiIdentifier identifier) throws OAIException {
		ListMetadataFormatsType formats = new ListMetadataFormatsType();

		OAIMetadataFormat[] formatList = OAIMetadataFormat.values();
		if (identifier != null) {
			// an identifier was specified, so return a list of the metadata
			// formats which that record can be disseminated in
			formatList = identifier.getRecordType().getMetadataFormats();
		}
		if (!config.enableTdarFormatInOAI()) {
			ArrayUtils.removeElement(formatList, OAIMetadataFormat.TDAR);
		}
		for (OAIMetadataFormat f : formatList) {
			MetadataFormatType type = new MetadataFormatType();
			type.setMetadataNamespace(f.getNamespace());
			type.setMetadataPrefix(f.getPrefix());
			type.setSchema(f.getSchemaLocation());
			formats.getMetadataFormat().add(type);
		}
		return formats;
	}

	/**
	 * Returns the result of the "GetRecord" verb, it looks up the identifier
	 * and returns the recod.
	 * 
	 * @param identifier
	 * @param requestedFormat
	 * @return
	 * @throws OAIException
	 */
	 @Transactional(readOnly = true)
	public GetRecordType getGetRecordResponse(OaiIdentifier identifier, OAIMetadataFormat requestedFormat)
			throws OAIException {
		// check that this kind of record can be disseminated in the requested
		// format
		GetRecordType get = new GetRecordType();
		OAIRecordType type = identifier.getRecordType();
		type.checkCanDisseminateFormat(requestedFormat);

		Long id = identifier.getTdarId();
		OaiDcProvider oaiDcObject = (OaiDcProvider) genericDao.find(type.getActualClass(), id);
		RecordType record = createRecord(oaiDcObject, identifier.getRecordType(), requestedFormat, true);
		get.setRecord(record);

		return get;
	}

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
	 @Transactional(readOnly = true)
	public ListRecordsType listRecords(Date from, Date until, OAIMetadataFormat requestedFormat,
			OAIResumptionToken resumptionToken) throws OAIException {
		ListRecordsType response = new ListRecordsType();
		ResumptionTokenType token = listIdentifiersOrRecords(from, until, requestedFormat, resumptionToken, response);
		response.setResumptionToken(token);
		return response;
	}

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
	 @Transactional(readOnly = true)
	public ListIdentifiersType listIdentifiers(Date from, Date until, OAIMetadataFormat requestedFormat,
			OAIResumptionToken resumptionToken) throws OAIException {
		ListIdentifiersType response = new ListIdentifiersType();
		ResumptionTokenType token = listIdentifiersOrRecords(from, until, requestedFormat, resumptionToken, response);
		response.setResumptionToken(token);
		return response;
	}

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
	 @Transactional(readOnly = true)
	public ListSetsType listSets(Date from, Date until, OAIMetadataFormat requestedFormat,
			OAIResumptionToken resumptionToken) throws OAIException {
		// Sort results by dateUpdated ascending and filter by dates, either
		// supplied in OAI-PMH 'from' and 'to' parameters, or encoded as parts
		// of the
		// 'resumptionToken' Optionally filter results by date range In Lucene,
		// dates are stored as "yyyyMMddHHmmssSSS" in UTC time zone see
		// http://docs.jboss.org/hibernate/search/3.4/reference/en-US/html_single/#d0e3510
		// but in OAI-PMH, date parameters are ISO8601 dates, so we must remove
		// the punctuation.
		OaiSearchResult search = new OaiSearchResult();
		ListSetsType response = new ListSetsType();
		Date effectiveFrom = from;
		Date effectiveUntil = until;
		if (resumptionToken != null) {
			// ... then this is the second or subsequent page of results. In
			// this case there are no separate "from" and "until" parameters
			// passed by the client;
			// instead all the parameters come packed into a resumption token.
			search.setCursor(resumptionToken.getCursor());
			effectiveFrom = resumptionToken.getEffectiveFrom(from);
			effectiveUntil = resumptionToken.getEffectiveUntil(until);
		}
		OAIMetadataFormat metadataFormat = null;
		// now actually build the queries and execute them
//		int total = 0;
		Collection<SetType> setList = new ArrayList<>();
		List<? extends OaiDcProvider> results =  new ArrayList<>();
		try {
			results = oaiDao.handleSearch(null, search, effectiveFrom, effectiveUntil,null);
//			total = search.getTotalRecords();
			for (OaiDcProvider i : results) {
				logger.debug("{}, {}", i, ((Viewable) i).isViewable());
				SetType set = new SetType();
				ResourceCollection coll = (ResourceCollection) i;
				set.setSetName(coll.getName());
				DescriptionType descr = new DescriptionType();
				DublinCoreDocument dcdoc = new DublinCoreDocument();
				dcdoc.getDescription().add(coll.getDescription());
				descr.setAny(dcdoc);
				set.getSetDescription().add(descr);
				set.setSetSpec(Long.toString(coll.getId()));
				setList.add(set);
			}
		} catch (TdarRecoverableRuntimeException e) {
			// this is expected as the cursor follows the "max" results for
			// person/inst/resource so, whatever the max is
			// means that the others will throw this error.
			logger.debug("an exception happened .. {} ", e);
		}

		// if any of the queries returned more than a page of search results,
		// create a resumptionToken to allow
		// the client to continue harvesting from that point
		int recordsPerPage = search.getRecordsPerPage();
		if (search.getResultSize() >= recordsPerPage) {
			// ... then this is a partial response, and should be terminated
			// with a ResumptionToken
			// which may be empty if this is the last page of results
			// advance the cursor by one page
			// check if there would be any resources, persons or institutions in
			// that hypothetical next page

			// check if there would be any resources, persons or institutions in
			// that hypothetical next page
				// ... populate the resumptionToken so the harvester can
				// continue harvesting from the next page
				OAIResumptionToken newResumptionToken = new OAIResumptionToken();
				ResumptionTokenType token = new ResumptionTokenType();
				Token cursor = extractCursorFromResults(results);
				newResumptionToken = new OAIResumptionToken(cursor, effectiveFrom, effectiveUntil, metadataFormat,
						null);
				token.setValue(newResumptionToken.getToken());
				response.setResumptionToken(token);
		}
		response.getSet().addAll(setList);
		return response;
	}

    private Token extractCursorFromResults(List<? extends OaiDcProvider> results) {
        Token token = new Token();
        OaiDcProvider lastRecord = results.get(results.size() - 1);
        token.setAfter(lastRecord.getDateUpdated());
        token.setIdFrom(lastRecord.getId());    
        return token;
    }
}
