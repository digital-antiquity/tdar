package org.tdar.dataone.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Log;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.NodeReplicationPolicy;
import org.dataone.service.types.v1.NodeState;
import org.dataone.service.types.v1.NodeType;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.ObjectInfo;
import org.dataone.service.types.v1.ObjectList;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Ping;
import org.dataone.service.types.v1.Schedule;
import org.dataone.service.types.v1.Service;
import org.dataone.service.types.v1.Services;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.Synchronization;
import org.dataone.service.types.v2.Node;
import org.dataone.service.types.v2.SystemMetadata;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.jdom2.JDOMException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.dao.resource.InformationResourceDao;
import org.tdar.dataone.bean.DataOneObject;
import org.tdar.dataone.bean.EntryType;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.LogEntryImpl;
import org.tdar.dataone.dao.DataOneDao;
import org.tdar.transform.ExtendedDcTransformer;

import edu.asu.lib.jaxb.JaxbDocumentWriter;
import edu.asu.lib.qdc.QualifiedDublinCoreDocument;

/**
 * The service backing DataOne controllers
 * 
 * @author abrin
 *
 */
@org.springframework.stereotype.Service
public class DataOneService implements DataOneConstants, D1Formatter {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    DataOneConfiguration D1CONFIG = DataOneConfiguration.getInstance();

    @Autowired
    private GenericService genericService;

    @Autowired
    private DataOneDao dataOneDao;

    @Autowired
    private ObfuscationService obfuscationService;

    @Autowired
    private InformationResourceDao informationResourceDao;

    /**
     * Create an OAI-ORE Resource Map this will include all versions of the files and metadata that get exposed to DataOne.
     * 
     * @param ir
     * @return
     * @throws OREException
     * @throws URISyntaxException
     * @throws ORESerialiserException
     * @throws JDOMException
     * @throws IOException
     */
    @Transactional(readOnly = true)
    public String createResourceMap(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException {
        OaiOreResourceMapGenerator generator = new OaiOreResourceMapGenerator(ir, false);
        return generator.generate();
    }

    /**
     * Formulates a NodeResponse
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNCore.getCapabilities
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public Node getNodeResponse() {
        Node node = new Node();
        node.setBaseURL(D1CONFIG.getBaseSecureUrl() + DATAONE);
        node.setDescription(CONFIG.getSystemDescription());
        node.setIdentifier(getTdarNodeReference());
        node.setName(CONFIG.getRepositoryName());

        // node.setNodeReplicationPolicy();
        Ping ping = new Ping();
        ping.setSuccess(Boolean.TRUE);
        node.setPing(ping);
        node.setReplicate(false);
        Services services = new Services();

        addService(MN_READ, VERSION, Boolean.TRUE, services);
        addService(MN_CORE, VERSION, Boolean.TRUE, services);
        addService(MN_AUTHORIZATION, VERSION, Boolean.FALSE, services);
        addService(MN_STORAGE, VERSION, Boolean.FALSE, services);
        addService(MN_REPLICATION, VERSION, Boolean.FALSE, services);

        NodeReplicationPolicy nrp = new NodeReplicationPolicy();
        nrp.setSpaceAllocated(BigInteger.valueOf(1024));
        node.setNodeReplicationPolicy(nrp);

        node.setServices(services);
        node.setState(NodeState.UP);
        List<Subject> list = new ArrayList<>();
        list.add(DataOneUtils.createSubject(D1CONFIG.getContactSubject()));
        node.setContactSubjectList(list);

        List<Subject> subjectList = new ArrayList<>();
        subjectList.add(DataOneUtils.createSubject(D1CONFIG.getSubject()));
        node.setSubjectList(subjectList);

        Synchronization sync = new Synchronization();
        sync.setLastCompleteHarvest(new DateTime(0, DateTimeZone.UTC).toDate());
        // node.getContactSubjectList().add(getSystemUserLdap());
        sync.setLastHarvested(new DateTime(DateTimeZone.UTC).toDate());
        Schedule schedule = new Schedule();
        schedule.setHour("20");
        schedule.setMday("*");
        schedule.setMin("*");
        schedule.setMon("*");
        schedule.setSec("*");
        schedule.setWday("?");
        schedule.setYear("*");
        // schedule.setWday("6");
        sync.setSchedule(schedule);
        node.setSynchronization(sync);
        node.setSynchronize(true);
        // node.setSynchronize();
        node.setType(NodeType.MN);
        return node;
    }

    /**
     * helper to create a node entry for tDAR
     */
    private NodeReference getTdarNodeReference() {
        NodeReference nodeReference = new NodeReference();
        nodeReference.setValue(D1CONFIG.getMemberNodeIdentifier());
        return nodeReference;
    }

    /**
     * Generate a service entry
     * 
     * @param name
     * @param version
     * @param available
     * @param services
     */
    private void addService(String name, String version, Boolean available, Services services) {
        Service service = new Service();
        service.setName(name);
        service.setVersion(version);
        service.setAvailable(available);
        services.getServiceList().add(service);
    }

    /**
     * DataOne logs all requests and to track what's been done, they can ask for and query their own longs.
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNCore.getLogRecords
     * 
     * @param fromDate
     * @param toDate
     * @param event
     * @param idFilter
     * @param start
     * @param count
     * @param request
     * @return
     */
    @Transactional(readOnly = true)
    public Log getLogResponse(Date fromDate, Date toDate, Event event, String idFilter, int start, int count, HttpServletRequest request) {
        Log log = new Log();

        log.setStart(start);
        log.setCount(count);
        logger.trace("logResponse: {} {} {} {} {} {} {}", fromDate, toDate, event, idFilter, start, count);
        List<LogEntryImpl> findLogFiles = dataOneDao.findLogFiles(fromDate, toDate, event, idFilter, start, count, log);
        // for each log entry
        for (LogEntryImpl impl : findLogFiles) {
            log.addLogEntry(impl.toEntry());
        }
        log.setCount(log.getLogEntryList().size());
        return log;
    }

    /**
     * Generate a checksum response -- Data One uses checksums mostly the way tDAR does
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.getChecksum
     * 
     * @param pid
     * @param checksum_
     * @return
     */
    @Transactional(readOnly = true)
    public Checksum getChecksumResponse(String pid, String checksum_) {
        DataOneObject resp = dataOneDao.findByIdentifier(pid);
        if (resp == null) {
            return null;
        }
        return DataOneUtils.createChecksum(resp.getChecksum());
    }

    /**
     * The object List response queries the database for objects that match and then returns them
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.listObjects
     * 
     * @param fromDate
     * @param toDate
     * @param formatid
     * @param identifier
     * @param start
     * @param count
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws OREException
     * @throws URISyntaxException
     * @throws ORESerialiserException
     * @throws JDOMException
     * @throws IOException
     * @throws JAXBException
     */
    @Transactional(readOnly = true)
    public ObjectList getListObjectsResponse(Date fromDate, Date toDate, String formatid, String identifier, int start, int count)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException,
            JAXBException {
        ObjectList list = new ObjectList();
        list.setCount(count);
        list.setStart(start);
        List<DataOneObject> dataOneObjects = dataOneDao.findUpdatedResources(fromDate, toDate, formatid, identifier, list, start, count);
        for (DataOneObject obj : dataOneObjects) {
            logger.trace("{}", obj);
            ObjectInfo item = new ObjectInfo();
            item.setChecksum(DataOneUtils.createChecksum(obj.getChecksum()));
            item.setDateSysMetadataModified(obj.getSysMetadataModified());
            item.setFormatId(DataOneUtils.createFormatId(obj.getFormatId()));
            item.setIdentifier(DataOneUtils.createIdentifier(obj.getIdentifier()));
            item.setSize(BigInteger.valueOf(obj.getSize()));
            list.addObjectInfo(item);
        }

        return list;
    }

    /**
     * This syncrhonizes tDAR records and DataOne records so that DataONE can see all of the various versions of tDAR records
     */
    @Transactional(readOnly = false)
    public void synchronizeTdarChangesWithDataOneObjects() {
        logger.trace("starting sync...");
        List<ListObjectEntry> resources = dataOneDao.unify(this);
        logger.debug("{}", resources);
        logger.trace("sync complete");
    }

    /**
     * Takes an ID and gets the tDAR record and D1 record; if the checksums are different, manually update the dateUpdated and the D1 Object chain by creating a
     * new DataOneObject.
     * 
     * @param id
     */
    @Transactional(readOnly = false)
    public void checkForChecksumConflict(String id) {
        ObjectResponseContainer object = getObjectFromTdar(id, false);
        logger.trace("{}", object);
        DataOneObject dataOneObject = dataOneDao.findByIdentifier(id);
        boolean redo = false;
        if (object != null && object.getTdarResource() != null) {
            genericService.markReadOnly(object.getTdarResource());
        }

        if (dataOneObject == null) {
            return;
        }

        // if the dataone object and the tdar object differ in checksum... redo
        if (dataOneObject != null && object != null &&
                !StringUtils.equals(object.getChecksum(), dataOneObject.getChecksum()) &&
                dataOneObject.getObsoletedBy() == null) {
            logger.debug("checksums differ? {} {} {}", object.getChecksum(), dataOneObject.getChecksum(), dataOneObject.getObsoletedBy());
            redo = true;
        }

        // if the date in the object Id is different such that we can't get the exact record, then we need to obsolete it
        if (object == null && dataOneObject.getObsoletedBy() == null) {
            redo = true;
            object = getObjectFromTdar(id, true);
            logger.debug("object was null for exact date");
            logger.debug("checksums differ? {} {} {}", object.getChecksum(), dataOneObject.getChecksum(), dataOneObject.getObsoletedBy());
        }

        logger.debug("{} {}", object, dataOneObject);
        if (object == null) {
            logger.debug("object still null... returning");
            return;
        }
        String externalId = object.getTdarResource().getExternalId();
        Long tdarId = object.getTdarResource().getId();
        String checksum = object.getChecksum();
        object = null;

        // if we have both, the checksums differ, and we're not archived/obsoleted
        if (redo) {
            genericService.clearCurrentSession();
            Date dateUpdated = new Date();
            dataOneDao.updateModifedDate(tdarId, dateUpdated);
            logger.warn("checksum varied between D1 object and tDAR object: {} {} {} {}", tdarId, checksum,
                    dataOneObject.getChecksum(), dataOneObject.getIdentifier());
            dataOneDao.unifyEntry(this, externalId, tdarId, DataOneUtils.toUtc(dateUpdated));
            genericService.refresh(dataOneObject);
        }
    }

    /**
     * Gets DataOne System metadata for a given id. The ID will be a tDAR DOI with a suffix that specifies a metadata object, a resource, or a file
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.getSystemMetadata
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public SystemMetadata metadataRequest(String id) {
        SystemMetadata metadata = new SystemMetadata();
        AccessPolicy policy = new AccessPolicy();

        policy.getAllowList().add(DataOneUtils.createAccessRule(Permission.READ, PUBLIC));
        metadata.setAccessPolicy(policy);
        metadata.setAuthoritativeMemberNode(getTdarNodeReference());
        metadata.setOriginMemberNode(getTdarNodeReference());
        // metadata.setReplicationPolicy(rpolicy );

        // rights to change the permissions sitting on the object
        metadata.setRightsHolder(getRightsHolder());
        // metadata.setSerialVersion(value);

        // look up in log table what the last exposed version of metadata was

        logger.debug("{}", id);
        ObjectResponseContainer object = getObjectFromTdar(id, false);
        logger.trace("{}", object);
        DataOneObject dataOneObject = dataOneDao.findByIdentifier(id);
        logger.trace("{}", dataOneObject);
        if (object == null && dataOneObject == null) {
            logger.debug("not found -- returning");
            return null;
        }

        IdentifierParser parser = new IdentifierParser(id, informationResourceDao);

        // if we don't have an object in tDAR that's an exact match -- it's likely a request for an old version...
        if (object == null) {
            InformationResource resource = informationResourceDao.find(dataOneObject.getTdarId());
            metadata.setSeriesId(DataOneUtils.createIdentifier(DataOneUtils.createSeriesId(resource.getId(), parser.getType())));
            metadata.setDateUploaded(DataOneUtils.toUtc(dataOneObject.getDateUploaded()).toDate());
            markArchived(metadata, true, resource);
            String obsoletedBy = dataOneObject.getObsoletedBy();
            String obsoletes = dataOneObject.getObsoletes();
            updateObsoletesObsoletedBy(metadata, obsoletedBy, obsoletes);
            metadata.setChecksum(DataOneUtils.createChecksum(dataOneObject.getChecksum()));
            metadata.setSize(BigInteger.valueOf(dataOneObject.getSize()));
            ObjectFormatIdentifier format = new ObjectFormatIdentifier();
            format.setValue(dataOneObject.getFormatId());
            metadata.setFormatId(format);
            metadata.setIdentifier(DataOneUtils.createIdentifier(dataOneObject.getIdentifier()));
            metadata.setDateSysMetadataModified(dataOneObject.getSysMetadataModified());

            // rights to change the permissions sitting on the object
            metadata.setSubmitter(DataOneUtils.createSubject(dataOneObject.getSubmitter()));
            logger.debug("returning: {}", metadata);
            return metadata;
        }
        InformationResource resource = object.getTdarResource();

        // if it's deleted, we mark it as archived
        markArchived(metadata, false, resource);
        // used to detect when changes happen in DataONE
        metadata.setDateSysMetadataModified(resource.getDateUpdated());

        // if (object.getType() == EntryType.TDAR) {
        String currentIdentifier = IdentifierParser.formatIdentifier(resource.getExternalId(), resource.getDateUpdated(), parser.getType(), null);
        metadata.setSeriesId(DataOneUtils.createIdentifier(DataOneUtils.createSeriesId(resource.getId(), parser.getType())));

        if (dataOneObject != null) {
            updateObsoletesObsoletedBy(metadata, dataOneObject.getObsoletedBy(), dataOneObject.getObsoletes());
        }

        metadata.setChecksum(DataOneUtils.createChecksum(object.getChecksum()));
        metadata.setFormatId(DataOneUtils.contentTypeToD1Format(object.getType(), object.getContentType()));
        metadata.setSize(BigInteger.valueOf(object.getSize()));

        if (parser.isSeriesIdentifier()) {
            metadata.setIdentifier(DataOneUtils.createIdentifier(currentIdentifier));
        } else {
            metadata.setIdentifier(DataOneUtils.createIdentifier(id));
        }

        metadata.setDateUploaded(DataOneUtils.toUtc(resource.getDateUpdated()).toDate());

        metadata.setSubmitter(DataOneUtils.createSubject(resource.getSubmitter().getProperName()));
        logger.trace("rights: {} ; submitter: {} ", metadata.getRightsHolder(), metadata.getSubmitter());
        return metadata;
    }

    private void updateObsoletesObsoletedBy(SystemMetadata metadata, String obsoletedBy, String obsoletes) {
        if (StringUtils.isNotBlank(obsoletedBy)) {
            metadata.setObsoletedBy(DataOneUtils.createIdentifier(obsoletedBy));
        }
        if (StringUtils.isNotBlank(obsoletes)) {
            metadata.setObsoletes(DataOneUtils.createIdentifier(obsoletes));
        }
    }

    private void markArchived(SystemMetadata metadata, boolean dateIgnored, InformationResource resource) {
        if (resource.getStatus() != Status.ACTIVE || dateIgnored) {
            metadata.setArchived(true);
        } else {
            metadata.setArchived(false);
        }
    }

    /**
     * generate the LDAP-style rights entry for the rights holder (likely change from tDAR's sysadmin)
     * 
     * @return
     */
    private Subject getRightsHolder() {
        return DataOneUtils.createSubject(String.format("CN=%s,O=TDAR,DC=org", CONFIG.getSystemAdminEmail()));
    }

    /**
     * generate the LDAP-style rights entry for the sysadmin
     * 
     * @return
     */
    private Subject getSystemUserLdap() {
        return DataOneUtils.createSubject(String.format("CN=%s,O=TDAR,DC=org", CONFIG.getSystemAdminEmail()));
    }

    /**
     * Get an object from tDAR based on the ID (Object, ObjectList, and Metadata responses)
     * 
     * @param id
     * @param request
     * @param event
     * @return
     */
    @Transactional(readOnly = false)
    public ObjectResponseContainer getObject(final String id, HttpServletRequest request, Event event) {
        ObjectResponseContainer resp = getObjectFromTdar(id, false);
        if (request != null && resp != null && event != null) {
            LogEntryImpl entry = new LogEntryImpl(id, request, event);
            genericService.markWritable(entry);
            genericService.save(entry);
        }
        return resp;
    }

    /**
     * For a given DataOne Identifier (tDAR DOI + additional suffix) get the entry from tDAR
     * 
     * @param id_
     * @return
     */
    @Transactional(readOnly = true)
    private ObjectResponseContainer getObjectFromTdar(String id_, boolean ignoreDate_) {
        ObjectResponseContainer resp = null;
        boolean ignoreDate = ignoreDate_;
        try {
            IdentifierParser parser = new IdentifierParser(id_, informationResourceDao);
            if (parser.getModified() != null && parser.getIr().getDateUpdated().compareTo(parser.getModified()) == 0) {
                ignoreDate = true;
            }
            logger.trace("ignoreDate:{}", ignoreDate);
            if (parser.getType() == EntryType.D1 && (parser.isSeriesIdentifier() || ignoreDate)) {
                resp = constructD1FormatObject(parser.getIr());
            }
            if (parser.getType() == EntryType.TDAR && (parser.isSeriesIdentifier() || ignoreDate)) {
                logger.trace("{} vs. {}", parser.getIr().getDateUpdated(), parser.getModified());
                resp = constructMetadataFormatObject(parser.getIr());
            }
            if (parser.getType() == EntryType.FILE) {
                // NOT FULLY IMPLEMENTED
                resp = constructFileFormatObject(parser.getPartIdentifier(), parser.getIr());
            }
            if (resp != null) {
                resp.setTdarResource(parser.getIr());
                resp.setIdentifier(id_);
            }
        } catch (NoResultException nsr) {
            logger.error("entity not found for id: {}", id_);
        } catch (Exception e) {
            logger.error("error in DataOneObjectRequest:" + id_, e);
        }

        return resp;
    }

    /**
     * Create an ObjectResponseContainer from a resource and the identifier for a file
     * 
     * @param partIdentifier
     * @param ir
     * @return
     */
    private ObjectResponseContainer constructFileFormatObject(String partIdentifier, InformationResource ir) {
        ObjectResponseContainer resp = setupResponse(ir);
        resp.setType(EntryType.FILE);
        Long irfid = Long.parseLong(StringUtils.substringBefore(partIdentifier, D1_VERS_SEP));
        Integer versionNumber = Integer.parseInt(StringUtils.substringAfter(partIdentifier, D1_VERS_SEP));

        for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
            if (irf.getId().equals(irfid)) {
                InformationResourceFileVersion version = irf.getUploadedVersion(versionNumber);
                resp.setContentType(version.getMimeType());
                resp.setSize(version.getFileLength().intValue());
                resp.setChecksum(version.getChecksum());
                break;
            }
        }
        return resp;
    }

    /**
     * Create an ObjectResponseContainer for a metadata request
     * 
     * @param ir
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public ObjectResponseContainer constructMetadataFormatObject(InformationResource ir)
            throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException {
        logger.debug("construct metadata: {}", ir);
        ObjectResponseContainer resp = setupResponse(ir);
        resp.setContentType(XML_CONTENT_TYPE);
        resp.setType(EntryType.TDAR);
        // ModsDocument modsDoc = ModsTransformer.transformAny(ir);
        QualifiedDublinCoreDocument modsDoc = ExtendedDcTransformer.transformAny(ir);
        resp.setObjectFormat(META);
        StringWriter sw = new StringWriter();
        JaxbDocumentWriter.write(modsDoc, sw, false);
        String metaXml = sw.toString();
        logger.trace(metaXml);
        resp.setSize(metaXml.getBytes(UTF_8).length);
        resp.setReader(new StringReader(metaXml));
        resp.setChecksum(DataOneUtils.checksumString(metaXml));
        return resp;
    }

    /**
     * Create an ObjectResponseContainer for a DataOne ResourceMap
     * 
     * @param ir
     * @return
     * @throws OREException
     * @throws URISyntaxException
     * @throws ORESerialiserException
     * @throws JDOMException
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public ObjectResponseContainer constructD1FormatObject(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException,
            JDOMException, IOException, UnsupportedEncodingException, NoSuchAlgorithmException {
        ObjectResponseContainer resp = setupResponse(ir);
        resp.setType(EntryType.D1);
        resp.setContentType(RDF_CONTENT_TYPE);
        String map = createResourceMap(ir);
        resp.setObjectFormat(D1_FORMAT);
        resp.setSize(map.getBytes(UTF_8).length);
        resp.setReader(new StringReader(map));
        resp.setChecksum(DataOneUtils.checksumString(map));
        return resp;
    }

    /**
     * setup a ObjectResponseContainer from a resource
     * 
     * @param ir
     * @return
     */
    private ObjectResponseContainer setupResponse(InformationResource ir) {
        obfuscationService.obfuscate(ir, null);
        ObjectResponseContainer resp = new ObjectResponseContainer();

        resp.setTdarResource(ir);
        return resp;
    }

    /**
     * Replicate request - not really sure how it's used for D1
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.getReplica
     * 
     * @param pid
     * @param request
     */
    @Transactional(readOnly = false)
    public void replicate(String pid, HttpServletRequest request) {
        LogEntryImpl entry = new LogEntryImpl(pid, request, Event.REPLICATE);
        genericService.markWritable(entry);
        genericService.save(entry);

    }

}
