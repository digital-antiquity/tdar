/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SimpleSearch;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.index.analyzer.AutocompleteAnalyzer;
import org.tdar.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.index.analyzer.PatternTokenAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;

/**
 * @author Adam Brin
 * 
 */
@Entity
@Indexed(index = "Resource")
@Table(name = "collection")
public class ResourceCollection extends Persistable.Base implements HasName, Updatable, Indexable, Validatable, Comparable<ResourceCollection> {

    public enum CollectionType {
        INTERNAL("Internal"),
        SHARED("Shared");

        private String label;

        private CollectionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    private static final long serialVersionUID = -5308517783896369040L;
    private transient Float score;
    private transient Explanation explanation;

    @Column
    @Fields({ @Field(name = QueryFieldNames.COLLECTION_NAME_AUTO, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class))
            , @Field(name = QueryFieldNames.COLLECTION_NAME, analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)) })
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @XmlTransient
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY,
            mappedBy = "resourceCollections", targetEntity = Resource.class)
    // @JoinTable(name = "collection_resource", joinColumns = { @JoinColumn(name = "collection_id") })
    private Set<Resource> resources = new LinkedHashSet<Resource>();

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order")
    private SortOption sortBy;

    @Field(name = QueryFieldNames.COLLECTION_TYPE)
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "collection_type")
    private CollectionType type;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resourceCollection", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AuthorizedUser> authorizedUsers = new LinkedHashSet<AuthorizedUser>();

    @ManyToOne
    @IndexedEmbedded
    @JoinColumn(name = "owner_id", nullable = false)
    private Person owner;

    @Column(nullable = false, name = "date_created")
    private Date dateCreated;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ResourceCollection parent;

    @Column(nullable = false)
    private boolean visible = true;

    public ResourceCollection() {
        setDateCreated(new Date());
    }

    public ResourceCollection(CollectionType type) {
        this.type = type;
        setDateCreated(new Date());
    }

    @Field(store = Store.YES, analyzer = @Analyzer(impl = KeywordAnalyzer.class), name = QueryFieldNames.ID)
    public Long getIndexedId() {
        return getId();
    }

    public ResourceCollection(Resource resource, Person owner) {
        this(CollectionType.SHARED);
        this.owner = owner;
        getResources().add(resource);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Field(boost = @Boost(1.2f))
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    @XmlIDREF
    @XmlAttribute(name = "userIds")
    public Set<AuthorizedUser> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public void setAuthorizedUsers(Set<AuthorizedUser> users) {
        this.authorizedUsers = users;
    }

    @XmlIDREF
    @XmlAttribute(name = "ownerId")
    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    @XmlIDREF
    @XmlAttribute(name = "parentId")
    public ResourceCollection getParent() {
        return parent;
    }

    public void setParent(ResourceCollection parent) {
        this.parent = parent;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

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
        }
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
    public SortOption getSortBy() {
        return sortBy;
    }

    @Override
    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    public Float getScore() {
        return this.score;
    }

    @Override
    public Explanation getExplanation() {
        return explanation;
    }

    @Override
    public void setExplanation(Explanation ex) {
        this.explanation = ex;
    }

    @Override
    public String toString() {
        return String.format("%s Resource collection %s: %s (creator: %s) [%s]", getType(), getId(), getName(), owner, getResources());
    }

    @Transient
    public Long getParentId() {
        if (parent == null)
            return null;
        return parent.getId();
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        return new String[] { "id", "name" };
    }

    @Field(name = QueryFieldNames.COLLECTION_USERS_WHO_CAN_MODIFY)
    @Analyzer(impl = PatternTokenAnalyzer.class)
    @Transient
    @JSONTransient
    public String getUsersWhoCanModify() {
        StringBuilder sb = new StringBuilder();
        HashSet<Person> writable = new HashSet<Person>();
        writable.add(getOwner());
        writable.addAll(getUsersWhoCan(GeneralPermissions.MODIFY_RECORD, true));
        for (Person p : writable) {
            if (p == null || p.getId() == null)
                continue;
            sb.append(p.getId()).append("|");
        }
        logger.trace("effectiveUsers:" + sb.toString());
        return sb.toString();
    }

    /*
     * Default to sorting by name, but grouping by parentId
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
    
    @Transient 
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
    public List<String> getParentNameList() {
        ArrayList<String> parentNameTree = new ArrayList<String>();
        for (ResourceCollection collection : getHierarchicalResourceCollections()) {
            parentNameTree.add(collection.getName());
        }
        return parentNameTree;
    }

    @Transient
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

    @Field(name = QueryFieldNames.TITLE_SORT, index = Index.UN_TOKENIZED, store = Store.YES)
    public String getTitleSort() {
        if (getTitle() == null)
            return "";
        return getTitle().replaceAll(SimpleSearch.TITLE_SORT_REGEX, "");
    }

    @Field(boost = @Boost(1.5f))
    public String getTitle() {
        return getName();
    }

    public String getKeywords() {
        return null;
    }

    public String getUrlNamespace() {
        return "collection";
    }
    
/* commenting out for Fluvial, but would enable ResourceCollections and Resources to share the same index
 *
 *  @Transient
    @Field(index = Index.UN_TOKENIZED, store = Store.YES, analyzer=@Analyzer(impl = TdarStandardAnalyzer.class), name=QueryFieldNames.SEARCH_TYPE)
    public SimpleSearchType getSimpleSearchType() {
        return SimpleSearchType.COLLECTION; 
    }
    
    
    @Field(name = QueryFieldNames.STATUS, analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class))
    public Status getStatusForSearch() {
        if (getType() == CollectionType.SHARED && visible) {
            return Status.ACTIVE;
        }
        return null;
    }

 */
}
