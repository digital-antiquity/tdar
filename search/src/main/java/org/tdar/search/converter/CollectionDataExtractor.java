package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.PersistableUtils;

/**
 * Extracts data from a resource and parses the information into the various
 * fields. When used with the ResourceDocumentConverter, the field values are
 * associated with the fields from QueryFieldName and indexed.
 * 
 * pull out the logic to get the rights info for a resource into a single spot
 * for indexing.
 * 
 * @author abrin
 *
 */
public class CollectionDataExtractor {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	private Resource resource;

	/**
	 * Construct a new instance and parse out the collection hierarchy
	 * information.
	 * 
	 * @param resource
	 */
	public CollectionDataExtractor(Resource resource) {
		this.resource = resource;
		extractHierarchy();
	}

	//Managed collection data that the resource is a direct part of.  
	private HashSet<Long> directManagedCollectionIds = new HashSet<>();
	private Set<String> directManagedCollectionNames = new HashSet<>(); 

	// Contains ONLY ids of Unmanaged collections the resource is a direct part of.
	private HashSet<Long> directUnmanagedCollectionIds = new HashSet<>();
	private HashSet<String> directUnamangedCollectionNames = new HashSet<>();	
	
	//This contains ALL collection ids (managed/unmanaged, and parents). 
	private HashSet<Long> managedCollectionIds = new HashSet<>();
	
    private HashSet<Long> unmanagedCollectionIds = new HashSet<>(); 

	
	private HashSet<String> collectionNames = new HashSet<>();
	
	//This ends up being the same as collectionsId. 
	private HashSet<Long> allCollectionIds = new HashSet<>();


	/*
	 * this function should introduce into the index all of the people who can
	 * modify a record which is useful for limiting things on the project page
	 */
	public List<Long> getUsersWhoCanModify() {
		// The corresponding list of IDs for users who can make chagnes. 
		List<Long> users = new ArrayList<Long>();
		
		//This is a set of TdarUsers who have the ability to write changes. 
		HashSet<TdarUser> writable = new HashSet<>();
		writable.add(resource.getSubmitter());
		writable.add(resource.getUpdatedBy());
		for (ResourceCollection collection : resource.getManagedResourceCollections()) {
			if (!collection.isActive()) {
				continue;
			}
			
			//Find all users who can modify metadata, in addition to the submitter and updater. 
			writable.addAll(CollectionRightsExtractor.getUsersWhoCan((ResourceCollection) collection,
					Permissions.MODIFY_METADATA, true));
		}
		
		//Iterate through the users and get the ids. 
		for (TdarUser p : writable) {
			if (PersistableUtils.isNullOrTransient(p)) {
				continue;
			}
			users.add(p.getId());
		}
		
		// FIXME: decide whether right should inherit from projects (1) of (2)
		// change see authorizedUserDao
		// sb.append(getAdditionalUsersWhoCanModify());
		logger.trace("effectiveUsers:" + users);
		return users;
	}

	/*
	 * this function should introduce into the index all of the people who can
	 * modify a record which is useful for limiting things on the project page
	 */
	public List<Long> getUsersWhoCanView() {
		List<Long> users = new ArrayList<Long>();
		HashSet<TdarUser> writable = new HashSet<>();
		writable.add(resource.getSubmitter());
		writable.add(resource.getUpdatedBy());
		for (ResourceCollection collection : resource.getManagedResourceCollections()) {
			writable.addAll(CollectionRightsExtractor.getUsersWhoCan((ResourceCollection) collection,
					Permissions.VIEW_ALL, true));
		}
		for (TdarUser p : writable) {
			if (PersistableUtils.isNullOrTransient(p)) {
				continue;
			}
			users.add(p.getId());
		}
		// FIXME: decide whether right should inherit from projects (1) of (2)
		// change see authorizedUserDao
		// sb.append(getAdditionalUsersWhoCanModify());
		logger.trace("effectiveUsers:" + users);
		return users;
	}

	/**
	 * For the given resource, traverse through all the collections, and parse
	 * out the associated collection information. The resource will keep track
	 * of which collections it is directly associated to as well as the parent
	 * collections.
	 *
	 */
	public void extractHierarchy() {
		// Go through each of the collections that is associated with the
		// resource.
		for (ResourceCollection collection : resource.getManagedResourceCollections()) {
			if (!collection.isActive()) {//Skip over this collection if its not active.
				continue;
			}

			directManagedCollectionIds.add(collection.getId());
			directManagedCollectionNames.add(collection.getName());

			// Add all this collection's heirarchy information to the lists.
			managedCollectionIds.add(collection.getId());
			managedCollectionIds.addAll(collection.getParentIds());
			managedCollectionIds.addAll(collection.getAlternateParentIds());
			collectionNames.addAll(collection.getParentNameList());
			collectionNames.addAll(collection.getAlternateParentNameList());
		}

		//Go through the unmanaged collections and get the parent info. 
		for (ResourceCollection collection : resource.getUnmanagedResourceCollections()) {
			if (!collection.isActive()) { //skip if the collection is not active. 
				continue;
			}
			
			directUnmanagedCollectionIds.add(collection.getId());
			directUnamangedCollectionNames.add(collection.getName());
	
			// Add all this collection's heirarchy information to the lists.
			unmanagedCollectionIds.add(collection.getId());
			unmanagedCollectionIds.addAll(collection.getParentIds());
			unmanagedCollectionIds.addAll(collection.getAlternateParentIds());
			collectionNames.addAll(collection.getParentNameList());
			collectionNames.addAll(collection.getAlternateParentNameList());
		}

		allCollectionIds.addAll(managedCollectionIds);
	}

	public HashSet<Long> getDirectManagedCollectionIds() {
		return directManagedCollectionIds;
	}

	public void setDirectManagedCollectionIds(HashSet<Long> directCollectionIds) {
		this.directManagedCollectionIds = directCollectionIds;
	}

	public HashSet<Long> getManagedCollectionIds() {
		return managedCollectionIds;
	}

	public void setManagedCollectionIds(HashSet<Long> collectionIds) {
		this.managedCollectionIds = collectionIds;
	}

	public HashSet<Long> getAllCollectionIds() {
		return allCollectionIds;
	}

	public void setAllCollectionIds(HashSet<Long> allCollectionIds) {
		this.allCollectionIds = allCollectionIds;
	}

	public HashSet<String> getCollectionNames() {
		return collectionNames;
	}

	public void setCollectionNames(HashSet<String> collectionNames) {
		this.collectionNames = collectionNames;
	}

	public Set<String> getDirectManagedCollectionNames() {
		return directManagedCollectionNames;
	}

	public void setDirectManagedCollectionNames(Set<String> directCollectionNames) {
		this.directManagedCollectionNames = directCollectionNames;
	}

	public HashSet<Long> getDirectUnmanagedCollectionIds() {
		return directUnmanagedCollectionIds;
	}

	public void setDirectUnmanagedCollectionIds(HashSet<Long> listCollectionIds) {
		this.directUnmanagedCollectionIds = listCollectionIds;
	}

	public HashSet<String> getDirectUnmanagedCollectionNames() {
		return directUnamangedCollectionNames;
	}

	public void setDirectUnmanagedCollectionNames(HashSet<String> listCollectionNames) {
		this.directUnamangedCollectionNames = listCollectionNames;
	}

    public HashSet<Long> getUnmanagedCollectionIds() {
        return unmanagedCollectionIds;
    }

    public void setUnmanagedCollectionIds(HashSet<Long> unmanagedCollectionIds) {
        this.unmanagedCollectionIds = unmanagedCollectionIds;
    }
}
