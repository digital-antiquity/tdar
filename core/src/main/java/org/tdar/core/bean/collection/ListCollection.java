package org.tdar.core.bean.collection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.Resource;

@DiscriminatorValue(value = "LIST")
@Entity
@SecondaryTable(name="whitelabel_collection", pkJoinColumns=@PrimaryKeyJoinColumn(name="id"))
@XmlRootElement(name = "listCollection")
public class ListCollection extends HierarchicalCollection<ListCollection> implements Comparable<ListCollection>, HasName {

    private static final long serialVersionUID = 1225586588061994193L;

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "unmanagedResourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.unmanagedResources")
    private Set<Resource> unmanagedResources = new LinkedHashSet<Resource>();

    public ListCollection() {
        setProperties(new CollectionDisplayProperties());
        setType(CollectionType.LIST);
    }
    
    public ListCollection(Long id, String title, String description, SortOption sortBy, boolean visible) {
        setId(id);
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(visible);
        this.setType(CollectionType.LIST);
        setProperties(new CollectionDisplayProperties());
    }

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

}
