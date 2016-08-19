package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tdar.core.bean.resource.Resource;

@DiscriminatorValue(value = "INTERNAL")
@XmlRootElement(name = "internalCollection")
@Entity
public class InternalCollection extends ResourceCollection implements RightsBasedResourceCollection {

    private static final long serialVersionUID = 2238608996291414672L;



    @XmlTransient
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "internalCollections", targetEntity = Resource.class)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Size(min=0,max=1)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.collection.InternalCollection.resources")
    private Set<Resource> resources = new LinkedHashSet<Resource>();


//    @OneToOne(cascade=CascadeType.ALL, mappedBy="internalCollection")
//    @JoinTable(name="collection_resource",joinColumns=@JoinColumn(name="collection_id"), inverseJoinColumns=@JoinColumn(name="resource_id"))
//    private Resource resource;

    //if you serialize this (even if just a list IDs, hibernate will request all necessary fields and do a traversion of the full resource graph (this could crash tDAR if > 100,000)
    @XmlTransient
    public Set<Resource> getResources() {
        return resources;
    }


    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }


    public InternalCollection() {
        this.setType(CollectionType.INTERNAL);
    }

    @Override
    public boolean isValidForController() {
        return true;
    }


    
//    public Resource getResource() {
//        return resource;
//    }
//
//
//    public void setResource(Resource resource) {
//        this.resource = resource;
//    }
    
}
