package org.tdar.dataone.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Log;
import org.dataone.service.types.v1.ObjectList;
import org.dataone.service.types.v2.Node;
import org.dataone.service.types.v2.SystemMetadata;
import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.jdom2.JDOMException;
import org.tdar.core.bean.resource.InformationResource;

public interface DataOneService extends DataOneConstants {

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
    String createResourceMap(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException;

    /**
     * Formulates a NodeResponse
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNCore.getCapabilities
     * 
     * @return
     */
    Node getNodeResponse();

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
    Log getLogResponse(Date fromDate, Date toDate, Event event, String idFilter, int start, int count, HttpServletRequest request);

    /**
     * Generate a checksum response -- Data One uses checksums mostly the way tDAR does
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.getChecksum
     * 
     * @param pid
     * @param checksum_
     * @return
     */
    Checksum getChecksumResponse(String pid, String checksum_);

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
    ObjectList getListObjectsResponse(Date fromDate, Date toDate, String formatid, String identifier, int start, int count)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException,
            JAXBException;

    /**
     * This syncrhonizes tDAR records and DataOne records so that DataONE can see all of the various versions of tDAR records
     */
    void synchronizeTdarChangesWithDataOneObjects();

    /**
     * Takes an ID and gets the tDAR record and D1 record; if the checksums are different, manually update the dateUpdated and the D1 Object chain by creating a
     * new DataOneObject.
     * 
     * @param id
     */
    void checkForChecksumConflict(String id);

    /**
     * Gets DataOne System metadata for a given id. The ID will be a tDAR DOI with a suffix that specifies a metadata object, a resource, or a file
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.getSystemMetadata
     * 
     * @param id
     * @return
     */
    SystemMetadata metadataRequest(String id);

    /**
     * Get an object from tDAR based on the ID (Object, ObjectList, and Metadata responses)
     * 
     * @param id
     * @param request
     * @param event
     * @return
     */
    ObjectResponseContainer getObject(String id, HttpServletRequest request, Event event);

    /**
     * Create an ObjectResponseContainer for a metadata request
     * 
     * @param ir
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    ObjectResponseContainer constructMetadataFormatObject(InformationResource ir)
            throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException;

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
    ObjectResponseContainer constructD1FormatObject(InformationResource ir) throws OREException, URISyntaxException, ORESerialiserException,
            JDOMException, IOException, UnsupportedEncodingException, NoSuchAlgorithmException;

    /**
     * Replicate request - not really sure how it's used for D1
     * https://releases.dataone.org/online/api-documentation-v1.2.0/apis/MN_APIs.html#MNRead.getReplica
     * 
     * @param pid
     * @param request
     */
    void replicate(String pid, HttpServletRequest request);

}