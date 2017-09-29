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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Hideable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.TitleSortComparator;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@Table(name = "collection", indexes = {
        @Index(name = "collection_parent_id_idx", columnList = "parent_id"),
        @Index(name = "collection_owner_id_idx", columnList = "owner_id"),
        @Index(name = "collection_updater_id_idx", columnList = "updater_id")
})
// @XmlRootElement(name = "resourceCollection")
@XmlType(name = "collection")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection")
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
@JsonInclude(value = Include.NON_NULL)
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name = "collection_type", length = FieldLength.FIELD_LENGTH_255, discriminatorType = DiscriminatorType.STRING)
//@XmlSeeAlso(value = { ResourceCollection.class })
@XmlRootElement(name = "resourceCollection")
@SecondaryTable(name = "whitelabel_collection", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class ResourceCollection extends AbstractPersistable
        implements Updatable, Validatable, DeHydratable, HasSubmitter, XmlLoggable, HasStatus, HasAuthorizedUsers, Sortable,
        OaiDcProvider, HasName, Slugable, Addressable, Indexable, Viewable, Hideable, Comparable<ResourceCollection> {

    public static final SortOption DEFAULT_SORT_OPTION = SortOption.TITLE;


    public ResourceCollection(String title, String description, boolean hidden, SortOption sortOption, DisplayOrientation displayOrientation, TdarUser creator) {
        setName(title);
        setDescription(description);
        setHidden(hidden);
        setSortBy(sortOption);
        setOrientation(displayOrientation);
        setOwner(creator);
    }
    
    public ResourceCollection(Long id, String title, String description, SortOption sortOption, boolean hidden) {
        setId(id);
        setName(title);
        setDescription(description);
        setHidden(hidden);
        setSortBy(sortOption);

    }

    public ResourceCollection(String title, String description, TdarUser submitter) {
        setName(title);
        setDescription(description);
        setHidden(false);
        this.setOwner(submitter);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
    }

    public ResourceCollection(Document document, TdarUser tdarUser) {
        markUpdated(tdarUser);
        getManagedResources().add(document);
        setHidden(false);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
    }

    public ResourceCollection() {
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
    }

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private transient boolean changesNeedToBeLogged = false;

    private static final long serialVersionUID = -5308517783896369040L;
    @Column(name = "system_managed")
    private Boolean systemManaged = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @XmlTransient
    @Column(name = "collection_type", updatable = false, insertable = false)
    private CollectionResourceSection type = CollectionResourceSection.MANAGED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_50)
    @JsonView(JsonLookupFilter.class)
    private Status status = Status.ACTIVE;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_collection_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.authorizedUsers")
    private Set<AuthorizedUser> authorizedUsers = new LinkedHashSet<AuthorizedUser>();

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "owner_id", nullable = false)
    private TdarUser owner;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "updater_id", nullable = true)
    private TdarUser updater;

    @Column(nullable = false, name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date dateCreated;

    private transient TreeSet<ResourceCollection> transientChildren = new TreeSet<>(new TitleSortComparator());

    @Column(nullable = false, name = "date_updated")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated;

    @OneToMany()
    @JoinColumn(name = "collection_id", foreignKey = @javax.persistence.ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = true)
    @XmlTransient
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<CollectionRevisionLog> collectionRevisionLog = new HashSet<>();

    /**
     * Sort-of hack to support saving of massive resource collections -- the select that is generated for getResources() does a polymorphic deep dive for every
     * field when it only really needs to get at the Ids for proper logging.
     *
     * @return
     */
    @ElementCollection
    @CollectionTable(name = "collection_resource", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "resource_id")
    @Immutable
    // fixme: replace resourceIds hack with service/dao with optimized DAO save() method. (TDAR-5605)
    private Set<Long> resourceIds = new HashSet<>();

    private transient boolean created;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption sortBy = DEFAULT_SORT_OPTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_sort_order", length = FieldLength.FIELD_LENGTH_25)
    private SortOption secondarySortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", length = FieldLength.FIELD_LENGTH_50)
    private DisplayOrientation orientation = DisplayOrientation.LIST;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "whitelabel", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "custom_header_enabled", column = @Column(table = "whitelabel_collection", columnDefinition = "boolean default false")),
            @AttributeOverride(name = "custom_doc_logo_enabled", column = @Column(table = "whitelabel_collection", columnDefinition = "boolean default false")),
            @AttributeOverride(name = "featured_resources_enabled",
                    column = @Column(table = "whitelabel_collection", columnDefinition = "boolean default false")),
            @AttributeOverride(name = "search_enabled", column = @Column(table = "whitelabel_collection", columnDefinition = "boolean default false")),
            @AttributeOverride(name = "sub_collections_enabled", column = @Column(table = "whitelabel_collection", columnDefinition = "boolean default false")),
            @AttributeOverride(name = "subtitle", column = @Column(table = "whitelabel_collection")),
            @AttributeOverride(name = "css", column = @Column(table = "whitelabel_collection"))
    })
    @Access(AccessType.FIELD)
    private CollectionDisplayProperties properties;

    public CollectionDisplayProperties getProperties() {
        return properties;
    }

    public void setProperties(CollectionDisplayProperties properties) {
        this.properties = properties;
    }

    private transient boolean viewable;

    @JsonView(JsonLookupFilter.class)
    @Length(max = FieldLength.FIELD_LENGTH_500, min = 1)
    @NotNull
    private String name;

    @ElementCollection()
    @CollectionTable(name = "collection_parents", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "parent_id")
    private Set<Long> parentIds = new HashSet<>();

    @ElementCollection()
    @CollectionTable(name = "collection_alternate_parents", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "parent_id")
    private Set<Long> alternateParentIds = new HashSet<>();

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_formatted")
    private String formattedDescription;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "managedResourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.SharedCollection.resources")
    private Set<Resource> managedResources = new LinkedHashSet<Resource>();

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "unmanagedResourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.unmanagedResources")
    private Set<Resource> unmanagedResources = new LinkedHashSet<Resource>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ResourceCollection parent;

    @ManyToOne
    @JoinColumn(name = "alternate_parent_id")
    private ResourceCollection alternateParent;

    // if you serialize this (even if just a list IDs, hibernate will request all necessary fields and do a traversion of the full resource graph (this could
    // crash tDAR if > 100,000)
    @XmlTransient
    public Set<Resource> getManagedResources() {
        return managedResources;
    }

    public void setManagedResources(Set<Resource> resources) {
        this.managedResources = resources;
    }

    /*
     * Get all of the resource collections via a tree (actually list of lists)
     */
    @Transient
    @XmlTransient
    // infinite loop because parentTree[0]==self
    public List<ResourceCollection> getHierarchicalResourceCollections() {
        ArrayList<ResourceCollection> parentTree = new ArrayList<>();
        parentTree.add((ResourceCollection) this);
        ResourceCollection collection = (ResourceCollection) this;
        while (collection.getParent() != null) {
            collection = (ResourceCollection) collection.getParent();
            parentTree.add(0, collection);
        }
        return parentTree;
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
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

    @XmlTransient
    @Transient
    public List<String> getParentNameList() {
        ArrayList<String> parentNameTree = new ArrayList<String>();
        for (ResourceCollection collection : getHierarchicalResourceCollections()) {
            parentNameTree.add(collection.getName());
        }
        return parentNameTree;
    }

    @Transient
    @XmlTransient
    public List<ResourceCollection> getVisibleParents() {
        List<ResourceCollection> hierarchicalResourceCollections = getHierarchicalResourceCollections();
        Iterator<ResourceCollection> iterator = hierarchicalResourceCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection collection = iterator.next();
            if (!(ResourceCollection.class.isAssignableFrom(collection.getClass())) || !collection.isHidden()) {
                iterator.remove();
            }
        }
        return hierarchicalResourceCollections;
    }

    @XmlAttribute(name = "altParentIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public ResourceCollection getAlternateParent() {
        return alternateParent;
    }

    public void setAlternateParent(ResourceCollection alternateParent) {
        this.alternateParent = alternateParent;
    }

    public void copyImmutableFieldsFrom(ResourceCollection resource) {
        this.setDateCreated(resource.getDateCreated());
        this.setOwner(resource.getOwner());
        this.setAuthorizedUsers(new HashSet<>(resource.getAuthorizedUsers()));
        this.setSystemManaged(resource.isSystemManaged());
        ((ResourceCollection) this).getManagedResources().addAll(((ResourceCollection) resource).getManagedResources());
        this.setParent(resource.getParent());
    }

    
    @XmlAttribute(name = "parentIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public ResourceCollection getParent() {
        return parent;
    }

    public void setParent(ResourceCollection parent) {
        this.parent = parent;
    }

    public Set<Resource> getUnmanagedResources() {
        return unmanagedResources;
    }

    public void setUnmanagedResources(Set<Resource> unmanagedResources) {
        this.unmanagedResources = unmanagedResources;
    }

    @XmlAttribute
    @Override
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean visible) {
        this.hidden = visible;
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trimToEmpty(name);
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trimToEmpty(description);
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(getName());
    }

    @Override
    public boolean isValid() {
        logger.trace("owner: {} name: {} sort: {}", getOwner(), getName());
        if (!isValidForController()) {
            return false;
        }

        if (sortBy == null) {
            return false;
        }

        return PersistableUtils.isNotNullOrTransient(getOwner());
    }

    @Override
    public String getTitle() {
        return getName();
    }

    public String getFormattedDescription() {
        return formattedDescription;
    }

    public void setFormattedDescription(String adminDescription) {
        this.formattedDescription = adminDescription;
    }

    @JsonView(JsonLookupFilter.class)
    public String getDetailUrl() {
        return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
    }

    @Override
    public String getSlug() {
        return UrlUtils.slugify(getName());
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

    public boolean isSupportsThumbnails() {
        return false;
    }

    @XmlTransient
    public boolean isVisibleAndActive() {
        if (hidden) {
            return false;
        }
        if (getStatus() != Status.ACTIVE) {
            return false;
        }
        return true;
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

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    public SortOption getSecondarySortBy() {
        return secondarySortBy;
    }

    public void setSecondarySortBy(SortOption secondarySortBy) {
        this.secondarySortBy = secondarySortBy;
    }


//    @XmlTransient
    @XmlElementWrapper(name="authorizedUsers")
    @XmlElement(name="authorizedUser")
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.Updatable#markUpdated(org.tdar.core.bean.entity.Person)
     */
    @Override
    public void markUpdated(TdarUser p) {
        if (getOwner() == null) {
            setOwner(p);
        }
        if (getDateCreated() == null) {
            setDateCreated(new Date());
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

    @Override
    public String toString() {
        String own = "no owner -1";
        if (owner != null) {
            own = owner.getProperName() + " " + owner.getId();
        }
        return String.format("%s | collection %s  (creator: %s)", getName(), getId(), own);
    }

    @Override
    @Transient
    public TdarUser getSubmitter() {
        return owner;
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

    @XmlTransient
    public boolean isChangesNeedToBeLogged() {
        return changesNeedToBeLogged;
    }

    public void setChangesNeedToBeLogged(boolean changesNeedToBeLogged) {
        this.changesNeedToBeLogged = changesNeedToBeLogged;
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

    @Transient
    public void setResourceIds(Set<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public String getUrlNamespace() {
        return "collection";
    }

    @Transient
    @XmlTransient
    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public Set<CollectionRevisionLog> getCollectionRevisionLog() {
        return collectionRevisionLog;
    }

    public void setCollectionRevisionLog(Set<CollectionRevisionLog> collectionRevisionLog) {
        this.collectionRevisionLog = collectionRevisionLog;

    }

    @XmlTransient
    @Transient
    @JsonIgnore
    public boolean isNew() {
        if (getDateCreated() == null) {
            return false;
        }

        if (DateTime.now().minusDays(7).isBefore(getDateCreated().getTime())) {
            return true;
        }
        return false;
    }

    @XmlAttribute(required = false)
    public Boolean isSystemManaged() {
        if (systemManaged == null) {
            systemManaged = false;
        }
        return systemManaged;
    }

    public void setSystemManaged(Boolean systemManaged) {
        this.systemManaged = systemManaged;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isDraft() {
        return status == Status.DRAFT;
    }

    @Override
    public boolean isDuplicate() {
        return status == Status.DUPLICATE;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isFlagged() {
        return status == Status.FLAGGED;
    }

    public void setParentIds(Set<Long> parentIds) {
        this.parentIds = parentIds;
    }

    @Transient
    @ElementCollection
    @XmlTransient
    public Set<Long> getParentIds() {
        return parentIds;
    }

    @Transient
    @ElementCollection
    @XmlTransient
    public Set<Long> getAlternateParentIds() {
        return alternateParentIds;
    }

    public void setAlternateParentIds(Set<Long> alternateParentIds) {
        this.alternateParentIds = alternateParentIds;
    }

    @XmlTransient
    @Transient
    public Long getParentId() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getId();
    }

    @Transient
    public Long getAlternateParentId() {
        if (getAlternateParent() == null) {
            return null;
        }
        return getAlternateParent().getId();
    }

    @XmlTransient
    @Transient
    public TreeSet<ResourceCollection> getTransientChildren() {
        return transientChildren;
    }

    public void setTransientChildren(TreeSet<ResourceCollection> transientChildren) {
        this.transientChildren = transientChildren;
    }

    public Collection<String> getAlternateParentNameList() {
        HashSet<String> names = new HashSet<>();
        if (PersistableUtils.isNotNullOrTransient(getAlternateParent())) {
            ResourceCollection hierarchicalCollection = getAlternateParent();
            if (PersistableUtils.isNotNullOrTransient(hierarchicalCollection.getParent())) {
                names.addAll(hierarchicalCollection.getParentNameList());
            }
            if (PersistableUtils.isNotNullOrTransient(hierarchicalCollection.getAlternateParent())) {
                names.addAll(hierarchicalCollection.getAlternateParentNameList());
            }
        }
        return names;
    }

    @XmlTransient
    @Transient
    public boolean isSubCollection() {
        return !isTopLevel();
    }

    @XmlTransient
    @Transient
    public boolean isTopLevel() {
        if ((getParent() == null) || (getParent().isHidden() == true)) {
            return true;
        }
        return false;
    }
}
