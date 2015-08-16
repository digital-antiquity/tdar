/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasImage;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SimpleSearch;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * @author Adam Brin
 * 
 *         Resource Collections serve a number of purposes:
 *         - they manage rights
 *         - they organize resources
 *         The combination enables us to manage all access rights and permissions for resources through the user of these collections.
 * 
 *         <b>INTERNAL</b> collections enable access rights to a specific resource. Users never see these, they simply see the rights on the resource.
 *         <b>SHARED</b> collections are ones that users create and enable access. Shared collections can be public or private
 *         <b>PUBLIC</b> collections do not store rights and can be used for bookmarks and such things (not fully implemented).
 * 
 *         The Tree structure that is represented is a hybrid of a "materialized path" implementation -- see
 *         http://vadimtropashko.wordpress.com/2008/08/09/one-more-nested-intervals-vs-adjacency-list-comparison/.
 *         It's however, optimized so that the node's children are manifested in a supporting table to optimize rights queries, which will be the most common
 *         lookup.
 */
@Entity
@Indexed(index = "Collection")
@Table(name = "collection", indexes = {
        @Index(name = "collection_parent_id_idx", columnList = "parent_id"),
        @Index(name = "collection_owner_id_idx", columnList = "owner_id"),
        @Index(name = "collection_updater_id_idx", columnList = "updater_id")
})
@XmlRootElement(name = "ResourceCollection")
@XmlSeeAlso(WhiteLabelCollection.class)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection")
@Inheritance(strategy = InheritanceType.JOINED)
public class ResourceCollection extends Persistable.Base implements HasName, Updatable, Indexable, Validatable, Addressable, Comparable<ResourceCollection>,
        SimpleSearch, Sortable, Viewable, DeHydratable, HasSubmitter, XmlLoggable, HasImage, Slugable {

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private transient boolean changesNeedToBeLogged = false;
    private transient boolean viewable;
    private transient Integer maxHeight;
    private transient Integer maxWidth;
    private transient VersionType maxSize;

    // private transient boolean readyToIndex = true;
    public enum CollectionType {
        INTERNAL("Internal"), SHARED("Shared"), PUBLIC("Public");

        private String label;

        private CollectionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    private static final long serialVersionUID = -5308517783896369040L;
    public static final SortOption DEFAULT_SORT_OPTION = SortOption.TITLE;
    private transient Float score;
    private transient Explanation explanation;

    @Column
    @JsonView(JsonLookupFilter.class)
    @Fields({
            @Field(name = QueryFieldNames.COLLECTION_NAME_AUTO, norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class) ),
            @Field(name = QueryFieldNames.COLLECTION_NAME),
            @Field(name = QueryFieldNames.COLLECTION_NAME_PHRASE, norms = Norms.NO, store = Store.NO,
                    analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class) ),
    })
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "description_admin")
    private String adminDescription;

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "resourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.resources")
    private Set<Resource> resources = new LinkedHashSet<Resource>();

    /**
     * Sort-of hack to support saving of massive resource collections -- the select that is generated for getResources() does a polymorphic deep dive for every
     * field when it only really needs to get at the Ids for proper logging.
     * 
     * @return
     */
    @ElementCollection
    @CollectionTable(name = "collection_resource", joinColumns = @JoinColumn(name = "collection_id") )
    @Column(name = "resource_id")
    @Immutable
    private Set<Long> resourceIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption sortBy = DEFAULT_SORT_OPTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption secondarySortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", length = FieldLength.FIELD_LENGTH_50)
    private DisplayOrientation orientation = DisplayOrientation.LIST;

    @Field(name = QueryFieldNames.COLLECTION_TYPE)
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", length = FieldLength.FIELD_LENGTH_255)
    @NotNull
    private CollectionType type;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_collection_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.authorizedUsers")
    private Set<AuthorizedUser> authorizedUsers = new LinkedHashSet<AuthorizedUser>();

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @IndexedEmbedded
    @JoinColumn(name = "owner_id", nullable = false)
    private TdarUser owner;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "updater_id", nullable = true)
    private TdarUser updater;

    @Column(nullable = false, name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Field(norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    @Column(nullable = true, name = "date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    @DateBridge(resolution = Resolution.MILLISECOND)
    private Date dateUpdated;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ResourceCollection parent;

    @ElementCollection()
    @CollectionTable(name = "collection_parents", joinColumns = @JoinColumn(name = "collection_id") )
    @Column(name = "parent_id")
    private Set<Long> parentIds = new HashSet<>();

    private transient Set<ResourceCollection> transientChildren = new LinkedHashSet<>();

    @Field
    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    public ResourceCollection() {
        setDateCreated(new Date());
    }

    public ResourceCollection(Long id, String title, String description, SortOption sortBy, CollectionType type, boolean visible) {
        this(title, description, sortBy, type, visible, null);
        setId(id);
    }

    public ResourceCollection(String title, String description, SortOption sortBy, CollectionType type, boolean visible, TdarUser creator) {
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setType(type);
        setHidden(visible);
        setOwner(creator);
    }

    public ResourceCollection(CollectionType type) {
        this.type = type;
        setDateCreated(new Date());
    }

    public ResourceCollection(Resource resource, TdarUser owner) {
        this(CollectionType.SHARED);
        this.owner = owner;
        getResources().add(resource);
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Fields({
            @Field,
            @Field(name = QueryFieldNames.DESCRIPTION_PHRASE, norms = Norms.NO, store = Store.NO, analyzer = @Analyzer(
                    impl = TdarCaseSensitiveStandardAnalyzer.class) )
    })
    @Override
    @JsonView(JsonLookupFilter.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //if you serialize this (even if just a list IDs, hibernate will request all necessary fields and do a traversion of the full resource graph (this could crash tDAR if > 100,000)
    @XmlTransient
    public Set<Resource> getResources() {
        return resources;
    }


    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public CollectionType getType() {
        return type;
    }

    public void setType(CollectionType type) {
        this.type = type;
    }

    // FIXME: want to serialize these out, but cannot properly obfuscate them because they're in a "managed" set
    // if you do, and try and obfsucate by removing, you end up in a situation of completely removing the object
    @XmlTransient
    public Set<AuthorizedUser> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(Set<AuthorizedUser> users) {
        this.authorizedUsers = users;
    }

    @XmlAttribute(name = "ownerIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getOwner() {
        return owner;
    }

    public void setOwner(TdarUser owner) {
        this.owner = owner;
    }

    @XmlAttribute(name = "parentIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public ResourceCollection getParent() {
        return parent;
    }

    public void setParent(ResourceCollection parent) {
        this.parent = parent;
    }

    @XmlAttribute
    public boolean isHidden() {
        return hidden;
    }

    @Field
    @XmlTransient
    public boolean isTopLevel() {
        if ((getParent() == null) || (getParent().isHidden() == true)) {
            return true;
        }
        return false;
    }

    public void setHidden(boolean visible) {
        this.hidden = visible;
    }

    /*
     * Convenience Method that provides a list of users that match the permission
     */
    public Set<TdarUser> getUsersWhoCan(GeneralPermissions permission, boolean recurse) {
        Set<TdarUser> people = new HashSet<>();
        for (AuthorizedUser user : authorizedUsers) {
            if (user.getEffectiveGeneralPermission() >= permission.getEffectivePermissions()) {
                people.add(user.getUser());
            }
        }
        if ((getParent() != null) && recurse) {
            people.addAll(getParent().getUsersWhoCan(permission, recurse));
        }
        return people;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.Updatable#markUpdated(org.tdar.core.bean.entity.Person)
     */
    @Override
    public void markUpdated(TdarUser p) {
        if ((getDateCreated() == null) || (getOwner() == null)) {
            setDateCreated(new Date());
            setOwner(p);
            setUpdater(p);
        }
        setUpdater(p);
        setDateUpdated(new Date());

    }

    /**
     * @param dateCreated
     *            the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    public boolean isShared() {
        return type == CollectionType.SHARED;
    }

    public boolean isInternal() {
        return type == CollectionType.INTERNAL;
    }

    /**
     * @param sortBy
     *            the sortBy to set
     */
    public void setSortBy(SortOption sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * @return the sortBy
     */
    @Override
    public SortOption getSortBy() {
        return sortBy;
    }

    @Override
    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    @Transient
    @XmlTransient
    public Float getScore() {
        return this.score;
    }

    @Override
    @Transient
    @XmlTransient
    public Explanation getExplanation() {
        return explanation;
    }

    @Override
    public void setExplanation(Explanation ex) {
        this.explanation = ex;
    }

    @Override
    public String toString() {
        return String.format("%s Resource collection %s: %s (creator: %s)", getType(), getId(), getName(), owner);
    }

    @Transient
    public Long getParentId() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getId();
    }

    /*
     * used for populating the Lucene Index with users that have appropriate rights to modify things in the collection
     */
    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_MODIFY)
    @Transient
    @ElementCollection
    @IndexedEmbedded
    public List<Long> getUsersWhoCanModify() {
        return toUserList(GeneralPermissions.MODIFY_RECORD);
    }

    private List<Long> toUserList(GeneralPermissions permission) {
        ArrayList<Long> users = new ArrayList<>();
        HashSet<TdarUser> writable = new HashSet<>();
        writable.add(getOwner());
        writable.addAll(getUsersWhoCan(permission, true));
        for (TdarUser p : writable) {
            if (PersistableUtils.isNullOrTransient(p)) {
                continue;
            }
            users.add(p.getId());
        }
        return users;
    }

    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER)
    @Transient
    @ElementCollection
    @IndexedEmbedded
    public List<Long> getUsersWhoCanAdminister() {
        return toUserList(GeneralPermissions.ADMINISTER_GROUP);
    }

    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW)
    @Transient
    @ElementCollection
    @IndexedEmbedded
    public List<Long> getUsersWhoCanView() {
        return toUserList(GeneralPermissions.VIEW_ALL);
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    @Override
    public int compareTo(ResourceCollection o) {
        List<String> tree = getParentNameList();
        List<String> tree_ = o.getParentNameList();
        while (!tree.isEmpty() && !tree_.isEmpty() && (tree.get(0) == tree_.get(0))) {
            tree.remove(0);
            tree_.remove(0);
        }
        if (tree.isEmpty()) {
            return -1;
        } else if (tree_.isEmpty()) {
            return 1;
        } else {
            return tree.get(0).compareTo(tree_.get(0));
        }
    }

    /*
     * Get all of the resource collections via a tree (actually list of lists)
     */
    @Transient
    @XmlTransient
    // infinite loop because parentTree[0]==self
    public List<ResourceCollection> getHierarchicalResourceCollections() {
        ArrayList<ResourceCollection> parentTree = new ArrayList<>();
        parentTree.add(this);
        ResourceCollection collection = this;
        while (collection.getParent() != null) {
            collection = collection.getParent();
            parentTree.add(0, collection);
        }
        return parentTree;
    }

    @Transient
    @XmlTransient
    public List<ResourceCollection> getVisibleParents() {
        List<ResourceCollection> hierarchicalResourceCollections = getHierarchicalResourceCollections();
        Iterator<ResourceCollection> iterator = hierarchicalResourceCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection collection = iterator.next();
            if (!collection.isShared() || !collection.isHidden()) {
                iterator.remove();
            }
        }
        return hierarchicalResourceCollections;
    }

    /*
     * Get ordered list of parents (titles) of this resources ... great grandfather, grandfather, father, you.
     */
    @Transient
    @XmlTransient
    public List<String> getParentNameList() {
        ArrayList<String> parentNameTree = new ArrayList<String>();
        for (ResourceCollection collection : getHierarchicalResourceCollections()) {
            parentNameTree.add(collection.getName());
        }
        return parentNameTree;
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(getName());
    }

    @Override
    public boolean isValid() {
        logger.trace("type: {} owner: {} name: {} sort: {}", getType(), getOwner(), getName(), getSortBy());
        if ((getType() == CollectionType.INTERNAL) || isValidForController()) {
            if ((getType() == CollectionType.SHARED) && (sortBy == null)) {
                return false;
            }
            return ((getOwner() != null) && (getOwner().getId() != null) && (getOwner().getId() > -1));
        }
        return false;
    }

    @Field(name = QueryFieldNames.TITLE_SORT, norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    @Override
    public String getTitleSort() {
        if (getTitle() == null) {
            return "";
        }
        return getTitle().replaceAll(SimpleSearch.TITLE_SORT_REGEX, "");
    }

    @Fields({
            @Field,
            @Field(name = QueryFieldNames.TITLE_PHRASE, norms = Norms.NO, store = Store.NO,
                    analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class) )
    })
    // @Boost(1.5f)
    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getUrlNamespace() {
        return "collection";
    }

    @XmlTransient
    @Override
    public boolean isViewable() {
        return viewable;
    }

    @Override
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    public boolean isPublic() {
        return type == CollectionType.PUBLIC;
    }

    @Override
    @Transient
    public TdarUser getSubmitter() {
        return owner;
    }

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    private transient boolean readyToStore = true;

    @Transient
    @XmlTransient
    public boolean isReadyToStore() {
        return readyToStore;
    }

    public void setReadyToStore(boolean readyToStore) {
        this.readyToStore = readyToStore;
    }

    @Override
    @XmlTransient
    @Transient
    public boolean isReadyToIndex() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setReadyToIndex(boolean ready) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @XmlAttribute(name = "updaterIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public TdarUser getUpdater() {
        return updater;
    }

    public void setUpdater(TdarUser updater) {
        this.updater = updater;
    }

    public void normalizeAuthorizedUsers() {
        normalizeAuthorizedUsers(authorizedUsers);
    }

    /**
     * Remove entries from provided list of AuthorizedUsers that contain duplicate User values. Retained
     * AuthorizedUsers will always have equal or greater permissions relative to the removed duplicate items.
     * 
     * @param authorizedUsers
     */
    public static final void normalizeAuthorizedUsers(Collection<AuthorizedUser> authorizedUsers) {
        Logger staticLogger = LoggerFactory.getLogger(ResourceCollection.class);
        staticLogger.trace("incoming " + authorizedUsers);
        Map<Long, AuthorizedUser> bestMap = new HashMap<>();
        Iterator<AuthorizedUser> iterator = authorizedUsers.iterator();
        while (iterator.hasNext()) {
            AuthorizedUser incoming = iterator.next();
            if ((incoming == null) || (incoming.getUser() == null)) {
                continue;
            }
            Long user = incoming.getUser().getId();

            AuthorizedUser existing = bestMap.get(user);
            staticLogger.trace(incoming + " <==>" + existing);
            if (existing != null) {
                if (existing.getGeneralPermission().getEffectivePermissions() >= incoming.getGeneralPermission().getEffectivePermissions()) {
                    continue;
                }
            }
            bestMap.put(user, incoming);
        }

        authorizedUsers.clear();
        authorizedUsers.addAll(bestMap.values());
        staticLogger.trace("outgoing" + authorizedUsers);

    }

    @XmlTransient
    @Transient
    public Set<ResourceCollection> getTransientChildren() {
        return transientChildren;
    }

    public void setTransientChildren(Set<ResourceCollection> transientChildren) {
        this.transientChildren = transientChildren;
    }

    public SortOption getSecondarySortBy() {
        return secondarySortBy;
    }

    public void setSecondarySortBy(SortOption secondarySortBy) {
        this.secondarySortBy = secondarySortBy;
    }

    public String getAdminDescription() {
        return adminDescription;
    }

    public void setAdminDescription(String adminDescription) {
        this.adminDescription = adminDescription;
    }

    /**
     * Get ordered list of parents (ids) of this resources ... great grandfather, grandfather, father.
     * 
     * Note: in earlier implementations this contained the currentId as well, I've removed this, but am unsure
     * whether it should be there
     */
    @Transient
    @Field(name = QueryFieldNames.COLLECTION_TREE)
    @ElementCollection
    @IndexedEmbedded
    public Set<Long> getParentIds() {
        return parentIds;
    }

    public void setParentIds(Set<Long> parentIds) {
        this.parentIds = parentIds;
    }

    public boolean isChangesNeedToBeLogged() {
        return changesNeedToBeLogged;
    }

    public void setChangesNeedToBeLogged(boolean changesNeedToBeLogged) {
        this.changesNeedToBeLogged = changesNeedToBeLogged;
    }

    @JsonView(JsonLookupFilter.class)
    public String getDetailUrl() {
        return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
    }

    @Fields({
            @Field(name = QueryFieldNames.ALL_PHRASE, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class) ),
            @Field(name = QueryFieldNames.ALL, analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class) ) })
    public String getAllFieldSearch() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle()).append(" ").append(getDescription()).append(" ");
        return sb.toString();
    }

    @Override
    public Integer getMaxHeight() {
        return maxHeight;
    }

    @Override
    public void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    public Integer getMaxWidth() {
        return maxWidth;
    }

    @Override
    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public VersionType getMaxSize() {
        return maxSize;
    }

    @Override
    public void setMaxSize(VersionType maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public String getSlug() {
        return UrlUtils.slugify(getName());
    }

    @XmlTransient
    public boolean isWhiteLabelCollection() {
        return false;
    }

    @XmlTransient
    public boolean isSearchEnabled() {
        return false;
    }

    @XmlTransient
    public boolean isTopCollection() {
        return parent == null;
    }

    @XmlTransient
    public boolean isSubCollection() {
        return parent != null;
    }

    /**
     * Sort-of hack to support saving of massive resource collections -- the select that is generated for getResources() does a polymorphic deep dive for every
     * field when it only really needs to get at the Ids for proper logging.
     * 
     * @return
     */
    @XmlElementWrapper(name = "resources")
    @XmlElement(name = "resourceId")
    public Set<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Set<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

}
