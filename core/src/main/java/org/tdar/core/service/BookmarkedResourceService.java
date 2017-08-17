package org.tdar.core.service;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;

public interface BookmarkedResourceService {

    <R> void applyTransientBookmarked(Collection<R> resources, TdarUser person);

    boolean isAlreadyBookmarked(Resource resource, TdarUser person);

    /**
     * Returns true if this a new @link BookmarkedResource for a @link Resource and @link Person was created as a result of this call, false otherwise.
     * 
     * @param resource
     * @param person
     * @return
     */
    boolean bookmarkResource(Resource resource, TdarUser person);

    /**
     * Returns true if a @link BookmarkedResouce exists for a @link Person and @link Resource and it was removed successfully.
     * 
     * @param resource
     * @param person
     * @return
     */
    boolean removeBookmark(Resource resource, TdarUser person);

    /**
     * Find all @link Resource entries that are referred to by a @link BookmarkedResource and a @link Person with a specified set of @link Status entries.
     * 
     * @param person
     * @param statuses
     * @return
     */
    List<Resource> findBookmarkedResourcesByPerson(TdarUser person, List<Status> statuses);

}