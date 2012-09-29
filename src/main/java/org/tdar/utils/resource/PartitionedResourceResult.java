package org.tdar.utils.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.ArrayUtils;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;

/**
 * $Id$
 * 
 * Seperates a List of Resources into sub-lists based on their type. These sub-lists
 * can be accessed by passing the class of the desired type to the getResourcesOfType
 * method.
 * 
 * To iterate over all of the datasets:
 * 
 * <code>
 * PartitionedResourceResult p = new PartitionedResourceResult(r);
 * for (Dataset d : p.getResourcesOfType(Dataset.class)) {
 *     process(d);
 * }
 * </code>
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public class PartitionedResourceResult implements Serializable {

    private static final long serialVersionUID = -7217871981422478834L;
    private Collection<? super InformationResource> resources;
    private Map<Class<?>, List<?>> partitions;

    public PartitionedResourceResult(Collection<? super InformationResource> collection, Status... statuses) {
        this.resources = collection;

        partitions = new HashMap<Class<?>, List<?>>();
        for (ResourceType type : ResourceType.values()) {
            List<? extends Resource> listOfType = createListOfType(type.getResourceClass());
            for (int i = listOfType.size() - 1; i >= 0; i--) {
                if (statuses != null && statuses.length > 0 && !ArrayUtils.contains(statuses, listOfType.get(i).getStatus())) {
                    listOfType.remove(i);
                }
            }
            partitions.put(type.getResourceClass(), listOfType);
        }
    }

    /**
     * Returns a list of all of the elements of type
     * 
     * @param <T>
     *            -- Must extend Resource
     * @param clazz
     *            -- Class which extends Resource
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Resource> List<T> getResourcesOfType(Class<T> clazz) {
        return (List<T>) partitions.get(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T extends Resource> List<T> createListOfType(Class<T> clazz) {
        Predicate p = PredicateUtils.instanceofPredicate(clazz);
        return (List<T>) CollectionUtils.select(resources, p);
    }

    /**
     * @return The originally supplied Collection of Resources passed to the constructor.
     */
    public Collection<? super InformationResource> getResources() {
        return resources;
    }

}
