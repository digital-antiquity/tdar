package org.tdar.dataone.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
import javax.xml.bind.JAXBException;

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
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.ListObjectEntry.Type;
import org.tdar.dataone.bean.LogEntryImpl;
import org.tdar.dataone.dao.DataOneDao;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;
import org.tdar.utils.PersistableUtils;

import edu.asu.lib.dc.DublinCoreDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;
import edu.asu.lib.mods.ModsDocument;

@Service
public class DataOneService {

    private static final String UTF_8 = "UTF-8";
    public static final String META = "meta";
    public static final String D1_VERS_SEP = "&v=";
    public static final String D1_SEP = "_";
    public static final String D1_FORMAT = "format=d1rem";
    public static final String D1_RESOURCE_MAP_FORMAT = "http://www.openarchives.org/ore/terms";
//    public static final String D1_MODS_FORMAT = "http://loc.gov/mods/v3";
    public static final String D1_DC_FORMAT = "http://ns.dataone.org/metadata/schema/onedcx/v1.0";
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
    private DataOneDao dataOneDao;

    @Autowired
    private ObfuscationService obfuscationService;

    @Autowired
    private InformationResourceService informationResourceService;

    @Transactional(readOnly=true)
    public String createResourceMap(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException {
        Identifier id = new Identifier();
        String formattedId = ListObjectEntry.webSafeDoi(ir.getExternalId());
        id.setValue(ListObjectEntry.formatIdentifier(formattedId, ir.getDateUpdated(), Type.D1, null));

        Identifier packageId = new Identifier();
        packageId.setValue(formattedId + D1_SEP + ir.getDateUpdated().toString());
        List<Identifier> dataIds = new ArrayList<>();
        if (includeFiles) {
            for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
                Identifier fileId = new Identifier();
                fileId.setValue(ListObjectEntry.formatIdentifier(formattedId, ir.getDateUpdated(), Type.FILE, irf));
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
        logger.trace(rdfXml);
        return rdfXml;
    }

    @Transactional(readOnly=true)
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
        List<Subject> list = new ArrayList<>();
        list.add(createSubject(CONFIG.getSystemAdminEmail()));
        node.setContactSubjectList(list);
        Synchronization sync = new Synchronization();
        sync.setLastCompleteHarvest(new Date(0));
        node.getContactSubjectList().add(getSystemUserLdap());
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

    @Transactional(readOnly=true)
    public Log getLogResponse(Date fromDate, Date toDate, Event event, String idFilter, int start, int count, HttpServletRequest request) {
        Log log = new Log();
        // LogEntryImpl from LogEntryImpl where logDate is between :fromDate and :toDate and (useEventType=true and eventType=:eventType) and (useIdFilter=true
        // and idFilter startsWith(idFilter)
        // log.setCount...

        log.setStart(start);
        log.setCount(count);
        logger.debug("logResponse: {} {} {} {} {} {} {}", fromDate, toDate, event, idFilter, start, count);
        List<LogEntryImpl> findLogFiles = dataOneDao.findLogFiles(fromDate, toDate, event, idFilter, start, count, log);
        for (LogEntryImpl impl : findLogFiles) {
            LogEntry entry = new LogEntry();
            entry.setDateLogged(impl.getDateLogged());
            entry.setEntryId(impl.getId().toString());
            entry.setEvent(impl.getEvent());
            Identifier identifier = new Identifier();
            identifier.setValue(impl.getIdentifier());
            entry.setIdentifier(identifier);
            NodeReference nodeRef = new NodeReference();
            if (impl.getNodeReference() == null) {
                nodeRef.setValue("");
            } else {
                nodeRef.setValue(impl.getNodeReference());
            }
            entry.setNodeIdentifier(nodeRef);
            entry.setIpAddress(impl.getIpAddress());
            entry.setUserAgent(impl.getUserAgent());
            entry.setSubject(createSubject(impl.getSubject()));
            log.addLogEntry(entry);
        }
        log.setCount(log.getLogEntryList().size());
        return log;
    }

    @Transactional(readOnly = true)
    public Checksum getChecksumResponse(String pid, String checksum_) {
        ObjectResponseContainer object = getObject(pid, null, null);
        if (object == null) {
            return null;
        }
        return createChecksum(object.getChecksum());
    }

    @Transactional(readOnly = true)
    public ObjectList getListObjectsResponse(Date fromDate, Date toDate, String formatid, String identifier, int start, int count) throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException, JAXBException {
        ObjectList list = new ObjectList();
        list.setCount(count);
        list.setStart(start);

        List<ListObjectEntry> resources = dataOneDao.findUpdatedResourcesWithDOIs(fromDate, toDate, formatid, identifier, list);
        for (ListObjectEntry entry : resources) {
            ObjectInfo info = new ObjectInfo();
            ObjectResponseContainer object = null;
            if (entry.getType() != Type.FILE) {
                InformationResource resource = genericService.find(InformationResource.class, entry.getPersistableId());
                if (entry.getType() == Type.D1) {
                    object = constructD1FormatObject(resource);
                }
                if (entry.getType() == Type.TDAR) {
                    object = constructMetadataFormatObject(resource);
                }
            }
            info.setChecksum(createChecksum(object.getChecksum()));
            info.setDateSysMetadataModified(entry.getDateUpdated());
            info.setFormatId(contentTypeToD1Format(entry.getType(), entry.getContentType()));
            info.setIdentifier(createIdentifier(entry.getFormattedIdentifier()));
            info.setSize(BigInteger.valueOf(object.getSize()));
            list.getObjectInfoList().add(info);
        }
        // matching count of list to match # of results per test
        list.setCount(list.getObjectInfoList().size());
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

    /**
     * Gets DataOne System metadata for a given id. The ID will be a tDAR DOI with a suffix that specifies a metadata object, a resource, or a file
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public SystemMetadata metadataRequest(String id) {
        SystemMetadata metadata = new SystemMetadata();
        AccessPolicy policy = new AccessPolicy();

        ObjectResponseContainer object = getObjectFromTdar(id);
        if (object == null) {
            return null;
        }
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
        metadata.setFormatId(contentTypeToD1Format(object.getType(), object.getContentType()));
        metadata.setSize(BigInteger.valueOf(object.getSize()));
        metadata.setIdentifier(createIdentifier(object.getIdentifier()));
        // metadata.setObsoletedBy(value);
        // metadata.setObsoletes(value);

        metadata.setOriginMemberNode(getTdarNodeReference());
        // metadata.setReplicationPolicy(rpolicy );

        // rights to change the permissions sitting on the object
        metadata.setRightsHolder(getRightsHolder());
        // metadata.setSerialVersion(value);
        metadata.setSubmitter(createSubject(resource.getSubmitter().getProperName()));
        logger.debug("rights: {} ; submitter: {} ", metadata.getRightsHolder(), metadata.getSubmitter());
        return metadata;
    }

    private Subject getRightsHolder() {
        return createSubject(String.format("CN=%s,O=TDAR,DC=org",CONFIG.getSystemAdminEmail()));
    }

    private Subject getSystemUserLdap() {
        return createSubject(String.format("CN=%s,O=TDAR,DC=org",CONFIG.getSystemAdminEmail()));
    }

    private ObjectFormatIdentifier contentTypeToD1Format(Type type, String contentType) {
        ObjectFormatIdentifier identifier = new ObjectFormatIdentifier();
        switch (type) {
            case D1:
                identifier.setValue(D1_RESOURCE_MAP_FORMAT);
                break;
//            case FILE:
//                break;
            case TDAR:
                identifier.setValue(D1_DC_FORMAT);
                break;
            default:
                identifier.setValue("BAD-FORMAT");
                break;
        }
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
     * For a given DataOne Identifier (tDAR DOI + additional suffix)
     * 
     * @param id_
     * @return
     */
    @Transactional(readOnly = true)
    public ObjectResponseContainer getObjectFromTdar(String id_) {
        ObjectResponseContainer resp = null;
        try {
            logger.debug("looking for Id: {}", id_);
            String doi = StringUtils.substringBefore(id_, D1_SEP);
            doi = doi.replace("doi:10.6067:", "doi:10.6067/");
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
                resp = null;
                logger.error("bad format");
            }
            resp.setIdentifier(id_);

        } catch (Exception e) {
            logger.error("error in DataOneObjectRequest", e);
        }
        return resp;
    }

    private ObjectResponseContainer constructFileFormatObject(String partIdentifier, InformationResource ir) {
        ObjectResponseContainer resp = setupResponse(ir);
        resp.setType(Type.FILE);
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

    private ObjectResponseContainer constructMetadataFormatObject(InformationResource ir) throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException {
        ObjectResponseContainer resp = setupResponse(ir);
        resp.setContentType(XML_CONTENT_TYPE);
        resp.setType(Type.TDAR);
//        ModsDocument modsDoc = ModsTransformer.transformAny(ir);
        DublinCoreDocument modsDoc = DcTransformer.transformAny(ir);
        resp.setObjectFormat(META);
        StringWriter sw = new StringWriter();
        JaxbDocumentWriter.write(modsDoc, sw, true);
        String metaXml = sw.toString();
        resp.setSize(metaXml.getBytes(UTF_8).length);
        resp.setReader(new StringReader(metaXml));
        resp.setChecksum(checksumString(metaXml));
        return resp;
    }

    private ObjectResponseContainer constructD1FormatObject(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException,
            JDOMException, IOException, UnsupportedEncodingException, NoSuchAlgorithmException {
        ObjectResponseContainer resp = setupResponse(ir);
        resp.setType(Type.D1);
        resp.setContentType(RDF_CONTENT_TYPE);
        String map = createResourceMap(ir);
        resp.setObjectFormat(D1_FORMAT);
        resp.setSize(map.getBytes(UTF_8).length);
        resp.setReader(new StringReader(map));
        resp.setChecksum(checksumString(map));
        return resp;
    }

    private ObjectResponseContainer setupResponse(InformationResource ir) {
        obfuscationService.obfuscate(ir, null);
        ObjectResponseContainer resp = new ObjectResponseContainer();

        resp.setTdarResource(ir);
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

    @Transactional(readOnly = false)
    public void replicate(String pid, HttpServletRequest request) {
        LogEntryImpl entry = new LogEntryImpl(pid, request, Event.REPLICATE);
        genericService.markWritable(entry);
        genericService.save(entry);

    }

    public String getMemberNodeName() {
        return MN_NAME_TEST;
    }
}
