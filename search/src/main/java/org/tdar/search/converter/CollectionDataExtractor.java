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

	// Contains ONLY ids of Managed collections the resource is a direct part of.  
	private HashSet<Long> directManagedCollectionIds = new HashSet<>();

	// Contains ONLY ids of Unmanaged collections the resource is a direct part of.
	private HashSet<Long> directUnmanagedCollectionIds = new HashSet<>();
	
	
	//This contains ALL collection ids (managed/unmanaged, and parents). 
	private HashSet<Long> collectionIds = new HashSet<>();;
	
	//This ends up being the same as collectionsId. 
	private HashSet<Long> allCollectionIds = new HashSet<>(); 
	
	//This contains ALL collection names (managed/unmanaged, and parents). 
	private HashSet<String> collectionNames = new HashSet<>();
	
	//This contains ONLY  names for direct collections. 
	private Set<String> directCollectionNames = new HashSet<>(); 

	//Contains ALL collection IDs for unmanaged and their parents. 
	private HashSet<Long> listCollectionIds = new HashSet<>();
	
	//Contains ALL collection names of Unmanaged collections and their parents. 
	private HashSet<String> listCollectionNames = new HashSet<>();

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

			ResourceCollection shared = (ResourceCollection) collection;
			directManagedCollectionIds.add(collection.getId());
			directCollectionNames.add(collection.getName());

			// Add all this collections heirarchy information to the lists.
			collectionIds.add(collection.getId());
			collectionIds.addAll(shared.getParentIds());
			collectionIds.addAll(shared.getAlternateParentIds());
			collectionNames.addAll(shared.getParentNameList());
			collectionNames.addAll(shared.getAlternateParentNameList());
		}

		//Go through the unmanaged collections and get the parent info. 
		for (ResourceCollection collection : resource.getUnmanagedResourceCollections()) {
			if (!collection.isActive()) { //skip if the collection is not active. 
				continue;
			}
			
			// BC: I created this field because the managed resources have direct managed collection ids. 
			getDirectUnmanagedCollectionIds().add(collection.getId());
	
			//Adds to the global list of ids. 
			collectionIds.add(collection.getId());
			listCollectionIds.add(collection.getId());
			
			//Store the Parents' data to both the List Collections and Collection names
			listCollectionIds.addAll(collection.getParentIds());
			listCollectionNames.addAll(collection.getParentNameList());
			collectionIds.addAll(collection.getParentIds());
			collectionNames.addAll(collection.getParentNameList());
		}

		allCollectionIds.addAll(collectionIds);
		
		//Why would managed collection ids be added to the list collections?
		//Or not add the names to the list?
		getListCollectionIds().addAll(directManagedCollectionIds);
	}

	public HashSet<Long> getDirectCollectionIds() {
		return directManagedCollectionIds;
	}

	public void setDirectCollectionIds(HashSet<Long> directCollectionIds) {
		this.directManagedCollectionIds = directCollectionIds;
	}

	public HashSet<Long> getCollectionIds() {
		return collectionIds;
	}

	public void setCollectionIds(HashSet<Long> collectionIds) {
		this.collectionIds = collectionIds;
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

	public Set<String> getDirectCollectionNames() {
		return directCollectionNames;
	}

	public void setDirectCollectionNames(Set<String> directCollectionNames) {
		this.directCollectionNames = directCollectionNames;
	}

	public HashSet<Long> getListCollectionIds() {
		return listCollectionIds;
	}

	public void setListCollectionIds(HashSet<Long> listCollectionIds) {
		this.listCollectionIds = listCollectionIds;
	}

	public HashSet<String> getListCollectionNames() {
		return listCollectionNames;
	}

	public void setListCollectionNames(HashSet<String> listCollectionNames) {
		this.listCollectionNames = listCollectionNames;
	}

	public HashSet<Long> getDirectUnmanagedCollectionIds() {
		return directUnmanagedCollectionIds;
	}

	public void setDirectUnmanagedCollectionIds(HashSet<Long> directUnmanagedCollectionIds) {
		this.directUnmanagedCollectionIds = directUnmanagedCollectionIds;
	}

}
