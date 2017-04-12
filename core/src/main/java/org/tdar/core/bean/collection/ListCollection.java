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
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@DiscriminatorValue(value = "LIST")
@Entity
@XmlRootElement(name = "listCollection")
public class ListCollection extends CustomizableCollection<ListCollection> implements Comparable<ListCollection>, HasName {

    private static final long serialVersionUID = 1225586588061994193L;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ListCollection parent;

    public ListCollection getParent() {
        return parent;
    }

    public void setParent(ListCollection parent) {
        this.parent = parent;
    }

    @ManyToOne
    @JoinColumn(name="include_id")
    private SharedCollection includedCollection;

    
    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "unmanagedResourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.unmanagedResources")
    private Set<Resource> unmanagedResources = new LinkedHashSet<Resource>();

    public ListCollection() {
        setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
        setType(CollectionType.LIST);
    }
    
    public ListCollection(Long id, String title, String description, SortOption sortBy, boolean hidden) {
        setId(id);
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(hidden);
        this.setType(CollectionType.LIST);
        setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
    }

    public ListCollection(String name, String description, SortOption sort, boolean hidden, TdarUser basicUser) {
        setName(name);
        setDescription(description);
        setSortBy(sort);
        setHidden(hidden);
        this.setType(CollectionType.LIST);
        setOwner(basicUser);
        setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
    }

    @XmlTransient
    public Set<Resource> getUnmanagedResources() {
        return unmanagedResources;
    }

    public void setUnmanagedResources(Set<Resource> publicResources) {
        this.unmanagedResources = publicResources;
    }


    /*
     * Get all of the resource collections via a tree (actually list of lists)
     */
    @Transient
    @XmlTransient
    // infinite loop because parentTree[0]==self
    public List<ListCollection> getHierarchicalResourceCollections() {
        return getHierarchicalResourceCollections(ListCollection.class, this);
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    @Override
    public int compareTo(ListCollection o) {
        return compareTo(this, o);
    }

    @Transient
    @XmlTransient
    public List<ListCollection> getVisibleParents() {
        return getVisibleParents(ListCollection.class);
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

    @XmlAttribute(name = "includedIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public SharedCollection getIncludedCollection() {
        return includedCollection;
    }

    public void setIncludedCollection(SharedCollection includedCollection) {
        this.includedCollection = includedCollection;
    }

}
