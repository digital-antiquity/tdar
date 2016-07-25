package org.tdar.core.bean.collection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;

@DiscriminatorValue(value = "SHARED")
@Entity
@XmlRootElement(name = "sharedCollection")
public class SharedCollection extends HierarchicalCollection<SharedCollection>
        implements Comparable<SharedCollection>,  RightsBasedResourceCollection, HasName {
    private static final long serialVersionUID = 7900346272773477950L;

    public SharedCollection(String title, String description, SortOption sortBy, boolean hidden, TdarUser creator) {
        super.setProperties(new CollectionDisplayProperties());
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(hidden);
        setOwner(creator);
        this.setType(CollectionType.SHARED);
    }

    public SharedCollection(Long id, String title, String description, SortOption sortBy, boolean hidden) {
        setId(id);
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(hidden);
        this.setType(CollectionType.SHARED);
        super.setProperties(new CollectionDisplayProperties());
    }

    public SharedCollection(Document document, TdarUser tdarUser) {
        markUpdated(tdarUser);
        getResources().add(document);
        this.setType(CollectionType.SHARED);
        super.setProperties(new CollectionDisplayProperties());
    }

    public SharedCollection() {
        this.setType(CollectionType.SHARED);
        super.setProperties(new CollectionDisplayProperties());
    }



    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SharedCollection parent;

    public SharedCollection getParent() {
        return parent;
    }

    public void setParent(SharedCollection parent) {
        this.parent = parent;
    }


    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "sharedCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.SharedCollection.resources")
    private Set<Resource> resources = new LinkedHashSet<Resource>();



    //if you serialize this (even if just a list IDs, hibernate will request all necessary fields and do a traversion of the full resource graph (this could crash tDAR if > 100,000)
    @XmlTransient
    public Set<Resource> getResources() {
        return resources;
    }


    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }


    /*
     * Get all of the resource collections via a tree (actually list of lists)
     */
    @Transient
    @XmlTransient
    // infinite loop because parentTree[0]==self
    public List<SharedCollection> getHierarchicalResourceCollections() {
        return getHierarchicalResourceCollections(SharedCollection.class, this);
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    @Override
    public int compareTo(SharedCollection o) {
        return compareTo(this, o);
    }

    @Transient
    @XmlTransient
    public List<SharedCollection> getVisibleParents() {
        return getVisibleParents(SharedCollection.class);
    }

    @Override
    public boolean isValid() {
        if (isValidForController()) {
            if ((getType() == CollectionType.SHARED) && (getSortBy() == null)) {
                return false;
            }
            return super.isValid();
        }
        return false;
    }


}
