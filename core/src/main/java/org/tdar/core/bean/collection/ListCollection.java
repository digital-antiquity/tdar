package org.tdar.core.bean.collection;

import java.util.ArrayList;
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
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@DiscriminatorValue(value = "PUBLIC")
@Entity
@XmlRootElement(name = "listCollection")
public class ListCollection extends ResourceCollection implements HierarchicalCollection<ListCollection>, Comparable<ListCollection> {

    private static final long serialVersionUID = 1225586588061994193L;

    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "unmanagedResourceCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.ResourceCollection.unmanagedResources")
    private Set<Resource> unmanagedResources = new LinkedHashSet<Resource>();

    public Set<Resource> getUnmanagedResources() {
        return unmanagedResources;
    }

    public void setUnmanagedResources(Set<Resource> publicResources) {
        this.unmanagedResources = publicResources;
    }

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ListCollection parent;

    @XmlAttribute(name = "parentIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Override
    public ListCollection getParent() {
        return parent;
    }

    @Override
    public void setParent(ListCollection parent) {
        this.parent = parent;
    }

    private transient Set<ListCollection> transientChildren = new LinkedHashSet<>();

    @XmlTransient
    @Transient
    @Override
    public Set<ListCollection> getTransientChildren() {
        return transientChildren;
    }

    @Override
    public void setTransientChildren(Set<ListCollection> transientChildren) {
        this.transientChildren = transientChildren;
    }

    @XmlTransient
    @Override
    public boolean isTopLevel() {
        if ((getParent() == null) || (getParent().isHidden() == true)) {
            return true;
        }
        return false;
    }

    @Transient
    @Override
    public Long getParentId() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getId();
    }

    /*
     * Get all of the resource collections via a tree (actually list of lists)
     */
    @Transient
    @XmlTransient
    // infinite loop because parentTree[0]==self
    @Override
    public List<ListCollection> getHierarchicalResourceCollections() {
        ArrayList<ListCollection> parentTree = new ArrayList<>();
        parentTree.add(this);
        ListCollection collection = this;
        while (collection.getParent() != null) {
            collection = collection.getParent();
            parentTree.add(0, collection);
        }
        return parentTree;
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    @Override
    public int compareTo(ListCollection o) {
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

    @Transient
    @XmlTransient
    @Override
    public List<ListCollection> getVisibleParents() {
        return getVisibleParents(ListCollection.class);
    }

    @XmlTransient
    @Override
    public boolean isTopCollection() {
        return parent == null;
    }

    @XmlTransient
    @Override
    public boolean isSubCollection() {
        return parent != null;
    }

}
