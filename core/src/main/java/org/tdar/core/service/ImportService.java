package org.tdar.core.service;

import java.io.IOException;
import java.util.Collection;

import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.APIException;

public interface ImportService {

    String UNDERSCORE = "_";
    String _NEW_ID = "_NEW_ID_";
    String COPY = " (Copy)";

    /**
     * @see #bringObjectOntoSession(Resource, Person, Collection, Long)
     * 
     * @param incoming
     * @param authorizedUser
     * @return
     * @throws APIException
     * @throws Exception
     */
    <R extends Resource> R bringObjectOntoSession(R incoming, TdarUser authorizedUser, boolean validate) throws Exception;

    /**
     * Bring a @link Resource onto the session by checking that it exists, validating that the user has the rights to, avoiding collisions, copying the
     * immutable fields from the existing object, and reconcilling all of the child objects by looking them up in the database.
     * 
     * @param incoming_
     * @param authorizedUser
     * @param proxies
     * @param projectId
     * @return
     * @throws APIException
     * @throws IOException
     */
    <R extends Resource> R bringObjectOntoSession(R incoming_, TdarUser authorizedUser, Collection<FileProxy> proxies, Long projectId, boolean validate)
            throws APIException, IOException;

    <R extends InformationResource> R processFileProxies(R incoming_, Collection<FileProxy> proxies, TdarUser authorizedUser) throws APIException,
            IOException;

    /**
     * Find all of the @link Persistable children and look them up in the database, using the entries that have ids or equivalents in tDAR before using the ones
     * that are attached to the XML.
     * 
     * @param authorizedUser
     * @param incomingResource
     * @return
     * @throws APIException
     */
    <R extends Persistable> R reconcilePersistableChildBeans(TdarUser authorizedUser, R incomingResource) throws APIException;

    /**
     * Takes a record and round-trips it to XML to allow us to manipulate it and clone it with the session
     * 
     * @param resource
     * @param user
     * @return
     * @throws Exception
     */
    <R extends Resource> R cloneResource(R resource, TdarUser user) throws Exception;

    <R extends Resource> void resetOneToManyPersistableIds(R rec);

    /**
     * Takes a POJO property that's off the session and returns a managed instance of that property and handling
     * special casing and validation as needed.
     * 
     * @param property
     * @param resource
     * @param authenticatedUser
     * @return
     * @throws APIException
     */
    // <P extends Persistable, R extends Persistable> P processIncoming(P property, R resource, TdarUser authenticatedUser) throws APIException;

    ResourceCollection bringCollectionOntoSession(ResourceCollection importedRecord, TdarUser authenticatedUser, boolean validate) throws APIException;

}