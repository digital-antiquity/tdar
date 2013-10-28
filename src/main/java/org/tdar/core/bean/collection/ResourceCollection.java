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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfile.FetchOverride;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SimpleSearch;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.utils.MessageHelper;
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
 */
@Entity
@Indexed(index = "Collection")
@Table(name = "collection")
@FetchProfiles(value = {
        @FetchProfile(name = "simple", fetchOverrides = {
                @FetchOverride(association = "resources", mode = FetchMode.JOIN, entity = ResourceCollection.class),
                @FetchOverride(association = "authorizedUsers", mode = FetchMode.JOIN, entity = ResourceCollection.class),
                @FetchOverride(association = "user", mode = FetchMode.JOIN, entity = AuthorizedUser.class),
                @FetchOverride(association = "owner", mode = FetchMode.JOIN, entity = ResourceCollection.class),
                @FetchOverride(association = "updater", mode = FetchMode.JOIN, entity = ResourceCollection.class),
                @FetchOverride(association = "parent", mode = FetchMode.JOIN, entity = ResourceCollection.class)
        })
})
public class ResourceCollection extends Persistable.Base implements HasName, Updatable, Indexable, Validatable, Addressable, Comparable<ResourceCollection>,
        SimpleSearch, Sortable, Viewable, DeHydratable {

    private transient boolean viewable;

    // private transient boolean readyToIndex = true;
    public enum CollectionType {
        INTERNAL(MessageHelper.getMessage("collectionType.internal")),
        SHARED(MessageHelper.getMessage("collectionType.internal")),
        PUBLIC(MessageHelper.getMessage("collectionType.public"));

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
    @Fields({
            @Field(name = QueryFieldNames.COLLECTION_NAME_AUTO, norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class))
            , @Field(name = QueryFieldNames.COLLECTION_NAME) })
    // @Boost(1.5f)
    @Length(max = 255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY,
            mappedBy = "resourceCollections", targetEntity = Resource.class)
    // cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE },
    // @JoinTable(name = "collection_resource", joinColumns = { @JoinColumn(name = "collection_id") })
    private Set<Resource> resources = new LinkedHashSet<Resource>();

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", length = 25)
    private SortOption sortBy = DEFAULT_SORT_OPTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_sort_order", length = 25)
    private SortOption secondarySortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "orientation", length = 50)
    private DisplayOrientation orientation = DisplayOrientation.LIST;

    @Field(name = QueryFieldNames.COLLECTION_TYPE)
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type", length = 255)
    private CollectionType type;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_collection_id")
    private Set<AuthorizedUser> authorizedUsers = new LinkedHashSet<AuthorizedUser>();

    @ManyToOne
    @IndexedEmbedded
    @JoinColumn(name = "owner_id", nullable = false)
    @Index(name = "collection_owner_id_idx")
    private Person owner;

    @ManyToOne
    @JoinColumn(name = "updater_id", nullable = true)
    @Index(name = "collection_updater_id_idx")
    private Person updater;

    @Column(nullable = false, name = "date_created")
    private Date dateCreated;

    @Column(nullable = true, name = "date_updated")
    private Date dateUpdated;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @Index(name = "collection_parent_id_idx")
    private ResourceCollection parent;

    private transient Set<ResourceCollection> transientChildren = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean visible = true;

    public ResourceCollection() {
        setDateCreated(new Date());
    }

    public ResourceCollection(Long id, String title, String description, SortOption sortBy, CollectionType type, boolean visible) {
        this(title, description, sortBy, type, visible, null);
        setId(id);
    }

    public ResourceCollection(String title, String description, SortOption sortBy, CollectionType type, boolean visible, Person creator) {
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setType(type);
        setVisible(visible);
        setOwner(creator);
    }

    public ResourceCollection(CollectionType type) {
        this.type = type;
        setDateCreated(new Date());
    }

    public ResourceCollection(Resource resource, Person owner) {
        this(CollectionType.SHARED);
        this.owner = owner;
        getResources().add(resource);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // @Boost(1.2f)
    @Field
    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElementWrapper(name = "resources")
    @XmlElement(name = "resourceRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
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
    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
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

    @Field
    @XmlAttribute
    public boolean isVisible() {
        return visible;
    }

    @Field
    @XmlTransient
    public boolean isTopLevel() {
        if (getParent() == null || getParent().isVisible() == false) {
            return true;
        }
        return false;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /*
     * Convenience Method that provides a list of users that match the permission
     */
    public Set<Person> getUsersWhoCan(GeneralPermissions permission, boolean recurse) {
        Set<Person> people = new HashSet<Person>();
        for (AuthorizedUser user : authorizedUsers) {
            if (user.getEffectiveGeneralPermission() >= permission.getEffectivePermissions()) {
                people.add(user.getUser());
            }
        }
        if (getParent() != null && recurse) {
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
    public void markUpdated(Person p) {
        if (getDateCreated() == null || getOwner() == null) {
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
        if (getParent() == null)
            return null;
        return getParent().getId();
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        return new String[] { "id", "name" };
    }

    /*
     * used for populating the Lucene Index with users that have appropriate rights to modify things in the collection
     */
    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_MODIFY)
    @Transient
    @JSONTransient
    @ElementCollection
    @IndexedEmbedded
    public List<Long> getUsersWhoCanModify() {
        return toUserList(GeneralPermissions.MODIFY_RECORD);
    }

    private List<Long> toUserList(GeneralPermissions permission) {
        ArrayList<Long> users = new ArrayList<Long>();
        HashSet<Person> writable = new HashSet<Person>();
        writable.add(getOwner());
        writable.addAll(getUsersWhoCan(permission, true));
        for (Person p : writable) {
            if (Persistable.Base.isNullOrTransient(p)) {
                continue;
            }
            users.add(p.getId());
        }
        return users;
    }

    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER)
    @Transient
    @JSONTransient
    @ElementCollection
    @IndexedEmbedded
    public List<Long> getUsersWhoCanAdminister() {
        return toUserList(GeneralPermissions.ADMINISTER_GROUP);
    }

    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW)
    @Transient
    @JSONTransient
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
        while (!tree.isEmpty() && !tree_.isEmpty() && tree.get(0) == tree_.get(0)) {
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
        ArrayList<ResourceCollection> parentTree = new ArrayList<ResourceCollection>();
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
            if (!collection.isShared() || !collection.isVisible()) {
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

    /*
     * Get ordered list of parents (ids) of this resources ... great grandfather, grandfather, father, you.
     */
    @Transient
    @Field(name = QueryFieldNames.COLLECTION_TREE)
    @JSONTransient
    @ElementCollection
    @IndexedEmbedded
    public List<Long> getParentIdList() {
        ArrayList<Long> parentIdTree = new ArrayList<Long>();
        for (ResourceCollection collection : getHierarchicalResourceCollections()) {
            parentIdTree.add(collection.getId());
        }
        return parentIdTree;
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(getName());
    }

    @Override
    public boolean isValid() {
        if (isValidForController() || getType() == CollectionType.INTERNAL) {
            if (getType() == CollectionType.SHARED && sortBy == null) {
                return false;
            }
            return (getOwner() != null && getOwner().getId() != null && getOwner().getId() > -1);
        }
        return false;
    }

    @Field(name = QueryFieldNames.TITLE_SORT, norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    @Override
    public String getTitleSort() {
        if (getTitle() == null)
            return "";
        return getTitle().replaceAll(SimpleSearch.TITLE_SORT_REGEX, "");
    }

    @Field
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

    @Transient
    public Person getSubmitter() {
        return owner;
    }

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    @Override
    @XmlTransient
    @Transient
    @JSONTransient
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
    public Person getUpdater() {
        return updater;
    }

    public void setUpdater(Person updater) {
        this.updater = updater;
    }

    public void normalizeAuthorizedUsers() {
        normalizeAuthorizedUsers(authorizedUsers);
    }

    public static final void normalizeAuthorizedUsers(Collection<AuthorizedUser> authorizedUsers) {
        Logger staticLogger = Logger.getLogger(ResourceCollection.class);
        staticLogger.trace("incoming " + authorizedUsers);
        Map<Long, AuthorizedUser> bestMap = new HashMap<Long, AuthorizedUser>();
        Iterator<AuthorizedUser> iterator = authorizedUsers.iterator();
        while (iterator.hasNext()) {
            AuthorizedUser incoming = iterator.next();
            if (incoming == null || incoming.getUser() == null) {
                continue;
            }
            Long user = incoming.getUser().getId();

            AuthorizedUser existing = bestMap.get(user);
            staticLogger.trace(incoming + " <==>" + existing);
            if (existing != null) {
                if (existing.getGeneralPermission().getEffectivePermissions() > incoming.getGeneralPermission().getEffectivePermissions()) {
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
    @JSONTransient
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
}
