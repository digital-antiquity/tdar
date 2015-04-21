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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.dataone.ore.ResourceMapFactory;
import org.dataone.service.cn.v1.CNCore;
import org.dataone.service.types.v1.Identifier;
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
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.dataone.bean.AccessPolicy;
import org.tdar.dataone.bean.AccessRule;
import org.tdar.dataone.bean.Checksum;
import org.tdar.dataone.bean.Event;
import org.tdar.dataone.bean.ListObjectEntry;
import org.tdar.dataone.bean.Log;
import org.tdar.dataone.bean.LogEntry;
import org.tdar.dataone.bean.LogEntryImpl;
import org.tdar.dataone.bean.Node;
import org.tdar.dataone.bean.NodeReference;
import org.tdar.dataone.bean.NodeState;
import org.tdar.dataone.bean.NodeType;
import org.tdar.dataone.bean.ObjectInfo;
import org.tdar.dataone.bean.ObjectList;
import org.tdar.dataone.bean.Permission;
import org.tdar.dataone.bean.Ping;
import org.tdar.dataone.bean.Schedule;
import org.tdar.dataone.bean.Service;
import org.tdar.dataone.bean.Services;
import org.tdar.dataone.bean.Subject;
import org.tdar.dataone.bean.Synchronization;
import org.tdar.dataone.bean.SystemMetadata;
import org.tdar.dataone.dao.DataOneDao;
import org.tdar.transform.ModsTransformer;
import org.tdar.utils.PersistableUtils;

import com.google.inject.util.Types;

import edu.asu.lib.mods.ModsDocument;

@org.springframework.stereotype.Service
public class DataOneService {

    private static final String META = "meta";

    private static final String D1_VERS_SEP = "&v=";

    private static final String RDF_CONTENT_TYPE = "application/rdf+xml; charset=UTF-8";

    private static final String D1_SEP = "#";

    private static final String D1_FORMAT = "format=d1rem";

    private static final String PUBLIC = "public";

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final String MN_REPLICATION = "MNREplication";
    private static final String MN_STORAGE = "MNStorage";
    private static final String MN_AUTHORIZATION = "MNAuthorization";
    private static final String MN_READ = "MNRead";
    private static final String MN_CORE = "MNCore";
    private static final String VERSION = "v1";
    private static final String DATAONE = "/dataone/";

    private static final String XML_CONTENT_TYPE = "application/xml; charset=UTF-8";
    TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Autowired
    private GenericService genericService;

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
        for (InformationResourceFile irf : ir.getActiveInformationResourceFiles()) {
            Identifier fileId = new Identifier();
            fileId.setValue(ir.getExternalId() + D1_SEP + irf.getId() + D1_VERS_SEP + irf.getLatestVersion());
            dataIds.add(fileId);
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
        /*
         * 
         * // associate the metadata and data identifiers
         * 
         * // serialize it as RDF/XML
         * String rdfXml = ResourceMapFactory.getInstance().serializeResourceMap(resourceMap);
         * // Reorder the RDF/XML to a predictable order
         * SAXBuilder builder = new SAXBuilder();
         * try {
         * Document d = builder.build(new StringReader(rdfXml));
         * Iterator it = d.getRootElement().getChildren().iterator();
         * List<Element> children = new ArrayList<Element>();
         * while(it.hasNext()) {
         * Element element = (Element)it.next();
         * children.add(element);
         * }
         * d.getRootElement().removeContent();
         * Collections.sort(children, new Comparator<Element> () {
         * 
         * @Override
         * public int compare(Element t, Element t1) {
         * return t.getAttributes().toString().compareTo(t1.getAttributes().toString());
         * }
         * });
         * for(Element el : children) {
         * d.getRootElement().addContent(el);
         * }
         * XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
         * rdfXml = outputter.outputString(d);
         * } catch (JDOMException ex) {
         * log.error("Exception parsing rdfXml", ex);
         * }
         */
    }

    public Node getNodeResponse() {

        Node node = new Node();
        node.setBaseURL(CONFIG.getBaseSecureUrl() + DATAONE);
        node.setDescription(CONFIG.getSystemDescription());
        NodeReference nodeReference = getTdarNodeReference();
        node.setIdentifier(nodeReference);
        node.setName(CONFIG.getRepositoryName());

        // v2?
        // node.setNodeReplicationPolicy();
        Ping ping = new Ping();
        ping.setSuccess(Boolean.TRUE);
        node.setPing(ping);
        node.setReplicate(false);
        Services services = new Services();

        /*
         * <service name="MNRead" version="v1" available="true"/>
         * <service name="MNCore" version="v1" available="true"/>
         * <service name="MNAuthorization" version="v1" available="true"/>
         * <service name="MNStorage" version="v1" available="true"/>
         * <service name="MNReplication" version="v1" available="true"/>
         */

        addService(MN_READ, VERSION, Boolean.TRUE, services);
        addService(MN_CORE, VERSION, Boolean.TRUE, services);
        addService(MN_AUTHORIZATION, VERSION, Boolean.FALSE, services);
        addService(MN_STORAGE, VERSION, Boolean.FALSE, services);
        addService(MN_REPLICATION, VERSION, Boolean.FALSE, services);

        node.setServices(services);
        node.setState(NodeState.UP);
        Synchronization sync = new Synchronization();
        sync.setLastCompleteHarvest(dateToGregorianCalendar(new Date(0)));
        sync.setLastHarvested(dateToGregorianCalendar(new Date()));
        Schedule schedule = new Schedule();
        schedule.setHour("2");
        schedule.setWday("6");
        sync.setSchedule(schedule);
        node.setSynchronization(sync);
        // node.setSynchronize();
        node.setType(NodeType.MN);
        return node;
    }

    private NodeReference getTdarNodeReference() {
        NodeReference nodeReference = new NodeReference();
        nodeReference.setValue("urn:node:tdar");
        return nodeReference;
    }

    private void addService(String name, String version, Boolean available, Services services) {
        Service service = new Service();
        service.setName(name);
        service.setVersion(version);
        service.setAvailable(available);
        services.getService().add(service);
    }

    private XMLGregorianCalendar dateToGregorianCalendar(Date date) {
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(date);
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            return xmlDate;
        } catch (DatatypeConfigurationException dce) {
            return null;
        }
    }

    public Log getLogResponse(Date fromDate, Date toDate, Event event, String idFilter, int start, int count, HttpServletRequest request) {
        Log log = new Log();
        // LogEntryImpl from LogEntryImpl where logDate is between :fromDate and :toDate and (useEventType=true and eventType=:eventType) and (useIdFilter=true and idFilter startsWith(idFilter)
        // log.setCount...
        
        
        List<LogEntry> logEntries = log.getLogEntry();
        for (LogEntryImpl impl : new ArrayList<LogEntryImpl>()) {
            LogEntry entry = new LogEntry();
            entry.setDateLogged(dateToGregorianCalendar(impl.getDateLogged()));
            entry.setEntryId(impl.getId().toString());
            entry.setEvent(impl.getEvent());
            org.tdar.dataone.bean.Identifier identifier = new org.tdar.dataone.bean.Identifier();
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
        Checksum checksum = new Checksum();
        // checksum.setAlgorithm(value);
        // checksum.setValue(value);
        return checksum;
    }

    public ObjectList getListObjectsResponse(Date fromDate, Date toDate, String formatid, String identifier, int start, int count) {
        ObjectList list = new ObjectList();
        // list.setCount(value);
        // list.setStart(value);
        // list.setTotal(value);
        List<ListObjectEntry> resources = dataOneDao.findUpdatedResourcesWithDOIs(fromDate, toDate, start, count);
        for (ListObjectEntry resource : resources) {
            ObjectInfo info = new ObjectInfo();
            info.setChecksum(createChecksum(resource.getChecksum()));
            info.setDateSysMetadataModified(dateToGregorianCalendar(resource.getDateUpdated()));
//            info.setFormatId(org.dataone.);
            info.setIdentifier(createIdentifier(resource.getFormattedIdentifier()));
            info.setSize(BigInteger.valueOf(resource.getSize()));
            list.getObjectInfo().add(info);
        }
        return list;
    }

    private org.tdar.dataone.bean.Identifier createIdentifier(String formattedIdentifier) {
        org.tdar.dataone.bean.Identifier id = new org.tdar.dataone.bean.Identifier();
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

    public SystemMetadata metadataRequest(String id) {
        SystemMetadata metadata = new SystemMetadata();
        AccessPolicy policy = new AccessPolicy();
        Resource resource = new Resource();
        policy.getAllow().add(createAccessRule(Permission.READ, PUBLIC));
        metadata.setAccessPolicy(policy);
        metadata.setAuthoritativeMemberNode(getTdarNodeReference());
        metadata.setDateSysMetadataModified(dateToGregorianCalendar(resource.getDateUpdated()));
        metadata.setDateUploaded(dateToGregorianCalendar(resource.getDateCreated()));

        // metadata.setArchived(value);
        // metadata.setChecksum(value);
        // metadata.setFormatId(value);
        // metadata.setIdentifier(value);
        // metadata.setObsoletedBy(value);
        // metadata.setObsoletes(value);

        metadata.setOriginMemberNode(getTdarNodeReference());
        // metadata.setReplicationPolicy(rpolicy );

        // FIXME: who should this be?
        metadata.setRightsHolder(createSubject(CONFIG.getSystemAdminEmail()));
        // metadata.setSerialVersion(value);
        // metadata.setSize(value);

        // FIXME: should we be exposing emails here?
        metadata.setSubmitter(createSubject(resource.getSubmitter().getEmail()));
        return metadata;
    }

    private AccessRule createAccessRule(Permission permission, String name) {
        AccessRule rule = new AccessRule();
        rule.getPermission().add(permission);
        rule.getSubject().add(createSubject(name));
        return rule;
    }

    private Subject createSubject(String name) {
        Subject subject = new Subject();
        subject.setValue(name);
        return subject;
    }

    public ObjectResponseContainer getObject(final String id, HttpServletRequest request) {
        String id_ = id.replace("doi:10.6067:", "doi:10.6067/");
        ObjectResponseContainer resp = null;
        try {
            String doi = StringUtils.substringBefore(id_, D1_SEP);
            String partIdentifier = StringUtils.substringAfter(id_, D1_SEP);
            InformationResource ir = informationResourceService.findByDoi(doi);
            obfuscationService.obfuscate(ir, null);
            resp = new ObjectResponseContainer();
            if (PersistableUtils.isNullOrTransient(ir)) {
                return null;
            }

            resp.setIdentifier(id);
            if (partIdentifier.equals(D1_FORMAT)) {
                resp.setContentType(RDF_CONTENT_TYPE);
                String map = createResourceMap(ir);
                resp.setSize( map.getBytes("UTF-8").length);
                resp.setReader(new StringReader(map));
                resp.setChecksum(checksumString(map));
            } else if (partIdentifier.equals(META)) {
                resp.setContentType(XML_CONTENT_TYPE);
                ModsDocument modsDoc = ModsTransformer.transformAny(ir);
                JAXBContext jaxbContext = JAXBContext.newInstance(ModsDocument.class);
                StringWriter sw = new StringWriter();
                jaxbContext.createMarshaller().marshal(modsDoc, sw);
                String metaXml = sw.toString();
                resp.setSize( metaXml.getBytes("UTF-8").length);
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
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("error in DataOneObjectRequest", e);
        }
        if (request != null && resp != null) {
            LogEntryImpl entry = new LogEntryImpl(id, request, Event.READ);
            genericService.save(entry);
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

}
