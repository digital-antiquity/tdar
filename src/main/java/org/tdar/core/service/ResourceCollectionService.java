/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.GenericDao;

/**
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class ResourceCollectionService {

    @Autowired
    GenericDao genericDao;

    @Transactional(readOnly = false)
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave) {

        // find the internal collection for this resource
        ResourceCollection internalCollection = null;
        for (ResourceCollection collection : resource.getResourceCollections()) {
            if (collection.getType() == CollectionType.INTERNAL) {
                internalCollection = collection;
                if (shouldSave) {
                    internalCollection = genericDao.merge(internalCollection);
                }
            }
        }

        if (CollectionUtils.isEmpty(authorizedUsers) && internalCollection == null)
            return;

        // if none, create one
        if (internalCollection == null) {
            internalCollection = new ResourceCollection();
            internalCollection.setType(CollectionType.INTERNAL);
            resource.getResourceCollections().add(internalCollection);

            if (shouldSave) {
                genericDao.saveOrUpdate(internalCollection);
            }
        }

        Set<AuthorizedUser> currentUsers = internalCollection.getAuthorizedUsers();

        Iterator<AuthorizedUser> currentIterator = currentUsers.iterator();
        while (currentIterator.hasNext()) {
            AuthorizedUser currentUser = currentIterator.next();
            if (!authorizedUsers.contains(currentUser)) {
                currentIterator.remove();
            }
        }

        // FIXME: probably need to lookup people and resolve them
        if (!CollectionUtils.isEmpty(authorizedUsers)) {
            Iterator<AuthorizedUser> incomingIterator = authorizedUsers.iterator();
            while (incomingIterator.hasNext()) {
                AuthorizedUser incomingUser = incomingIterator.next();
                if (shouldSave) {
                    incomingUser = genericDao.merge(incomingUser);
                }
                currentUsers.add(incomingUser);
            }
            currentUsers.addAll(authorizedUsers);
        }
    }

    public List<AuthorizedUser> getAuthorizedUsersForResource(Resource resource) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<AuthorizedUser>();

        for (ResourceCollection collection : resource.getResourceCollections()) {
            if (collection.getType() == CollectionType.INTERNAL) {
                authorizedUsers.addAll(collection.getAuthorizedUsers());
            }
        }

        return authorizedUsers;
    }
}
