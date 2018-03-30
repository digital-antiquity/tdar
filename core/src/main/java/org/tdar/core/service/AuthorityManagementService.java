package org.tdar.core.service;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.ScrollableResults;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.service.authority.DupeMode;

public interface AuthorityManagementService {

    /**
     * Search through all of the defined classes in {@link #hostClasses} and find Fields that refer to the specified class.
     * 
     * @param referredClass
     * @param dupeIds
     * @return
     */
    Map<Field, ScrollableResults> getReferrers(Class<?> referredClass, Collection<Long> dupeIds);

    /**
     * Count the total number of Objects that refer to the set of Ids specified (to report to the user)
     * 
     * @param referredClass
     * @param idlist
     * @return
     */
    Map<Class<?>, Long> getReferrerCounts(Class<?> referredClass, List<Long> idlist);

    /**
     * Aggregate all referrer counts
     * 
     * @param referredClass
     * @param idlist
     * @return
     */
    long getTotalReferrerCount(Class<?> referredClass, List<Long> idlist);

    /**
     * Create an aggregated map of Ids and counts to report to the user what's going to be changed or adjusted
     * 
     * @param referredClass
     * @param idlist
     * @return
     */
    Map<Long, Long> getReferrerCountMaps(Class<?> referredClass, List<Long> idlist);

    // TODO: jim you (probably) aren't handling one-to-many correctly yet.
    /**
     * Find objects that refer to the specified duplicates and replace the references with the specified authority,
     * saving the referrers in the process. This method handles objects that may refer to another object via scalar
     * fields as well as via collection fields. A few assumptions, restricions:
     * 
     * - this method assumes that, for collection fields, it is not necessary to perform a piecewise replacement
     * of each duplicate record with an authority record. In other words, a collection that contains multiple
     * duplicates will be replaced by one (and only one) authority record (if the authority record is not already
     * in the collection)
     * 
     * - all of the potential referring classes must refer to duplicate objects via fields that have public getters
     * and setters.
     * 
     * Based on DupeMode, this method will do different things:
     * - MARK_DUPS_ONLY -- only marks the dups, does not do anything else
     * - MARK_DUPS_AND_CONSOLDIATE -- mark the items as dups, but also transfer their references to the declared master
     * - DELETE_DUPLICATES -- completely delete the duplicate
     * 
     * @param user
     * @param class1
     * @param dupeIds
     * @param authorityId
     * @param dupeMode
     */
    <T extends Dedupable> void updateReferrers(Person user, Class<? extends Dedupable> class1, Collection<Long> dupeIds, Long authorityId,
            DupeMode dupeMode, boolean sendEmail);

    /**
     * For People, we have "protected" resources, those that have User accounts, we have to count them to ensure that we don't try and dedup two into one (which
     * is unsupported, and bad).
     * 
     * @param dupes
     * @return
     */
    <T extends Dedupable> int countProtectedRecords(Collection<T> dupes);

    void findPluralDups(Class<? extends Keyword> cls, Person user, boolean listOnly);

    void cleanupKeywordDups(TdarUser authenticatedUser);

    void cleanupInstitutionsWithSpaces(TdarUser user);

}