package org.tdar.dataone.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.dataone.ore.ResourceMapFactory;
import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.Log;
import org.dataone.service.types.v1.LogEntry;
import org.dataone.service.types.v1.Node;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.NodeState;
import org.dataone.service.types.v1.NodeType;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.ObjectInfo;
import org.dataone.service.types.v1.ObjectList;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Ping;
import org.dataone.service.types.v1.Schedule;
import org.dataone.service.types.v1.Services;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.Synchronization;
import org.dataone.service.types.v1.SystemMetadata;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.dspace.foresite.ResourceMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.ListObjectEntry.Type;
import org.tdar.dataone.bean.LogEntryImpl;
import org.tdar.dataone.dao.DataOneDao;
import org.tdar.transform.ModsTransformer;
import org.tdar.utils.PersistableUtils;

import edu.asu.lib.jaxb.JaxbDocumentWriter;
import edu.asu.lib.mods.ModsDocument;

@Service
public class DataOneService {

    private static final String UTF_8 = "UTF-8";
    static final String META = "meta";
    static final String D1_VERS_SEP = "&v=";
    static final String D1_SEP = "_";
    static final String D1_FORMAT = "format=d1rem";
    static final String MN_REPLICATION = "MNREplication";
    static final String MN_STORAGE = "MNStorage";
    static final String MN_AUTHORIZATION = "MNAuthorization";
    static final String MN_READ = "MNRead";
    static final String MN_CORE = "MNCore";
    static final String VERSION = "v1";
    static final String DATAONE = "/dataone/";

    static final String RDF_CONTENT_TYPE = "application/rdf+xml; charset=UTF-8";
    static final String XML_CONTENT_TYPE = "application/xml; charset=UTF-8";

    static final String MN_NAME = "urn:node:tdar";
    static final String MN_NAME_TEST = "urn:node:tdar_test";

    // this is for Tier 3 support
    private boolean includeFiles = false;

    static final String PUBLIC = "public";

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Autowired
    private GenericService genericService;

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private DataOneDao dataOneDao;

    @Autowired
    private ObfuscationService obfuscationService;

    @Autowired
    private InformationResourceService informationResourceService;

    public String createResourceMap(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException {
        Identifier id = new Identifier();
        id.setValue(ir.getExternalId().replace("/", ":") + D1_SEP + D1_FORMAT + ir.getDateUpdated().toString());

        Identifier packageId = new Identifier();
        packageId.setValue(ir.getExternalId() + D1_SEP + ir.getDateUpdated().toString());
        List<Identifier> dataIds = new ArrayList<>();
        if (includeFiles) {
            for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
                Identifier fileId = new Identifier();
                fileId.setValue(ir.getExternalId() + D1_SEP + irf.getId() + D1_VERS_SEP + irf.getLatestVersion());
                dataIds.add(fileId);
            }
        }
        Map<Identifier, List<Identifier>> idMap = new HashMap<Identifier, List<Identifier>>();
        idMap.put(packageId, dataIds);
        // generate the resource map
        ResourceMapFactory rmf = ResourceMapFactory.getInstance();
        ResourceMap resourceMap = rmf.createResourceMap(id, idMap);
        Date itemModDate = ir.getDateUpdated();
        resourceMap.setModified(itemModDate);
        String rdfXml = ResourceMapFactory.getInstance().serializeResourceMap(resourceMap);
        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(new StringReader(rdfXml));
        Iterator<Element> it = d.getRootElement().getChildren().iterator();
        List<Element> children = new ArrayList<Element>();
        while (it.hasNext()) {
            Element element = (Element) it.next();
            children.add(element);
        }
        d.getRootElement().removeContent();
        Collections.sort(children, new Comparator<Element>() {
            @Override
            public int compare(Element t, Element t1) {
                return t.getAttributes().toString().compareTo(t1.getAttributes().toString());
            }
        });
        for (Element el : children) {
            d.getRootElement().addContent(el);
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        rdfXml = outputter.outputString(d);
        logger.debug(rdfXml);
        return rdfXml;
    }

    public Node getNodeResponse() {
        org.dataone.service.types.v1.Node node = new Node();
        node.setBaseURL(CONFIG.getBaseSecureUrl() + DATAONE);
        node.setDescription(CONFIG.getSystemDescription());
        node.setIdentifier(getTdarNodeReference());
        node.setName(CONFIG.getRepositoryName());

        // v2?
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

        node.setServices(services);
        node.setState(NodeState.UP);
        Synchronization sync = new Synchronization();
        sync.setLastCompleteHarvest(new Date(0));
        sync.setLastHarvested(new Date());
        Schedule schedule = new Schedule();
        schedule.setHour("2");
        schedule.setMday("*");
        schedule.setMin("*");
        schedule.setMon("*");
        schedule.setSec("*");
        schedule.setWday("*");
        schedule.setYear("*");
        schedule.setWday("6");
        sync.setSchedule(schedule);
        node.setSynchronization(sync);
        // node.setSynchronize();
        node.setType(NodeType.MN);
        return node;
    }

    private NodeReference getTdarNodeReference() {
        NodeReference nodeReference = new NodeReference();
        nodeReference.setValue(getMemberNodeName());
        return nodeReference;
    }

    private void addService(String name, String version, Boolean available, Services services) {
        org.dataone.service.types.v1.Service service = new org.dataone.service.types.v1.Service();
        service.setName(name);
        service.setVersion(version);
        service.setAvailable(available);
        services.getServiceList().add(service);
    }

    public Log getLogResponse(Date fromDate, Date toDate, Event event, String idFilter, int start, int count, HttpServletRequest request) {
        Log log = new Log();
        // LogEntryImpl from LogEntryImpl where logDate is between :fromDate and :toDate and (useEventType=true and eventType=:eventType) and (useIdFilter=true
        // and idFilter startsWith(idFilter)
        // log.setCount...

        dataOneDao.findLogFiles(fromDate, toDate, event, idFilter, start, count, log);
        List<LogEntry> logEntries = log.getLogEntryList();
        for (LogEntryImpl impl : new ArrayList<LogEntryImpl>()) {
            LogEntry entry = new LogEntry();
            entry.setDateLogged(impl.getDateLogged());
            entry.setEntryId(impl.getId().toString());
            entry.setEvent(impl.getEvent());
            Identifier identifier = new Identifier();
            identifier.setValue(impl.getIdentifier());
            entry.setIdentifier(identifier);
            entry.setNodeIdentifier(entry.getNodeIdentifier());
            entry.setSubject(createSubject(impl.getSubject()));
            entry.setIpAddress(impl.getIpAddress());
            entry.setUserAgent(impl.getUserAgent());
            logEntries.add(entry);
        }
        return log;
    }

    public Checksum getChecksumResponse(String pid, String checksum_) {
        ObjectResponseContainer object = getObject(pid, null, false);
        return createChecksum(object.getChecksum());
    }

    public ObjectList getListObjectsResponse(Date fromDate, Date toDate, String formatid, String identifier, int start, int count) {
        ObjectList list = new ObjectList();
        list.setCount(count);
        list.setStart(start);

        // FIXME: CONVERT IDENTIFIER to TDAR QUERY
        // FIXME: CONVERT FORMAT to TDAR FORMAT
        ListObjectEntry.Type type = Type.D1;
        List<ListObjectEntry> resources = dataOneDao.findUpdatedResourcesWithDOIs(fromDate, toDate, type, list);
        for (ListObjectEntry resource : resources) {
            ObjectInfo info = new ObjectInfo();
            info.setChecksum(createChecksum(resource.getChecksum()));
            info.setDateSysMetadataModified(resource.getDateUpdated());
            info.setFormatId(contentTypeToD1Format(resource, resource.getContentType()));
            info.setIdentifier(createIdentifier(resource.getFormattedIdentifier()));
            info.setSize(BigInteger.valueOf(resource.getSize()));
            list.getObjectInfoList().add(info);
        }
        return list;
    }

    private Identifier createIdentifier(String formattedIdentifier) {
        Identifier id = new Identifier();
        id.setValue(formattedIdentifier);
        return id;
    }

    private Checksum createChecksum(String checksum) {
        Checksum cs = new Checksum();
        cs.setAlgorithm("MD5");
        cs.setValue(checksum);
        return cs;
    }

    public void synchronizationFailed(String id, long serialVersion, Date dateSysMetaLastModified, HttpServletRequest request) {
        LogEntryImpl entry = new LogEntryImpl(id, request, Event.REPLICATION_FAILED);
        genericService.save(entry);

    }

    /**
     * Gets DataOne System metadata for a given id. The ID will be a tDAR DOI with a suffix that specifies a metadata object, a resource, or a file
     * 
     * @param id
     * @return
     */
    public SystemMetadata metadataRequest(String id) {
        SystemMetadata metadata = new SystemMetadata();
        AccessPolicy policy = new AccessPolicy();

        ObjectResponseContainer object = getObjectFromTdar(id);
        InformationResource resource = object.getTdarResource();
        policy.getAllowList().add(createAccessRule(Permission.READ, PUBLIC));
        metadata.setAccessPolicy(policy);
        metadata.setAuthoritativeMemberNode(getTdarNodeReference());
        metadata.setDateSysMetadataModified(resource.getDateUpdated());
        metadata.setDateUploaded(resource.getDateCreated());
        if (resource.getStatus() != Status.ACTIVE) {
            metadata.setArchived(true);
        } else {
            metadata.setArchived(false);
        }
        metadata.setChecksum(createChecksum(object.getChecksum()));
        metadata.setFormatId(contentTypeToD1Format(object, object.getContentType()));
        metadata.setSize(BigInteger.valueOf(object.getSize()));
        metadata.setIdentifier(createIdentifier(object.getIdentifier()));
        // metadata.setObsoletedBy(value);
        // metadata.setObsoletes(value);

        metadata.setOriginMemberNode(getTdarNodeReference());
        // metadata.setReplicationPolicy(rpolicy );

        // rights to change the permissions sitting on the object
        metadata.setRightsHolder(createSubject(CONFIG.getSystemAdminEmail()));
        // metadata.setSerialVersion(value);
        metadata.setSubmitter(createSubject(resource.getSubmitter().getProperName()));
        logger.debug("rights: {} ; submitter: {} ", metadata.getRightsHolder(), metadata.getSubmitter());
        return metadata;
    }

    private ObjectFormatIdentifier contentTypeToD1Format(ObjectResponseContainer object, String contentType) {
        ObjectFormatIdentifier identifier = new ObjectFormatIdentifier();
        identifier.setValue("BAD-FORMAT");
        return identifier;
    }

    private ObjectFormatIdentifier contentTypeToD1Format(ListObjectEntry resource, String objectFormat) {
        ObjectFormatIdentifier identifier = new ObjectFormatIdentifier();
        identifier.setValue("BAD-FORMAT");
        return identifier;
    }

    private AccessRule createAccessRule(Permission permission, String name) {
        AccessRule rule = new AccessRule();
        rule.getPermissionList().add(permission);
        if (StringUtils.isNotBlank(name)) {
            rule.getSubjectList().add(createSubject(name));
        }
        return rule;
    }

    private Subject createSubject(String name) {
        Subject subject = new Subject();
        subject.setValue(name);
        return subject;
    }

    public ObjectResponseContainer getObject(final String id, HttpServletRequest request, boolean log) {
        ObjectResponseContainer resp = getObjectFromTdar(id);
        if (request != null && resp != null && !log) {
            LogEntryImpl entry = new LogEntryImpl(id, request, Event.READ);
            genericService.save(entry);
        } else {
            logger.debug("req: {} , resp: {}, id {}", request, resp, id);
        }
        logger.debug("response: {}", resp);
        return resp;
    }

    /**
     * For a given DataOne Identifier (tDAR DOI + additional suffix)
     * 
     * @param id_
     * @return
     */
    public ObjectResponseContainer getObjectFromTdar(String id_) {
        ObjectResponseContainer resp = null;
        try {
            logger.debug("looking for Id: {}", id_);
            String doi = StringUtils.substringBefore(id_, D1_SEP);
            doi = doi.replace("doi:10.6067:", "doi:10.6067/");
            String partIdentifier = StringUtils.substringAfter(id_, D1_SEP);
            InformationResource ir = informationResourceService.findByDoi(doi);
            obfuscationService.obfuscate(ir, null);
            resp = new ObjectResponseContainer();
            if (PersistableUtils.isNullOrTransient(ir)) {
                logger.debug("resource not foound: {}", doi);
                return null;
            }
            resp.setTdarResource(ir);
            resp.setIdentifier(id_);
            if (partIdentifier.equals(D1_FORMAT) || partIdentifier == null) {
                resp.setContentType(RDF_CONTENT_TYPE);
                String map = createResourceMap(ir);
                resp.setSize(map.getBytes(UTF_8).length);
                resp.setReader(new StringReader(map));
                resp.setChecksum(checksumString(map));
            } else if (partIdentifier.equals(META)) {
                resp.setContentType(XML_CONTENT_TYPE);
                ModsDocument modsDoc = ModsTransformer.transformAny(ir);

                StringWriter sw = new StringWriter();
                JaxbDocumentWriter.write(modsDoc, sw, true);
                String metaXml = sw.toString();
                logger.debug(metaXml);
                resp.setSize(metaXml.getBytes(UTF_8).length);
                resp.setReader(new StringReader(metaXml));
                resp.setChecksum(checksumString(metaXml));
            } else if (partIdentifier.contains(D1_VERS_SEP)) {
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
            } else {
                resp = null;
                logger.error("bad format");
            }

        } catch (Exception e) {
            logger.error("error in DataOneObjectRequest", e);
        }
        return resp;
    }

    private String checksumString(String string) throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        messageDigest.update(string.getBytes(Charset.forName("UTF8")));
        final byte[] resultByte = messageDigest.digest();
        final String result = new String(Hex.encodeHex(resultByte));
        return result;
    }

    public void replicate(String pid, HttpServletRequest request) {
        LogEntryImpl entry = new LogEntryImpl(pid, request, Event.REPLICATE);
        genericService.save(entry);

    }

    public String getMemberNodeName() {
        return MN_NAME_TEST;
    }
}
