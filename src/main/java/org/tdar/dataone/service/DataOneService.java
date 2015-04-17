package org.tdar.dataone.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.dspace.foresite.Agent;
import org.dspace.foresite.AggregatedResource;
import org.dspace.foresite.Aggregation;
import org.dspace.foresite.OREException;
import org.dspace.foresite.OREFactory;
import org.dspace.foresite.ORESerialiser;
import org.dspace.foresite.ORESerialiserException;
import org.dspace.foresite.ORESerialiserFactory;
import org.dspace.foresite.Predicate;
import org.dspace.foresite.ResourceMap;
import org.dspace.foresite.ResourceMapDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.dataone.bean.AccessPolicy;
import org.tdar.dataone.bean.AccessRule;
import org.tdar.dataone.bean.Checksum;
import org.tdar.dataone.bean.Event;
import org.tdar.dataone.bean.Log;
import org.tdar.dataone.bean.LogEntry;
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

import com.sun.tools.doclets.internal.toolkit.resources.doclets;

@org.springframework.stereotype.Service
public class DataOneService {

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
    TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    public ResourceMapDocument createAggregationForResource(InformationResource resource) throws OREException, URISyntaxException, ORESerialiserException {
        String idref = "/doi/" + resource.getExternalId();
        Aggregation agg = OREFactory.createAggregation(new URI(CONFIG.getBaseSecureUrl() + idref + "/agg"));
        ResourceMap rem = agg.createResourceMap(new URI(CONFIG.getBaseSecureUrl() + idref + "/map"));

        AggregatedResource ar = agg.createAggregatedResource(new URI(CONFIG.getBaseSecureUrl() + idref + "/meta"));
        String literal = "scimeta_id";
        ar.addIdentifier(literal);
        for (InformationResourceFile irf : resource.getActiveInformationResourceFiles()) {
            agg.createAggregatedResource(new URI(CONFIG.getBaseSecureUrl() + idref + "/" + irf.getId() + "/" + irf.getLatestVersion()));
        }

        for (ResourceCreator rc : resource.getPrimaryCreators()) {
            Agent creator = OREFactory.createAgent();
            creator.addName(rc.getCreator().getName());
            rem.addCreator(creator);
            agg.addCreator(creator);
        }

        agg.addTitle(resource.getTitle());

        ORESerialiser serial = ORESerialiserFactory.getInstance("RDF/XML");
        ResourceMapDocument doc = serial.serialise(rem);
        return doc;
    }

    private void addIdentifier(AggregatedResource ar, Predicate pred, String literal) throws URISyntaxException, OREException {
        pred.setName("identifier");
        pred.setNamespace("http://purl.org/dc/terms");
        pred.setPrefix("dcterms");
        pred.setURI(new URI("http://purl.org/dc/terms/Idenitifier"));
        ar.createTriple(pred, literal);
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
        // log.setCount(value);
        // log.setStart(value);
        // log.setTotal(value);
        // Event.CREATE;
        // Event.DELETE;
        // Event.READ;
        // Event.REPLICATE;
        // Event.REPLICATION_FAILED;
        // Event.SYNCHRONIZATION_FAILED;
        // Event.UPDATE;
        List<LogEntry> logEntries = log.getLogEntry();
        for (Resource resource : new ArrayList<Resource>()) {
            LogEntry entry = new LogEntry();
            // entry.setDateLogged(value);
            // entry.setEntryId(value);
            // entry.setEvent(value);
            // entry.setIdentifier(value);
            entry.setIpAddress(request.getRemoteAddr());
            // entry.setNodeIdentifier(value);
            // entry.setSubject(value);
            entry.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
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
        List<ObjectInfo> objectInfoList = list.getObjectInfo();
        for (Resource resource : new ArrayList<Resource>()) {
            ObjectInfo info = new ObjectInfo();
            // info.setChecksum(value);
            // info.setDateSysMetadataModified(value);
            // info.setFormatId(value);
            // info.setIdentifier(value);
            // info.setSize(value);
        }
        return list;
    }

    public void synchronizationFailed(String id, long serialVersion, Date dateSysMetaLastModified) {
        // TODO Auto-generated method stub

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

    public ObjectResponseContainer getObject(String id) {
        // TODO Auto-generated method stub
        return null;
    }

}
