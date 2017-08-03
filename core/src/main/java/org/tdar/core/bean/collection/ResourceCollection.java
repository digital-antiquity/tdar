/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.collection;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
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
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.PersistableUtils;
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
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters=true)
@JsonInclude(value=Include.NON_NULL)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "collection_type", length = FieldLength.FIELD_LENGTH_255, discriminatorType = DiscriminatorType.STRING)
@XmlSeeAlso(value = { SharedCollection.class, ListCollection.class })
@SecondaryTable(name = "whitelabel_collection", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public abstract class ResourceCollection extends AbstractPersistable
        implements Updatable, Validatable, DeHydratable, HasSubmitter, XmlLoggable, HasStatus , HasAuthorizedUsers, Sortable,
        OaiDcProvider, HasName, Slugable, Addressable, Indexable, Viewable, Hideable {

    public static final SortOption DEFAULT_SORT_OPTION = SortOption.TITLE;

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private transient boolean changesNeedToBeLogged = false;

    private static final long serialVersionUID = -5308517783896369040L;
    @Column(name="system_managed")
    private Boolean systemManaged = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @XmlTransient
    @Column(name = "collection_type", updatable = false, insertable = false)
    private CollectionType type;


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

    @Column(nullable = false, name = "date_updated")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated;
    
    @OneToMany()
    @JoinColumn(name = "collection_id", foreignKey = @javax.persistence.ForeignKey(value = ConstraintMode.NO_CONSTRAINT),nullable=true)
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
    @CollectionTable(name = "collection_resource", joinColumns = @JoinColumn(name = "collection_id") )
    @Column(name = "resource_id")
    @Immutable
    //fixme: replace resourceIds hack with service/dao with optimized DAO save() method. (TDAR-5605)
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
            @AttributeOverride(name = "custom_header_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "custom_doc_logo_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "featured_resources_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "search_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
            @AttributeOverride(name = "sub_collections_enabled", column = @Column(table = "whitelabel_collection", columnDefinition="boolean default false")),
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
    @Length(max = FieldLength.FIELD_LENGTH_500)
    @NotNull
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_formatted")
    private String formattedDescription;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;


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
        logger.trace("type: {} owner: {} name: {} sort: {}", getType(), getOwner(), getName());
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


    public CollectionType getType() {
        return type;
    }

    protected void setType(CollectionType type) {
        this.type = type;
    }

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
        return String.format("%s collection %s  (creator: %s)", getType(), getId(), own);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void copyImmutableFieldsFrom(ResourceCollection resource) {
        this.setDateCreated(resource.getDateCreated());
        this.setOwner(resource.getOwner());
        this.setType(resource.getType());
        this.setAuthorizedUsers(new HashSet<>(resource.getAuthorizedUsers()));
        this.setSystemManaged(resource.isSystemManaged());
        if (resource instanceof SharedCollection && this instanceof SharedCollection) {
            ((SharedCollection)this).getResources().addAll(((SharedCollection) resource).getResources());
        }
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

    @XmlAttribute(required=false)
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

}
