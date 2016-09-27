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

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Log;
import org.dataone.service.types.v2.Node;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.NodeReplicationPolicy;
import org.dataone.service.types.v1.NodeState;
import org.dataone.service.types.v1.NodeType;
import org.dataone.service.types.v1.ObjectInfo;
import org.dataone.service.types.v1.ObjectList;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Ping;
import org.dataone.service.types.v1.Schedule;
import org.dataone.service.types.v1.Service;
import org.dataone.service.types.v1.Services;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.Synchronization;
import org.dataone.service.types.v2.SystemMetadata;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.jdom2.JDOMException;
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
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.dataone.bean.EntryType;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.LogEntryImpl;
import org.tdar.dataone.dao.DataOneDao;
import org.tdar.transform.ExtendedDcTransformer;
import org.tdar.utils.PersistableUtils;

import edu.asu.lib.jaxb.JaxbDocumentWriter;
import edu.asu.lib.qdc.QualifiedDublinCoreDocument;

/**
 * The service backing DataOne controllers
 * 
 * @author abrin
 *
 */
@org.springframework.stereotype.Service
public class DataOneService implements DataOneConstants {

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
    private InformationResourceService informationResourceService;

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
        sync.setLastCompleteHarvest(new Date(0));
//        node.getContactSubjectList().add(getSystemUserLdap());
        sync.setLastHarvested(new Date());
        Schedule schedule = new Schedule();
        schedule.setHour("20");
        schedule.setMday("*");
        schedule.setMin("*");
        schedule.setMon("*");
        schedule.setSec("*");
        schedule.setWday("?");
        schedule.setYear("*");
//        schedule.setWday("6");
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
        ObjectResponseContainer object = getObject(pid, null, null);
        if (object == null) {
            return null;
        }
        return DataOneUtils.createChecksum(object.getChecksum());
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

        List<ListObjectEntry> resources = dataOneDao.findUpdatedResourcesWithDOIs(fromDate, toDate, formatid, identifier, list, count, start);
        logger.trace("{}", resources);
        // for each entry we find in the database, create a packaged response
        for (ListObjectEntry entry : resources) {
            ObjectInfo info = new ObjectInfo();
            ObjectResponseContainer object = null;

            // contstruct the metadata/response
            if (entry.getType() != EntryType.FILE) {
                InformationResource resource = genericService.find(InformationResource.class, entry.getPersistableId());
                if (resource == null || StringUtils.isBlank(resource.getExternalId())) {
                    continue;
                }
                if (entry.getType() == EntryType.D1) {
                    object = constructD1FormatObject(resource);
                }
                if (entry.getType() == EntryType.TDAR) {
                    object = constructMetadataFormatObject(resource);
                }
            }
            info.setDateSysMetadataModified(entry.getDateUpdated());
            info.setFormatId(DataOneUtils.contentTypeToD1Format(entry.getType(), entry.getContentType()));
            info.setIdentifier(DataOneUtils.createIdentifier(entry.getFormattedIdentifier()));
            if (object != null) {
                info.setChecksum(DataOneUtils.createChecksum(object.getChecksum()));
                info.setSize(BigInteger.valueOf(object.getSize()));
            }
            list.getObjectInfoList().add(info);
        }
        // matching count of list to match # of results per test
        list.setCount(list.getObjectInfoList().size());
        return list;
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
        org.dataone.service.types.v1.SystemMetadata metadata = new SystemMetadata();
        AccessPolicy policy = new AccessPolicy();

        ObjectResponseContainer object = getObjectFromTdar(id);
        if (object == null) {
            return null;
        }
        InformationResource resource = object.getTdarResource();
        policy.getAllowList().add(DataOneUtils.createAccessRule(Permission.READ, PUBLIC));
        metadata.setAccessPolicy(policy);
        metadata.setAuthoritativeMemberNode(getTdarNodeReference());
        metadata.setDateSysMetadataModified(resource.getDateUpdated());
        // look up in log table what the last exposed version of metadata was
        String oldId = dataOneDao.findLastExposedVersion(id);
        if (oldId != null) {
            metadata.setObsoletes(DataOneUtils.createIdentifier(oldId));
        }
        metadata.setDateUploaded(resource.getDateUpdated());

        // if it's deleted, we mark it as archived
        if (resource.getStatus() != Status.ACTIVE) {
            metadata.setArchived(true);
        } else {
            metadata.setArchived(false);
        }
        metadata.setChecksum(DataOneUtils.createChecksum(object.getChecksum()));
        metadata.setFormatId(DataOneUtils.contentTypeToD1Format(object.getType(), object.getContentType()));
        metadata.setSize(BigInteger.valueOf(object.getSize()));
        metadata.setSeriesId(DataOneUtils.createIdentifier(object.getTdarResource().getId().toString()));
        metadata.setIdentifier(DataOneUtils.createIdentifier(object.getIdentifier()));
        // metadata.setObsoletedBy(value);
        // metadata.setObsoletes(value);

        metadata.setOriginMemberNode(getTdarNodeReference());
        // metadata.setReplicationPolicy(rpolicy );

        // rights to change the permissions sitting on the object
        metadata.setRightsHolder(getRightsHolder());
        // metadata.setSerialVersion(value);
        metadata.setSubmitter(DataOneUtils.createSubject(resource.getSubmitter().getProperName()));
        logger.debug("rights: {} ; submitter: {} ", metadata.getRightsHolder(), metadata.getSubmitter());
        return metadata;
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
        ObjectResponseContainer resp = getObjectFromTdar(id);
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
    private ObjectResponseContainer getObjectFromTdar(String id_) {
        ObjectResponseContainer resp = null;
        try {
            logger.debug("looking for Id: {}", id_);
            String doi = StringUtils.substringBefore(id_, D1_SEP);
            // switching back DOIs to have / in them.
            doi = doi.replace(D1CONFIG.getDoiPrefix() + ":", D1CONFIG.getDoiPrefix() + "/");
            String partIdentifier = StringUtils.substringAfter(id_, D1_SEP);
            InformationResource ir = informationResourceService.findByDoi(doi);
            if (PersistableUtils.isNullOrTransient(ir)) {
                logger.debug("resource not foound: {}", doi);
                return null;
            }
            logger.debug("{} --> {} (id: {} {})", doi, ir.getId(), partIdentifier);
            if (partIdentifier.startsWith(D1_FORMAT) || partIdentifier == null) {
                resp = constructD1FormatObject(ir);
            } else if (partIdentifier.equals(META)) {
                resp = constructMetadataFormatObject(ir);
            } else if (partIdentifier.contains(D1_VERS_SEP)) {
                resp = constructFileFormatObject(partIdentifier, ir);
            } else {
                logger.warn("bad format for: {}", id_ );
                return null;
            }
            
            resp.setIdentifier(id_);
            resp.setTdarResource(ir);

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
    protected ObjectResponseContainer constructMetadataFormatObject(InformationResource ir)
            throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException {
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
    private ObjectResponseContainer constructD1FormatObject(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException,
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
