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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@DiscriminatorValue(value = "SHARED")
@Entity
@XmlRootElement(name = "resourceCollection")
public class SharedCollection extends HierarchicalCollection<SharedCollection>
        implements Comparable<SharedCollection>,  RightsBasedResourceCollection, HasName, Sortable {
    private static final long serialVersionUID = 7900346272773477950L;

    public SharedCollection(String title, String description, boolean hidden, SortOption sortOption, DisplayOrientation displayOrientation, TdarUser creator) {
        setName(title);
        setDescription(description);
        setHidden(hidden);
        setSortBy(sortOption);
        setOrientation(displayOrientation);
        setOwner(creator);
        this.setType(CollectionType.SHARED);
    }
    
    public SharedCollection(Long id, String title, String description, SortOption sortOption, boolean hidden) {
        setId(id);
        setName(title);
        setDescription(description);
        setHidden(hidden);
        setSortBy(sortOption);
        this.setType(CollectionType.SHARED);

    }

    public SharedCollection(String title, String description, TdarUser submitter) {
        setName(title);
        setDescription(description);
        setHidden(false);
        this.setOwner(submitter);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
        this.setType(CollectionType.SHARED);
    }

    public SharedCollection(Document document, TdarUser tdarUser) {
        markUpdated(tdarUser);
        getResources().add(document);
        setHidden(false);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
        this.setType(CollectionType.SHARED);
    }

    public SharedCollection() {
        this.setType(CollectionType.SHARED);
        setSortBy(SortOption.TITLE);
        setOrientation(DisplayOrientation.LIST);
    }



    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SharedCollection parent;

    @ManyToOne
    @JoinColumn(name = "alternate_parent_id")
    private SharedCollection alternateParent;
    
    @Override
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
    public SharedCollection getAlternateParent() {
        return alternateParent;
    }

    public void setAlternateParent(SharedCollection alternateParent) {
        this.alternateParent = alternateParent;
    }

    @Override
    public void copyImmutableFieldsFrom(ResourceCollection resource) {
        super.copyImmutableFieldsFrom(resource);
        if (resource instanceof SharedCollection ) {
            this.setParent(((SharedCollection) resource).getParent());
        }
    }
}
