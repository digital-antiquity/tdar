/**
    @XmlTransient
    @Override
    public boolean isViewable() {
        return viewable;
    }

    @Override
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.collection;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

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

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "collection_type", length = FieldLength.FIELD_LENGTH_255, discriminatorType = DiscriminatorType.STRING)
@XmlSeeAlso(value = { SharedCollection.class, InternalCollection.class, ListCollection.class })
public abstract class ResourceCollection extends AbstractPersistable
        implements Updatable, Validatable, DeHydratable, HasSubmitter, XmlLoggable {

    public static final SortOption DEFAULT_SORT_OPTION = SortOption.TITLE;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private transient boolean changesNeedToBeLogged = false;

    private static final long serialVersionUID = -5308517783896369040L;
    private transient Float score;

    @Enumerated(EnumType.STRING)
    @XmlTransient
    @Column(name = "collection_type", updatable = false, insertable = false)
    private CollectionType type;

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
    public String toString() {
        return String.format("%s collection %s  (creator: %s %s)", getType(), getId(), owner.getProperName(), owner.getId());
    }

    @Override
    public boolean isValid() {
        logger.trace("type: {} owner: {} name: {} sort: {}", getType(), getOwner());
        return PersistableUtils.isNotNullOrTransient(getOwner());
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
}
