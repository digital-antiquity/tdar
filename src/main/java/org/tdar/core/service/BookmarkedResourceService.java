package org.tdar.core.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.BookmarkedResourceDao;

/**
 * Helps create and manage Bookmarks for users. Bookmarks are for keeping track of items, and identifying items for DataIntegration
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional
@Service
public class BookmarkedResourceService extends ServiceInterface.TypedDaoBase<BookmarkedResource, BookmarkedResourceDao> {

    @Transactional(readOnly = true)
    public boolean isAlreadyBookmarked(Resource resource, Person person) {
        return getDao().isAlreadyBookmarked(resource, person);
    }

    /**
     * Returns true if this a new @link BookmarkedResource for a @link Resource and @link Person was created as a result of this call, false otherwise.
     * 
     * @param resource
     * @param person
     * @return
     */
    public boolean bookmarkResource(Resource resource, Person person) {
        if (isAlreadyBookmarked(resource, person)) {
            logger.trace(String.format("person %s already bookmarked resource %s", person, resource.getId()));
            return false;
        }
        logger.trace("Creating bookmark for :" + person + " of " + resource.getId() + "[" + resource.getResourceType() + "]");
        BookmarkedResource bookmark = new BookmarkedResource();
        bookmark.setResource(resource);
        // FIXME: names should be editable by the user, and have a better default.
        // not sure why names are constructed this way, was in previous bookmarking code.
        bookmark.setName("Bookmark for " + TdarConfiguration.getInstance().getSiteAcronym() + " resource:" + resource.getId());
        bookmark.setTimestamp(new Date());
        bookmark.setPerson(person);
        resource.getBookmarks().add(bookmark);
        getDao().save(bookmark);
        return true;
    }

    /**
     * Returns true if a @link BookmarkedResouce exists for a @link Person and @link Resource and it was removed successfully.
     * @param resource
     * @param person
     * @return
     */
    public boolean removeBookmark(Resource resource, Person person) {
        BookmarkedResource bookmark = getDao().findBookmark(resource, person);
        if (bookmark == null) {
            return false;
        }
        resource = getDao().merge(resource);
        boolean removed = resource.getBookmarks().remove(bookmark);
        logger.debug("was bookmark removed? " + removed);
        save(resource);
        getDao().delete(bookmark);
        return true;
    }

    /**
     * Find all @link Resource entries that are referred to by a @link BookmarkedResource and a @link Person with a specified set of @link Status entries.
     * 
     * @param person
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    public List<Resource> findBookmarkedResourcesByPerson(Person person, List<Status> statuses) {
        return getDao().findBookmarkedResourcesByPerson(person, statuses);
    }
}
