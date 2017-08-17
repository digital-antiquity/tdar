package org.tdar.core.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AgreementTypes;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;

import com.opensymphony.xwork2.TextProvider;

public interface EntityService {

    /**
     * Find a @link Person by ID
     * 
     * @param id
     * @return
     */
    Person findPerson(Long id);

    /**
     * Find all registered users (limited by most recent x)
     * 
     * @param maxResults
     * @return
     */
    List<TdarUser> findAllRegisteredUsers(int maxResults);

    /**
     * Find all registered users
     * 
     * @return
     */
    List<TdarUser> findAllRegisteredUsers();

    /**
     * Find all @link Institution entities.
     * 
     * @return
     */
    List<Institution> findAllInstitutions();

    /**
     * Find an @link Institution by exact name
     * 
     * @param name
     * @return
     */
    Institution findInstitutionByName(String name);

    /**
     * Find an @link Institution by exact name
     * 
     * @param institution
     * @return
     */
    List<Person> findPersonsByInstitution(Institution institution);

    /**
     * Find a @link Institution by ID
     * 
     * @param id
     * @return
     */
    Institution findInstitution(long id);

    /**
     * Find an @link Institution based on name bounded by wildcards %name%
     * 
     * @param name
     * @return
     */
    List<Institution> findInstitutionLike(String name);

    /**
     * Find a @link Person by their email
     * 
     * @param email
     * @return
     */
    Person findByEmail(String email);

    TdarUser findUserByEmail(String email);

    /**
     * Find a @link Person by their Username
     * 
     * @param username
     * @return
     */
    TdarUser findByUsername(String username);

    /**
     * Find people with a similar name (for suggesting we merge)
     * 
     * @param person
     * @return
     */
    List<Person> findSimilarPeople(TdarUser person);

    /**
     * Find a Person by their full name (guessing about how to split the name into parts)
     * 
     * @param fullName
     * @return
     */
    Set<Person> findByFullName(String fullName);

    /**
     * Find all resources to which the given user/Person has full access.
     * 
     * @param person
     * @return
     */
    Set<Resource> findFullUserResources(TdarUser person, boolean isAdmin);

    /**
     * Find or saveCreator based on type
     * 
     * @see #findOrSaveInstitution(Institution)
     * @see #findOrSavePerson(Person)
     * 
     * @param transientCreator
     * @return
     */
    <C extends Creator<?>> C findOrSaveCreator(C transientCreator);

    /**
     * Find a @link Person by id, email
     * 
     * @param transientPerson
     * @return
     */
    Person findPerson(Person transientPerson);

    /**
     * Finds the matching @link ResourceCreator (based on name, id, unique value) or save a new one.
     * 
     * @param resourceCreator
     */
    void findOrSaveResourceCreator(ResourceCreator resourceCreator);

    /**
     * List @link ResourceCollection entries that a @link Person (user) has access to
     * 
     * @param user
     * @return
     */
    List<ResourceCollection> findAccessibleResourceCollections(TdarUser user);

    /**
     * List recent logins to the system
     * 
     * @return
     */
    List<TdarUser> showRecentLogins();

    /**
     * Increment the login counter for a given user
     * 
     * @param authenticatedUser
     */
    void registerLogin(TdarUser authenticatedUser, String userAgent);

    /**
     * Find the number of actual contributors to tDAR based on resource.submitter
     * 
     * @return
     */
    Long findNumberOfActualContributors();

    /**
     * Given a @link Creator with a @link Status of Duplicate, return the Authority.
     * 
     * @param dup
     * @return
     */
    Creator<?> findAuthorityFromDuplicate(Creator<?> dup);

    /**
     * Find all Ids of (actual) contributors within the system based on "submitter" id
     * 
     * @return
     */
    Set<Long> findAllContributorIds();

    /**
     * Update the Person occurrance count for each Person -- effectively how often they're used or referenced in the Database. This can be used for relevancy
     * ranking and visualization
     */
    void updatePersonOcurrances();

    /**
     * get aggregate view counts for creators
     * 
     * @param creator
     * @return
     */
    Long getCreatorViewCount(Creator<?> creator);

    void findResourceCreator(ResourceCreator creator);

    void deleteForController(Creator<?> creator, String deletionReason, TdarUser authenticatedUser);

    DeleteIssue getDeletionIssues(TextProvider textProvider, Creator<?> persistable);

    void saveInstitutionForController(Institution persistable, String name, String email, FileProxy fileProxy);

    void savePersonforController(Person person, String email, String institutionName, FileProxy fileProxy);

    List<BookmarkedResource> getBookmarkedResourcesForUser(TdarUser user);

    Map<AgreementTypes, Long> getAgreementCounts();

    Map<UserAffiliation, Long> getAffiliationCounts();

    Map<UserAffiliation, Long> getAffiliationCounts(boolean b);

    String getSchemaOrgJson(Creator<?> creator, String logoUrl) throws IOException;

    void saveAddress(Address address2, Creator<?> creator);

    void deleteAddressForCreator(Address address, Creator<?> creator);

    List<ResourceRevisionLog> findChangesForUser(TdarUser user, Date date);

    TdarUser findUser(TdarUser user);

    Person find(Long dupe1Id);

    void delete(Person findByEmail);

}