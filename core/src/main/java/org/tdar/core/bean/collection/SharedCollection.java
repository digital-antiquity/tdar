package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

@DiscriminatorValue(value = "SHARED")
@Entity
@XmlRootElement(name = "sharedCollection")
public class SharedCollection extends RightsBasedResourceCollection implements Comparable<SharedCollection> {
    private static final long serialVersionUID = 7900346272773477950L;

    public SharedCollection(String title, String description, SortOption sortBy, boolean visible, TdarUser creator) {
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(visible);
        setOwner(creator);
    }

    public SharedCollection(Long id, String title, String description, SortOption sortBy, boolean visible) {
        setId(id);
        setName(title);
        setDescription(description);
        setSortBy(sortBy);
        setHidden(visible);
    }

    public SharedCollection(Document document, TdarUser tdarUser) {
        markUpdated(tdarUser);
        getResources().add(document);
    }

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SharedCollection parent;


    @XmlAttribute(name = "parentIdRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public SharedCollection getParent() {
        return parent;
    }

    public void setParent(SharedCollection parent) {
        this.parent = parent;
    }

    public SharedCollection() {
    }


    private transient Set<SharedCollection> transientChildren = new LinkedHashSet<>();

    @XmlTransient
    @Transient
    public Set<SharedCollection> getTransientChildren() {
        return transientChildren;
    }

    public void setTransientChildren(Set<SharedCollection> transientChildren) {
        this.transientChildren = transientChildren;
    }

    @XmlTransient
    public boolean isTopLevel() {
        if ((getParent() == null) || (getParent().isHidden() == true)) {
            return true;
        }
        return false;
    }


    @Transient
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
    public List<SharedCollection> getHierarchicalResourceCollections() {
        ArrayList<SharedCollection> parentTree = new ArrayList<>();
        parentTree.add(this);
        SharedCollection collection = this;
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
    public int compareTo(SharedCollection o) {
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
    public List<SharedCollection> getVisibleParents() {
        List<SharedCollection> hierarchicalResourceCollections = getHierarchicalResourceCollections();
        Iterator<SharedCollection> iterator = hierarchicalResourceCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection collection = iterator.next();
            if (!(collection instanceof SharedCollection) || !collection.isHidden()) {
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

    @XmlTransient
    public boolean isTopCollection() {
        return parent == null;
    }

    @XmlTransient
    public boolean isSubCollection() {
        return parent != null;
    }

}
